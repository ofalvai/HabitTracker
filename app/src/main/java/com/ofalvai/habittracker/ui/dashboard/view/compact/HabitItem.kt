/*
 * Copyright 2022 Olivér Falvai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofalvai.habittracker.ui.dashboard.view.compact

import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.ui.common.HorizontalGrid
import com.ofalvai.habittracker.ui.dashboard.view.satisfyingToggleable
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.theme.AppTextStyle
import com.ofalvai.habittracker.ui.theme.composeColor
import com.ofalvai.habittracker.ui.theme.gray1

@Composable
fun HabitItem(
    habit: Habit,
    actions: List<Action>,
    onActionToggle: (Action, Habit, Int) -> Unit,
    onDetailClick: (Habit) -> Unit
) {

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = { onDetailClick(habit) })
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically),
                text = habit.name,
                style = AppTextStyle.habitCompactTitle
            )
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = { onDetailClick(habit) }
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.dashboard_item_details)
                )
            }
        }

        ActionSquares(
            actions = actions,
            habitColor = habit.color,
            onActionToggle = { action, dayIndex ->
                val daysInPast = (Constants.DAY_COUNT - 1 - dayIndex)
                onActionToggle(action, habit, daysInPast)
            })
    }
}

@Composable
fun ActionSquares(
    actions: List<Action>,
    habitColor: Habit.Color,
    onActionToggle: (Action, Int) -> Unit
) {
    var singlePressCounter by remember { mutableStateOf(0) }

    Column {
        HorizontalGrid(Modifier.fillMaxWidth()) {
            actions.mapIndexed { index, action ->
                ActionSquare(
                    activeColor = habitColor.composeColor,
                    toggled = action.toggled,
                    onToggle = { newState ->
                        singlePressCounter = 0
                        onActionToggle(
                            action.copy(toggled = newState),
                            index
                        )
                    },
                    onSinglePress = { singlePressCounter++ }
                )
            }
        }
        if (singlePressCounter >= 3) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.dashboard_toggle_help),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionSquare(
    activeColor: Color,
    toggled: Boolean,
    onToggle: (Boolean) -> Unit,
    onSinglePress: () -> Unit
) {
    val color = if (toggled) activeColor else MaterialTheme.colors.gray1
    val borderColor = if (toggled) activeColor.darken() else Color.Transparent
    val vibrator = LocalContext.current.getSystemService<Vibrator>()!!

    Box(
        modifier = Modifier
            .satisfyingToggleable(vibrator, Dp.Unspecified, true, toggled, onToggle, onSinglePress)
            .aspectRatio(1f)
            .padding(1.dp)
            .border(1.dp, borderColor)
            .background(color)
    )
}

private fun Color.darken() = this.copy(
    red = this.red * 0.85f,
    green = this.green * 0.85f,
    blue = this.blue * 0.85f
)

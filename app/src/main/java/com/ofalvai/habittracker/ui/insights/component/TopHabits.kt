package com.ofalvai.habittracker.ui.insights.component

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import com.ofalvai.habittracker.R
import com.ofalvai.habittracker.persistence.entity.HabitActionCount
import com.ofalvai.habittracker.ui.Screen
import com.ofalvai.habittracker.ui.insights.InsightsViewModel
import com.ofalvai.habittracker.ui.model.HabitId
import com.ofalvai.habittracker.ui.theme.AppIcons
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import com.ofalvai.habittracker.ui.theme.habitInactive
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TopHabits(viewModel: InsightsViewModel, navController: NavController) {
    val topHabits by viewModel.mostSuccessfulHabits.observeAsState(emptyList())

    val onHabitClick: (HabitId) -> Unit = {
        val route = Screen.HabitDetails.buildRoute(habitId = it)
        navController.navigate(route)
    }

    val onSeeAllClick = {
        // TODO
    }

    InsightCard(
        iconPainter = AppIcons.Habits,
        title = stringResource(R.string.insights_tophabits_title),
        description = "See your most often performed habits", // TODO: bar explanation
    ) {
        Column {
            TopHabitsTable(
                habits = topHabits,
                onHabitClick = onHabitClick
            )

            TextButton(
                onClick = onSeeAllClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = stringResource(R.string.insights_tophabits_see_all))
            }
        }
    }
}

@Composable
fun TopHabitsTable(
    habits: List<HabitActionCount>,
    onHabitClick: (HabitId) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        habits.forEachIndexed { index, element ->
            TopHabitsRow(
                index = index + 1,
                habitActionCount = element,
                onClick = onHabitClick
            )
        }
    }
}

@Composable
fun TopHabitsRow(
    index: Int,
    habitActionCount: HabitActionCount,
    onClick: (HabitId) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.width(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = habitActionCount.name,
            modifier = Modifier.weight(0.50f),
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            style = MaterialTheme.typography.body1,
        )

        Text(
            text = habitActionCount.count.toString(),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(0.2f)
        )

        Spacer(Modifier.width(16.dp))

        val activeDays = ChronoUnit.DAYS.between(habitActionCount.first_day, LocalDate.now())
        val habitProgress = habitActionCount.count / activeDays.toFloat()
        HabitBar(
            progress = habitProgress,
            modifier = Modifier.weight(0.2f)
        )

        IconButton(onClick = { onClick(habitActionCount.habit_id) }) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(
                    R.string.insights_tophabits_navigate,
                    habitActionCount.name
                )
            )
        }
    }
}

@Composable
fun HabitBar(
    @FloatRange(from = 0.0, to = 1.0) progress: Float,
    modifier: Modifier = Modifier,
) {
    val height = 8.dp
    val shape = RoundedCornerShape(4.dp)

    Box(modifier = modifier
            .clip(shape)
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colors.habitInactive)
    ) {
        Box(modifier = Modifier
            .clip(shape)
            .fillMaxWidth(fraction = progress)
            .height(height)
            .background(MaterialTheme.colors.primary)
        )
    }
}

@Preview(showBackground = true, widthDp = 300, backgroundColor = 0xFFFDEDCE)
@Composable
fun PreviewTopHabitsTable() {
    val topHabits = listOf(
        HabitActionCount(
            habit_id = 1,
            name = "Short name",
            first_day = LocalDate.now(),
            count = 1567
        ),
        HabitActionCount(
            habit_id = 1,
            name = "Name",
            first_day = LocalDate.now(),
            count = 153
        ),HabitActionCount(
            habit_id = 1,
            name = "Loooong name lorem ipsum dolor sit amet",
            first_day = LocalDate.now(),
            count = 10
        ),HabitActionCount(
            habit_id = 1,
            name = "Meditation",
            first_day = LocalDate.now(),
            count = 9
        ),
        HabitActionCount(
            habit_id = 1,
            name = "Workout",
            first_day = LocalDate.now(),
            count = 3
        )
    )

    HabitTrackerTheme {
        TopHabitsTable(
            habits = topHabits,
            onHabitClick = {  }
        )
    }
}
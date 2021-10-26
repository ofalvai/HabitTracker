/*
 * Copyright 2021 Olivér Falvai
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

package com.ofalvai.habittracker.ui.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ofalvai.habittracker.ui.model.ActionCountChart
import com.ofalvai.habittracker.ui.theme.HabitTrackerTheme
import kotlin.math.max

private const val MinBarHeight = 0.02f
private val BarWidth = 32.dp

@Composable
fun ActionCountChart(
    values: List<ActionCountChart.ChartItem>,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) {
        return
    }
    val maxValue = values.maxByOrNull { it.value }!!.value
    // Reverse items and use reverseLayout = true to initially scroll to end
    val reversedItems = remember(values) { values.reversed() }


    LazyRow(
        modifier = modifier.height(200.dp),
        reverseLayout = true
    ) {
        itemsIndexed(reversedItems) { index, chartItem ->
            Column(
                modifier = Modifier.padding(horizontal = 4.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom,
                    ) {
                    val value = chartItem.value
                    val heightRatio = if (maxValue > 0) value / maxValue.toFloat() else 0f
                    val isEven = index % 2 == 0
                    Text(
                        modifier = Modifier.width(BarWidth).padding(top = 8.dp, bottom = 4.dp),
                        text = value.toString(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.caption
                    )
                    val background = if (isEven) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = .8f) // TODO
                    Box(
                        modifier = Modifier
                            .background(background, shape = RoundedCornerShape(4.dp))
                            .fillMaxHeight(fraction = max(MinBarHeight, heightRatio))
                            .width(BarWidth)
                    )
                }
                Text(
                    modifier = Modifier.width(BarWidth).padding(top = 4.dp),
                    text = chartItem.label,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 400, backgroundColor = 0xFFFDEDCE)
fun PreviewActionCountChart() {
    HabitTrackerTheme {
        Column {
            ActionCountChart(listOf(
                ActionCountChart.ChartItem("6", 2021, 15),
                ActionCountChart.ChartItem("7", 2021, 0),
                ActionCountChart.ChartItem("8", 2021, 7),
                ActionCountChart.ChartItem("9", 2021, 5),
                ActionCountChart.ChartItem("10", 2021, 19)
            ))
            ActionCountChart(listOf(
                ActionCountChart.ChartItem("2", 2021, 0),
                ActionCountChart.ChartItem("3", 2021, 0),
                ActionCountChart.ChartItem("4", 2021, 0),
                ActionCountChart.ChartItem("5", 2021, 0),
                ActionCountChart.ChartItem("6", 2021, 0),
                ActionCountChart.ChartItem("7", 2021, 0),
                ActionCountChart.ChartItem("8", 2021, 0),
                ActionCountChart.ChartItem("9", 2021, 0),
                ActionCountChart.ChartItem("10", 2021, 0)
            ))
            ActionCountChart(listOf(
                ActionCountChart.ChartItem("W22", 2021, 0),
                ActionCountChart.ChartItem("W33", 2021, 1),
                ActionCountChart.ChartItem("W44", 2021, 2),
                ActionCountChart.ChartItem("W55", 2021, 3),
                ActionCountChart.ChartItem("W66", 2021, 4),
                ActionCountChart.ChartItem("W77", 2021, 5),
                ActionCountChart.ChartItem("W88", 2021, 6),
                ActionCountChart.ChartItem("W99", 2021, 7),
                ActionCountChart.ChartItem("W10", 2021, 8)
            ))
        }
    }
}
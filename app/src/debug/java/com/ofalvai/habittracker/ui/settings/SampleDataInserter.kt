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

package com.ofalvai.habittracker.ui.settings

import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import com.ofalvai.habittracker.Dependencies
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.persistence.entity.Action
import com.ofalvai.habittracker.persistence.entity.Habit
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime

@Composable
fun SampleDataInserterButton() {
    var isSuccess by remember { mutableStateOf(false) }
    val inserter = remember { SampleDataInserter(Dependencies.dao) }
    val coroutineScope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        coroutineScope.launch {
            inserter.insert()
            isSuccess = true
        }
    }

    Button(onClick) {
        if (isSuccess) {
            Icon(Icons.Default.Check, null)
        }
        Text(text = "Insert sample data")
    }
}

class SampleDataInserter(private val dao: HabitDao) {

    suspend fun insert() {
        dao.deleteAllHabits()

        val today = LocalDate.now()

        val habits = arrayOf(
            Habit(id = 1, name = "Meditate", color = Habit.Color.Yellow, order = 0, archived = false, notes = ""),
            Habit(id = 2, name = "Exercise for 10 min", color = Habit.Color.Blue, order = 1, archived = false, notes = ""),
            Habit(id = 3, name = "Read for 20 min", color = Habit.Color.Red, order = 3, archived = false, notes = ""),
            Habit(id = 4, name = "Plan my day", color = Habit.Color.Green, order = 4, archived = false, notes = ""),
        )

        dao.insertHabit(*habits)

        val actions = arrayOf(
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(1))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(2))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(7))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(10))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(12))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(13))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(14))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(15))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(16))),
            Action(habit_id = 1, timestamp = toTimestamp(today.minusDays(21))),

            Action(habit_id = 2, timestamp = toTimestamp(today)),
            Action(habit_id = 2, timestamp = toTimestamp(today.minusDays(2))),
            Action(habit_id = 2, timestamp = toTimestamp(today.minusDays(3))),
            Action(habit_id = 2, timestamp = toTimestamp(today.minusDays(4))),
            Action(habit_id = 2, timestamp = toTimestamp(today.minusDays(16))),
            Action(habit_id = 2, timestamp = toTimestamp(today.minusDays(19))),

            Action(habit_id = 3, timestamp = toTimestamp(today.minusDays(2))),
            Action(habit_id = 3, timestamp = toTimestamp(today.minusDays(3))),
            Action(habit_id = 3, timestamp = toTimestamp(today.minusDays(16))),
            Action(habit_id = 3, timestamp = toTimestamp(today.minusDays(18))),
            Action(habit_id = 3, timestamp = toTimestamp(today.minusDays(19))),

            Action(habit_id = 4, timestamp = toTimestamp(today)),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(1))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(2))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(3))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(4))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(5))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(6))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(12))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(13))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(15))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(16))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(17))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(21))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(22))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(23))),
            Action(habit_id = 4, timestamp = toTimestamp(today.minusDays(24))),
        )

        dao.insertAction(*actions)
    }

    private fun toTimestamp(date: LocalDate): Instant {
        return date.atStartOfDay().toInstant(OffsetDateTime.now().offset)
    }

}
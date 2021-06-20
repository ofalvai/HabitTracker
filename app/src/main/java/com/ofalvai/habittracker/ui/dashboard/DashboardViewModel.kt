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

package com.ofalvai.habittracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.mapHabitEntityToModel
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.common.SingleLiveEvent
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.HabitWithActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

class DashboardViewModel(
    private val dao: HabitDao,
    appPreferences: AppPreferences
) : ViewModel() {

    val habitsWithActions: Flow<Result<List<HabitWithActions>>> = dao
        .getHabitsWithActions()
        .distinctUntilChanged()
        .map<List<HabitWithActionsEntity>, Result<List<HabitWithActions>>> {
            Result.Success(mapHabitEntityToModel(it))
        }
        .catch {
            Timber.e(it)
            emit(Result.Failure(it))
        }

    var dashboardConfig by appPreferences::dashboardConfig

    val toggleActionErrorEvent = SingleLiveEvent<Throwable>()

    fun toggleActionFromDashboard(habitId: Int, action: Action, date: LocalDate) {
        viewModelScope.launch {
            try {
                toggleAction(habitId, action, date)
            } catch (e: Throwable) {
                Timber.e(e)
                toggleActionErrorEvent.value = e
            }
        }
    }

    // TODO: duplicated across Dashboard + HabitDetails
    private suspend fun toggleAction(
        habitId: Int,
        updatedAction: Action,
        date: LocalDate,
    ) {
        if (updatedAction.toggled) {
            val newAction = ActionEntity(
                habit_id = habitId,
                timestamp = LocalDateTime.of(date, LocalTime.now())
                    .toInstant(OffsetDateTime.now().offset)
            )
            dao.insertAction(newAction)
        } else {
            dao.deleteAction(updatedAction.id)
        }
    }

}
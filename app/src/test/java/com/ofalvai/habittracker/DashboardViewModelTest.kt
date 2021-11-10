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

package com.ofalvai.habittracker

import app.cash.turbine.test
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.telemetry.Telemetry
import com.ofalvai.habittracker.ui.AppPreferences
import com.ofalvai.habittracker.ui.common.Result
import com.ofalvai.habittracker.ui.dashboard.DashboardEvent
import com.ofalvai.habittracker.ui.dashboard.DashboardViewModel
import com.ofalvai.habittracker.ui.dashboard.ItemMoveEvent
import com.ofalvai.habittracker.ui.dashboard.OnboardingManager
import com.ofalvai.habittracker.ui.model.Action
import com.ofalvai.habittracker.ui.model.ActionHistory
import com.ofalvai.habittracker.ui.model.Habit
import com.ofalvai.habittracker.ui.model.HabitWithActions
import com.ofalvai.habittracker.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.LocalDate
import com.ofalvai.habittracker.persistence.entity.Action as ActionEntity
import com.ofalvai.habittracker.persistence.entity.Habit as HabitEntity
import com.ofalvai.habittracker.persistence.entity.Habit.Color as ColorEntity
import com.ofalvai.habittracker.persistence.entity.HabitWithActions as HabitWithActionsEntity

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    private val dao = mock<HabitDao>()
    private val appPreferences = mock<AppPreferences>()
    private val telemetry = mock<Telemetry>()
    private val onboardingManager = mock<OnboardingManager>()

    private lateinit var viewModel: DashboardViewModel

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Test
    fun `Given habits without actions When VM loaded Then list contains habits with empty history`() = runBlockingTest {
        // Given
        given(dao.getActiveHabitsWithActions()).willReturn(flowOf(listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green, 0, false), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green, 1, false), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green, 2, false), emptyList())
        )))

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            val expectedActionHistory = (1..7).map { Action(0, false, null) }
            val expectedHabits = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Given habits and actions in DB When Flow is observed Then collector is notified once`() = runBlockingTest {
        // Given
        given(dao.getActiveHabitsWithActions()).willReturn(flowOf((listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green, 0, false), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green, 1, false), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green, 2, false), emptyList())
        ))))

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            val expectedActionHistory = (1..7).map { Action(0, false, null) }
            val expectedHabits = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Given observed habit list When a habit is updated Then collector is notified`() = runBlockingTest {
        // Given
        val dateNow = LocalDate.now()
        val instantNow = Instant.now()
        val mockFlow = MutableSharedFlow<List<HabitWithActionsEntity>>(replay = 0)
        given(dao.getActiveHabitsWithActions()).willReturn(mockFlow)

        val initialHabitWithActions = listOf(
            HabitWithActionsEntity(HabitEntity(0, "Meditation", ColorEntity.Green, 0, false), emptyList()),
            HabitWithActionsEntity(HabitEntity(1, "Running", ColorEntity.Green, 1, false), emptyList()),
            HabitWithActionsEntity(HabitEntity(2, "Workout", ColorEntity.Green, 2, false), emptyList())
        )
        val modifiedHabitWithActions = initialHabitWithActions.mapIndexed { index, habit ->
            if (index == 0) {
                habit.copy(actions = listOf(ActionEntity(0, 0, instantNow)))
            } else {
                habit
            }
        }

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            mockFlow.emit(initialHabitWithActions)

            val expectedActionHistory = (1..7).map { Action(0, false, null) }
            val expectedHabits1 = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits1), awaitItem())

            viewModel.toggleActionFromDashboard(0, Action(0, true, instantNow), dateNow)
            mockFlow.emit(modifiedHabitWithActions)

            val expectedHabits2 = listOf(
                HabitWithActions(Habit(0, "Meditation", Habit.Color.Green), expectedActionHistory.take(6) + Action(0, true, instantNow), 1, ActionHistory.Streak(1)),
                HabitWithActions(Habit(1, "Running", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean),
                HabitWithActions(Habit(2, "Workout", Habit.Color.Green), expectedActionHistory, 0, ActionHistory.Clean)
            )
            assertEquals(Result.Success(expectedHabits2), awaitItem())
        }
    }

    @Test
    fun `Given exception when toggling action When action is toggled Then error event is sent to UI`() = runBlockingTest {
        // Given
        val exception = RuntimeException("Mocked error")
        given(dao.insertAction()).willThrow(exception)
        viewModel = createViewModel()

        // When
        launch { // https://github.com/cashapp/turbine/issues/33
            viewModel.dashboardEvent.test {
                val action = Action(id = 0, toggled = true, timestamp = Instant.EPOCH)
                viewModel.toggleActionFromDashboard(habitId = 0, action = action, date = LocalDate.of(2021, 6, 7))

                // Then
                assertEquals(DashboardEvent.ToggleActionError, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Test
    fun `Given exception when loading habits When habits are loaded Then ViewModel state is Failure`() = runBlockingTest {
        // Given
        val exception = RuntimeException("Mocked error")
        val habitFlow = flow<List<HabitWithActionsEntity>> {
            throw exception
        }
        given(dao.getActiveHabitsWithActions()).willReturn(habitFlow)

        // When
        viewModel = createViewModel()

        // Then
        viewModel.habitsWithActions.test {
            val expected = Result.Failure(exception)
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `Given non-continuous item orders When moving an item Then it is persisted in the DB`() = runBlockingTest {
        // Given
        val habit1 = HabitEntity(id = 1, name = "First habit", color = ColorEntity.Yellow, order = 0, false)
        val habit2 = HabitEntity(id = 2, name = "Second habit", color = ColorEntity.Red, order = 9, false)
        given(dao.getHabitPair(1, 2)).willReturn(listOf(habit1, habit2))

        val updateNotificationFlow = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST // Allow tryEmit() to succeed
        )
        given(dao.updateHabitOrders(any(), any(), any(), any())).will {
            updateNotificationFlow.tryEmit(Unit)
        }

        viewModel = createViewModel()

        updateNotificationFlow.test {
            // When
            val event = ItemMoveEvent(firstHabitId = 1, secondHabitId = 2)
            viewModel.persistItemMove(event)

            // Then
            awaitItem()
            verifyNoInteractions(telemetry) // No exceptions
            verify(dao).updateHabitOrders(
                id1 = 1,
                order1 = 9,
                id2 = 2,
                order2 = 0
            )
        }
    }

    @Test
    fun `When moving multiple items Then they are persisted in consistent order to the DB`() = runBlockingTest {
        // Given
        val habit1 = HabitEntity(id = 1, name = "First habit", color = ColorEntity.Yellow, order = 0, false)
        val habit2 = HabitEntity(id = 2, name = "Second habit", color = ColorEntity.Red, order = 1, false)
        val habit3 = HabitEntity(id = 3, name = "Third habit", color = ColorEntity.Red, order = 2, false)
        given(dao.getHabitPair(1, 2)).willReturn(listOf(habit1, habit2))
        given(dao.getHabitPair(1, 3)).willReturn(listOf(habit1.copy(order = 1), habit3))

        val updateNotificationFlow = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST // Allow tryEmit() to succeed
        )
        given(dao.updateHabitOrders(any(), any(), any(), any())).will {
            updateNotificationFlow.tryEmit(Unit)
        }

        viewModel = createViewModel()

        updateNotificationFlow.test {
            // When
            val event1 = ItemMoveEvent(firstHabitId = 1, secondHabitId = 2)
            viewModel.persistItemMove(event1)
            val event2 = ItemMoveEvent(firstHabitId = 1, secondHabitId = 3)
            viewModel.persistItemMove(event2)

            // Then
            awaitItem()
            verify(dao).updateHabitOrders(
                id1 = 1,
                order1 = 1,
                id2 = 2,
                order2 = 0
            )

            awaitItem()
            verify(dao).updateHabitOrders(
                id1 = 1,
                order1 = 2,
                id2 = 3,
                order2 = 1
            )

            verifyNoInteractions(telemetry) // No exceptions
        }
    }

    @Test
    fun `Given error in handling the event When moving an item Then error is handled and processing continues`() = runBlockingTest {
        // Given
        val habit1 = HabitEntity(id = 5, name = "First habit", color = ColorEntity.Yellow, order = 0, false)
        val habit2 = HabitEntity(id = 6, name = "Second habit", color = ColorEntity.Red, order = 1, false)
        given(dao.getHabitPair(5, 6)).willReturn(listOf(habit1, habit2))

        val updateNotificationFlow = MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST // Allow tryEmit() to succeed
        )
        var invocations = 0
        given(dao.updateHabitOrders(any(), any(), any(), any())).will {
            updateNotificationFlow.tryEmit(Unit)
            invocations++
            if (invocations == 1) {
                throw RuntimeException("Mocked error")
            }
        }

        viewModel = createViewModel()

        updateNotificationFlow.test {
            // When
            val event = ItemMoveEvent(firstHabitId = 5, secondHabitId = 6)
            viewModel.persistItemMove(event)

            // Then
            awaitItem()
            verify(telemetry).logNonFatal(any())

            // Try another, non-throwing operation
            viewModel.persistItemMove(ItemMoveEvent(firstHabitId = 5, secondHabitId = 6))

            awaitItem()
            verifyNoMoreInteractions(telemetry)
            verify(dao, times(2)).updateHabitOrders(
                id1 = 5,
                order1 = 1,
                id2 = 6,
                order2 = 0
            )
        }
    }

    private fun createViewModel() = DashboardViewModel(dao, appPreferences, telemetry, onboardingManager)
}
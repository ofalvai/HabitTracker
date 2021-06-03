package com.ofalvai.habittracker.ui.insights

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ofalvai.habittracker.mapper.mapHabitActionCount
import com.ofalvai.habittracker.mapper.mapHabitTopDay
import com.ofalvai.habittracker.mapper.mapSumActionCountByDay
import com.ofalvai.habittracker.persistence.HabitDao
import com.ofalvai.habittracker.ui.model.HeatmapMonth
import com.ofalvai.habittracker.ui.model.TopDayItem
import com.ofalvai.habittracker.ui.model.TopHabitItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

sealed class HeatmapState {
    object Loading: HeatmapState()
    data class Loaded(val heatmapData: HeatmapMonth): HeatmapState()
}

class InsightsViewModel(
    private val habitDao: HabitDao
): ViewModel() {

    val heatmapState = MutableStateFlow<HeatmapState>(HeatmapState.Loading)
    val topHabits = MutableLiveData<List<TopHabitItem>>()
    val habitTopDays = MutableLiveData<List<TopDayItem>>()

    private val habitCount: SharedFlow<Int> = habitDao.getHabitCount().shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1
    )

    init {
        fetchStats()
    }

    fun fetchHeatmap(yearMonth: YearMonth) {
        viewModelScope.launch {
            reloadHeatmap(yearMonth)
        }
    }

    private fun fetchStats() {
        viewModelScope.launch {
            // TODO: parallel coroutines
            reloadHeatmap(yearMonth = YearMonth.now())
            reloadTopHabits()
            reloadHabitTopDays()
        }
    }

    private suspend fun reloadHeatmap(yearMonth: YearMonth) {
        heatmapState.value = HeatmapState.Loading

        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val actionCountList = habitDao.getSumActionCountByDay(startDate, endDate)
        val habitCount = habitCount.first()

        heatmapState.value = HeatmapState.Loaded(
            mapSumActionCountByDay(
                entityList = actionCountList,
                yearMonth,
                habitCount
            )
        )
    }

    private suspend fun reloadTopHabits() {
        topHabits.value = habitDao
            .getMostSuccessfulHabits(100) // TODO: smaller number when "See all" screen is done
            .filter { it.first_day != null }
            .map { mapHabitActionCount(it, LocalDate.now()) }
    }

    private suspend fun reloadHabitTopDays() {
        habitTopDays.value = habitDao
            .getTopDayForHabits()
            .map { mapHabitTopDay(it) }
    }
}
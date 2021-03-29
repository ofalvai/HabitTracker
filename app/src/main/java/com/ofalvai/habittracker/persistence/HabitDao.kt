package com.ofalvai.habittracker.persistence

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ofalvai.habittracker.persistence.entity.*
import java.time.Instant

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit")
    suspend fun getHabits(): List<Habit>

    @Insert
    suspend fun insertHabit(vararg habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    // TODO: limit by timestamp
    @Transaction
    @Query("SELECT * FROM habit")
    fun getHabitsWithActions(): LiveData<List<HabitWithActions>>

    @Transaction
    @Query("SELECT * FROM habit WHERE id = :habitId")
    suspend fun getHabitWithActions(habitId: Int): HabitWithActions

    @Query("SELECT * FROM `action` WHERE habit_id = :habitId")
    suspend fun getActionsForHabit(habitId: Int): List<Action>

    @Query("SELECT * FROM `action` WHERE timestamp >= :after")
    suspend fun getActionsAfter(after: Instant): List<Action>

    @Insert
    suspend fun insertAction(vararg action: Action)

    @Query("DELETE FROM `action` WHERE id = :id")
    suspend fun deleteAction(id: Int)

    /**
     * When there are no actions returns UNIX epoch time as first_day and action_count of 0
     */
    @Query(
        """SELECT
                min(timestamp) as first_day,
                count(*) as action_count
            FROM `action`
            WHERE habit_id = :habitId
    """
    )
    suspend fun getCompletionRate(habitId: Int): ActionCompletionRate

    @Query(
        """SELECT 
                strftime('%Y', timestamp / 1000, 'unixepoch', 'localtime') as year,
                strftime('%m', timestamp / 1000, 'unixepoch', 'localtime') as month,
                count(*) as action_count
            FROM `action`
            WHERE habit_id = :habitId
            GROUP BY year, month"""
    )
    suspend fun getActionCountByMonth(habitId: Int): List<ActionCountByMonth>

    // Week of year calculation explanation: https://stackoverflow.com/a/15511864/745637
    @Query(
        """SELECT
                strftime('%Y', date(timestamp / 1000, 'unixepoch', 'localtime', '-3 days', 'weekday 4')) as year,
                (strftime('%j', date(timestamp / 1000, 'unixepoch', 'localtime', '-3 days', 'weekday 4')) - 1) / 7 + 1 as week,
                count(*) as action_count
            FROM `action`
            WHERE habit_id = :habitId
            GROUP BY year, week"""
    )
    suspend fun getActionCountByWeek(habitId: Int): List<ActionCountByWeek>
}
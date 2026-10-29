package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DinLipiDao {
  @Query("SELECT * FROM routines ORDER BY hour ASC, minute ASC, id ASC")
  fun observeRoutines(): Flow<List<RoutineEntity>>

  @Query("SELECT * FROM routines WHERE isReminderEnabled = 1")
  suspend fun getEnabledRoutines(): List<RoutineEntity>

  @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
  suspend fun getRoutineById(id: Long): RoutineEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRoutine(routine: RoutineEntity): Long

  @Update
  suspend fun updateRoutine(routine: RoutineEntity)

  @Query("DELETE FROM routines WHERE id = :id")
  suspend fun deleteRoutineById(id: Long)

  // Completion logs for streaks & weekly progress charts
  @Query("SELECT * FROM routine_completions ORDER BY dayKey DESC")
  fun observeCompletions(): Flow<List<RoutineCompletionEntity>>

  @Query(
    "SELECT COUNT(*) FROM routine_completions WHERE routineId = :routineId AND dayKey = :dayKey"
  )
  suspend fun isRoutineCompletedOnDay(routineId: Long, dayKey: Int): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCompletion(completion: RoutineCompletionEntity)

  @Query("DELETE FROM routine_completions WHERE routineId = :routineId AND dayKey = :dayKey")
  suspend fun deleteCompletion(routineId: Long, dayKey: Int)

  @Query("DELETE FROM routine_completions WHERE routineId = :routineId")
  suspend fun deleteCompletionsForRoutine(routineId: Long)

  // Focus Pomodoro sessions
  @Query("SELECT * FROM focus_sessions ORDER BY completedAt DESC")
  fun observeFocusSessions(): Flow<List<FocusSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFocusSession(session: FocusSessionEntity): Long

  // To-Do tasks
  @Query("SELECT * FROM todos ORDER BY isCompleted ASC, createdAt DESC")
  fun observeTodos(): Flow<List<TodoEntity>>

  @Query(
    "SELECT * FROM todos WHERE isCompleted = 0 AND isReminderEnabled = 1 AND scheduledHour IS NOT NULL AND scheduledMinute IS NOT NULL"
  )
  suspend fun getEnabledTimedTodos(): List<TodoEntity>

  @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
  suspend fun getTodoById(id: Long): TodoEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTodo(todo: TodoEntity): Long

  @Update
  suspend fun updateTodo(todo: TodoEntity)

  @Query("DELETE FROM todos WHERE id = :id")
  suspend fun deleteTodoById(id: Long)

  @Query("SELECT * FROM todos WHERE isCompleted = 1")
  suspend fun getCompletedTodos(): List<TodoEntity>

  @Query("DELETE FROM todos WHERE isCompleted = 1")
  suspend fun deleteCompletedTodos()
}

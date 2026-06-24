package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReforgeDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getAllHabitsDirect(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Int)

    // --- Relapse Events ---
    @Query("SELECT * FROM relapse_events ORDER BY timestamp DESC")
    fun getAllRelapseEvents(): Flow<List<RelapseEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelapseEvent(event: RelapseEvent)

    // --- Journal Entries ---
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    // --- Daily Check-ins ---
    @Query("SELECT * FROM daily_checkins ORDER BY date DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckIn>>

    @Query("SELECT * FROM daily_checkins WHERE date = :date")
    suspend fun getCheckInForDate(date: String): DailyCheckIn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckIn)

    // --- Coach Messages ---
    @Query("SELECT * FROM coach_messages ORDER BY timestamp ASC")
    fun getCoachMessages(): Flow<List<CoachMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoachMessage(message: CoachMessage)

    @Query("DELETE FROM coach_messages")
    suspend fun clearCoachMessages()

    // --- Addiction Clocks ---
    @Query("SELECT * FROM addiction_clocks")
    fun getAllAddictionClocks(): Flow<List<AddictionClock>>

    @Query("SELECT * FROM addiction_clocks")
    suspend fun getAllAddictionClocksDirect(): List<AddictionClock>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddictionClock(clock: AddictionClock)

    // --- Habit Logs ---
    @Query("SELECT * FROM habit_logs")
    fun getAllHabitLogs(): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLog)

    // --- Workouts ---
    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkoutsDirect(): List<Workout>

    @Query("SELECT * FROM workouts WHERE date = :date")
    fun getWorkoutsForDate(date: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE date = :date")
    suspend fun getWorkoutsForDateDirect(date: String): List<Workout>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Query("DELETE FROM workouts WHERE date = :date")
    suspend fun clearWorkoutsForDate(date: String)

    // --- Meals ---
    @Query("SELECT * FROM meals WHERE date = :date")
    fun getMealsForDate(date: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE date = :date")
    suspend fun getMealsForDateDirect(date: String): List<Meal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE date = :date")
    suspend fun clearMealsForDate(date: String)

    @Query("SELECT * FROM meals WHERE date >= :date")
    suspend fun getMealsFromDateDirect(date: String): List<Meal>

    // --- Weight History ---
    @Query("SELECT * FROM weight_history ORDER BY date ASC")
    fun getWeightHistory(): Flow<List<WeightHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightHistory)

    // --- Daily Coach Analysis ---
    @Query("SELECT * FROM daily_coach_analyses ORDER BY date DESC")
    fun getAllDailyCoachAnalyses(): Flow<List<DailyCoachAnalysis>>

    @Query("SELECT * FROM daily_coach_analyses WHERE date = :date")
    suspend fun getDailyCoachAnalysisForDate(date: String): DailyCoachAnalysis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCoachAnalysis(analysis: DailyCoachAnalysis)

    // --- Exercise Database ---
    @Query("SELECT * FROM exercise_database ORDER BY exercise_id ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercise_database ORDER BY exercise_id ASC")
    suspend fun getAllExercisesDirect(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("DELETE FROM exercise_database")
    suspend fun clearExercises()

    // --- Confidence Challenges ---
    @Query("SELECT * FROM confidence_challenges ORDER BY day ASC")
    fun getAllConfidenceChallenges(): Flow<List<ConfidenceChallenge>>

    @Query("SELECT * FROM confidence_challenges ORDER BY day ASC")
    suspend fun getAllConfidenceChallengesDirect(): List<ConfidenceChallenge>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfidenceChallenge(challenge: ConfidenceChallenge)

    @Update
    suspend fun updateConfidenceChallenge(challenge: ConfidenceChallenge)

    @Query("DELETE FROM confidence_challenges")
    suspend fun clearConfidenceChallenges()
}

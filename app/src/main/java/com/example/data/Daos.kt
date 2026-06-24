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

    // --- Goals ---
    @Query("SELECT * FROM goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY id ASC")
    suspend fun getAllGoalsDirect(): List<Goal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Int)

    @Query("DELETE FROM goals")
    suspend fun clearGoals()

    // --- Habits ---
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getAllHabitsDirect(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Int)

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    // --- Habit Events ---
    @Query("SELECT * FROM habit_events ORDER BY timestamp DESC")
    fun getAllHabitEvents(): Flow<List<HabitEvent>>

    @Query("SELECT * FROM habit_events ORDER BY timestamp DESC")
    suspend fun getAllHabitEventsDirect(): List<HabitEvent>

    @Query("SELECT * FROM habit_events WHERE habitId = :habitId ORDER BY timestamp DESC")
    fun getHabitEventsForHabit(habitId: Int): Flow<List<HabitEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitEvent(event: HabitEvent)

    @Query("DELETE FROM habit_events WHERE id = :id")
    suspend fun deleteHabitEvent(id: Int)

    @Query("DELETE FROM habit_events")
    suspend fun clearHabitEvents()

    // --- Milestones ---
    @Query("SELECT * FROM milestones ORDER BY id ASC")
    fun getAllMilestones(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones ORDER BY id ASC")
    suspend fun getAllMilestonesDirect(): List<Milestone>

    @Query("SELECT * FROM milestones WHERE habitId = :habitId ORDER BY targetDays ASC")
    fun getMilestonesForHabit(habitId: Int): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone): Long

    @Query("DELETE FROM milestones")
    suspend fun clearMilestones()

    // --- Milestone Progress ---
    @Query("SELECT * FROM milestone_progress")
    fun getAllMilestoneProgress(): Flow<List<MilestoneProgress>>

    @Query("SELECT * FROM milestone_progress WHERE milestoneId = :milestoneId LIMIT 1")
    suspend fun getMilestoneProgressForMilestone(milestoneId: Int): MilestoneProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestoneProgress(progress: MilestoneProgress)

    @Query("DELETE FROM milestone_progress")
    suspend fun clearMilestoneProgress()

    // --- AI Memories ---
    @Query("SELECT * FROM ai_memories WHERE id = 1")
    fun getAiMemory(): Flow<AiMemory?>

    @Query("SELECT * FROM ai_memories WHERE id = 1")
    suspend fun getAiMemoryDirect(): AiMemory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiMemory(memory: AiMemory)

    // --- Daily Check-ins ---
    @Query("SELECT * FROM daily_checkins ORDER BY date DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckIn>>

    @Query("SELECT * FROM daily_checkins WHERE date = :date")
    suspend fun getCheckInForDate(date: String): DailyCheckIn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckIn)

    // --- Coach Insights ---
    @Query("SELECT * FROM coach_insights ORDER BY date DESC")
    fun getAllCoachInsights(): Flow<List<CoachInsight>>

    @Query("SELECT * FROM coach_insights WHERE date = :date")
    suspend fun getCoachInsightForDate(date: String): CoachInsight?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoachInsight(insight: CoachInsight)

    @Query("DELETE FROM coach_insights")
    suspend fun clearCoachInsights()

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

    @Query("DELETE FROM addiction_clocks")
    suspend fun clearAddictionClocks()

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

    // --- Hair Logs ---
    @Query("SELECT * FROM hair_logs ORDER BY date DESC")
    fun getAllHairLogs(): Flow<List<HairLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHairLog(log: HairLog)

    // --- Photo Analysis Logs ---
    @Query("SELECT * FROM photo_analysis_logs ORDER BY date DESC")
    fun getAllPhotoAnalyses(): Flow<List<PhotoAnalysisLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotoAnalysis(log: PhotoAnalysisLog)

    // --- Legacy / Restored entities for screen compilation compatibility ---
    @Query("SELECT * FROM relapse_events ORDER BY timestamp DESC")
    fun getAllRelapseEvents(): Flow<List<RelapseEvent>>

    @Query("SELECT * FROM relapse_events ORDER BY timestamp DESC")
    suspend fun getAllRelapseEventsDirect(): List<RelapseEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelapseEvent(event: RelapseEvent)

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Query("SELECT * FROM daily_coach_analyses ORDER BY date DESC")
    fun getAllDailyCoachAnalyses(): Flow<List<DailyCoachAnalysis>>

    @Query("SELECT * FROM daily_coach_analyses WHERE date = :date")
    suspend fun getDailyCoachAnalysisForDate(date: String): DailyCoachAnalysis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCoachAnalysis(analysis: DailyCoachAnalysis)

    // --- Deletion Queries for Reset ---
    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    @Query("DELETE FROM ai_memories")
    suspend fun clearAiMemories()

    @Query("DELETE FROM daily_checkins")
    suspend fun clearDailyCheckIns()

    @Query("DELETE FROM workouts")
    suspend fun clearWorkouts()

    @Query("DELETE FROM meals")
    suspend fun clearMeals()

    @Query("DELETE FROM weight_history")
    suspend fun clearWeightHistory()

    @Query("DELETE FROM hair_logs")
    suspend fun clearHairLogs()

    @Query("DELETE FROM photo_analysis_logs")
    suspend fun clearPhotoAnalysisLogs()

    @Query("DELETE FROM relapse_events")
    suspend fun clearRelapseEvents()

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournalEntries()

    @Query("DELETE FROM daily_coach_analyses")
    suspend fun clearDailyCoachAnalyses()
}

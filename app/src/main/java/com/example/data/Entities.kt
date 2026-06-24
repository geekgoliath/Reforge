package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val age: Int = 0,
    val dob: String = "",
    val birthTime: String = "",
    val birthPlace: String = "",
    val weight: Float = 0f,
    val height: Float = 0f,
    val neck: Float = 0f,
    val waist: Float = 0f,
    val activityLevel: String = "Sedentary",
    val alcoholFrequency: String = "Abstinent",
    val smokingFrequency: String = "Abstinent",
    val sleepHours: Float = 8f,
    val goals: String = "", // Comma-separated goals summary
    val level: Int = 1,
    val xp: Int = 0,
    val isOnboarded: Boolean = false,
    val addictions: String = "", // Comma-separated selected addictions
    val zodiacTheme: String = "",
    val recallScore: Int = 0,
    val readingStreak: Int = 0,
    val speakingPracticeMinutes: Int = 0,
    val hairType: String = "Straight",
    val hairFallSeverity: String = "Mild",
    val familyBaldness: String = "None",
    val scalpCondition: String = "Normal",
    val budget: String = "Medium",
    val currentLocation: String = "Delhi, India",
    val foodPreferences: String = "Eggs, Chicken, Bananas, Curd",
    val foodDislikes: String = "Oats"
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetDetails: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isBadHabit: Boolean = false,
    val category: String = "Routine", // e.g. "Addiction", "Routine", "Diet", "Workout", "Mindset"
    val severity: String = "Mild", // "Mild", "Moderate", "Severe"
    val createdByAi: Boolean = false,
    val createdByUser: Boolean = true,
    val lastCompletedDate: String = "", // e.g. "2026-06-18"
    val streak: Int = 0
)

@Entity(tableName = "habit_events")
data class HabitEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int, // Linked to Habit.id
    val date: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val isRelapse: Boolean = false, // If true, it's a relapse; if false, it's positive completion
    val trigger: String = "",
    val location: String = "",
    val companion: String = "Alone",
    val emotion: String = "",
    val timeOfDay: String = "",
    val notes: String = "" // CBT analysis / advice text
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int, // Linked to Habit.id
    val title: String, // e.g. "Milestone 1: 3 Days Clean"
    val description: String = "",
    val targetDays: Int
)

@Entity(tableName = "milestone_progress")
data class MilestoneProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val milestoneId: Int, // Linked to Milestone.id
    val currentDays: Int = 0,
    val isCompleted: Boolean = false,
    val completionDate: String = ""
)



@Entity(tableName = "ai_memories")
data class AiMemory(
    @PrimaryKey val id: Int = 1,
    val memoryJson: String = "{}"
)

@Entity(tableName = "daily_checkins")
data class DailyCheckIn(
    @PrimaryKey val date: String, // e.g. "2026-06-18"
    val mood: Int, // 1: bad, 2: meh, 3: good, 4: great
    val energy: Int, // 1 to 10
    val sleepQuality: Int, // 1 to 10
    val cravings: Int, // 1 to 10
    val weight: Float = 0f
)

@Entity(tableName = "coach_insights")
data class CoachInsight(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val insight: String = "",
    val risk: String = "",
    val action: String = "",
    val tomorrowFocus: String = "",
    val recoveryScore: Int = 70,
    val riskLevel: String = "Low",
    val actionPlan: String = "" // Semicolon separated list of items
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "yyyy-MM-dd"
    val name: String, // e.g. "PUSH DAY"
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weight: Float,
    val isCompleted: Boolean = false
)

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "yyyy-MM-dd"
    val name: String, // e.g. "BREAKFAST"
    val description: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val isEaten: Boolean = false
)

@Entity(tableName = "weight_history")
data class WeightHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "yyyy-MM-dd"
    val weight: Float
)

@Entity(tableName = "coach_messages")
data class CoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "addiction_clocks")
data class AddictionClock(
    @PrimaryKey val addictionName: String, // e.g. "Alcohol"
    val lastResetTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercise_database")
data class Exercise(
    @PrimaryKey val exercise_id: String,
    val name: String,
    val body_part: String,
    val difficulty: String,
    val equipment: String,
    val video_url: String
)

@Entity(tableName = "confidence_challenges")
data class ConfidenceChallenge(
    @PrimaryKey val day: Int,
    val title: String,
    val description: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val isCompleted: Boolean = false,
    val completionTimestamp: Long = 0L,
    val notes: String = "" // Reflections
)

@Entity(tableName = "hair_logs")
data class HairLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "yyyy-MM-dd"
    val density: Int, // 1-10
    val shedding: Int, // 1-10
    val hairlineChange: Int, // 1-10
    val aiCauses: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "photo_analysis_logs")
data class PhotoAnalysisLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "yyyy-MM-dd"
    val frontPhotoUri: String = "",
    val sidePhotoUri: String = "",
    val backPhotoUri: String = "",
    val forwardHeadAssessment: String = "",
    val roundedShouldersAssessment: String = "",
    val pelvicTiltAssessment: String = "",
    val bodyFatEstimate: Float = 0f,
    val posturePlan: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "relapse_events")
data class RelapseEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val addiction: String,
    val timestamp: Long = System.currentTimeMillis(),
    val trigger: String,
    val location: String,
    val companion: String = "Alone",
    val emotion: String,
    val timeOfDay: String = "",
    val preventionPlan: String
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val text: String,
    val summary: String = "",
    val emotionalAnalysis: String = "",
    val triggersIdentified: String = "",
    val wins: String = "",
    val mistakes: String = "",
    val tomorrowFocus: String = ""
)

@Entity(tableName = "daily_coach_analyses")
data class DailyCoachAnalysis(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val patterns: String,
    val relapseRisk: String, // "Low" / "Medium" / "High"
    val recoveryScore: Int,
    val relapsePatterns: String,
    val missedWorkoutPatterns: String,
    val sleepIssues: String,
    val confidenceTrends: String,
    val todayFocus: String,
    val riskLevel: String, // "Low" / "Medium" / "High"
    val actionPlan: String // Comma-separated or JSON list of items
)

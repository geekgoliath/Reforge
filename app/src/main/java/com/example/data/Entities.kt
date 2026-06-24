package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Vikas",
    val age: Int = 35,
    val dob: String = "1991-03-15",
    val birthTime: String = "14:30",
    val birthPlace: String = "Delhi, India",
    val weight: Float = 78.4f,
    val height: Float = 178f,
    val neck: Float = 38f,
    val waist: Float = 90f,
    val activityLevel: String = "Moderate",
    val alcoholFrequency: String = "Weekly",
    val smokingFrequency: String = "Daily",
    val sleepHours: Float = 7f,
    val goals: String = "Gain Muscle,Quit Smoking,Quit Alcohol",
    val level: Int = 3,
    val xp: Int = 650,
    val isOnboarded: Boolean = true,
    val addictions: String = "Alcohol,Smoking", // Comma-separated selected addictions
    val zodiacTheme: String = "Saturn\'s alignment aligns with your self-purification.",
    val recallScore: Int = 80,
    val readingStreak: Int = 3,
    val speakingPracticeMinutes: Int = 15
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isBadHabit: Boolean = false,
    val lastCompletedDate: String = "", // e.g. "2026-06-18"
    val streak: Int = 0
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String,
    val completed: Boolean
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

@Entity(tableName = "daily_checkins")
data class DailyCheckIn(
    @PrimaryKey val date: String, // e.g. "2026-06-18"
    val mood: Int, // 1: bad, 2: meh, 3: good, 4: great
    val energy: Int, // 1 to 10
    val sleepQuality: Int, // 1 to 10
    val cravings: Int, // 1 to 10
    val weight: Float = 0f
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



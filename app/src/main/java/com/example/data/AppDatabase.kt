package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        Goal::class,
        Habit::class,
        HabitEvent::class,
        Milestone::class,
        MilestoneProgress::class,
        AiMemory::class,
        DailyCheckIn::class,
        CoachInsight::class,
        Workout::class,
        Meal::class,
        WeightHistory::class,
        CoachMessage::class,
        AddictionClock::class,
        Exercise::class,
        ConfidenceChallenge::class,
        HairLog::class,
        PhotoAnalysisLog::class,
        RelapseEvent::class,
        JournalEntry::class,
        DailyCoachAnalysis::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): ReforgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reforge_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

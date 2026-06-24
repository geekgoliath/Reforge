package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        Habit::class,
        HabitLog::class,
        Workout::class,
        Meal::class,
        WeightHistory::class,
        RelapseEvent::class,
        JournalEntry::class,
        DailyCheckIn::class,
        CoachMessage::class,
        AddictionClock::class,
        DailyCoachAnalysis::class,
        Exercise::class,
        ConfidenceChallenge::class
    ],
    version = 10,
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

package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ReforgeRepository(private val dao: ReforgeDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allHabits: Flow<List<Habit>> = dao.getAllHabits()
    val relapseEvents: Flow<List<RelapseEvent>> = dao.getAllRelapseEvents()
    val journalEntries: Flow<List<JournalEntry>> = dao.getAllJournalEntries()
    val checkIns: Flow<List<DailyCheckIn>> = dao.getAllCheckIns()
    val coachMessages: Flow<List<CoachMessage>> = dao.getCoachMessages()
    val addictionClocks: Flow<List<AddictionClock>> = dao.getAllAddictionClocks()
    val habitLogs: Flow<List<HabitLog>> = dao.getAllHabitLogs()
    val weightHistory: Flow<List<WeightHistory>> = dao.getWeightHistory()
    val dailyCoachAnalyses: Flow<List<DailyCoachAnalysis>> = dao.getAllDailyCoachAnalyses()
    val allExercises: Flow<List<Exercise>> = dao.getAllExercises()
    val confidenceChallenges: Flow<List<ConfidenceChallenge>> = dao.getAllConfidenceChallenges()

    suspend fun getAllExercisesDirect(): List<Exercise> = dao.getAllExercisesDirect()
    suspend fun insertExercise(exercise: Exercise) = dao.insertExercise(exercise)
    suspend fun clearExercises() = dao.clearExercises()

    suspend fun getAllConfidenceChallengesDirect(): List<ConfidenceChallenge> = dao.getAllConfidenceChallengesDirect()
    suspend fun insertConfidenceChallenge(challenge: ConfidenceChallenge) = dao.insertConfidenceChallenge(challenge)
    suspend fun updateConfidenceChallenge(challenge: ConfidenceChallenge) = dao.updateConfidenceChallenge(challenge)
    suspend fun clearConfidenceChallenges() = dao.clearConfidenceChallenges()

    fun getWorkoutsForDate(date: String): Flow<List<Workout>> = dao.getWorkoutsForDate(date)
    suspend fun getWorkoutsForDateDirect(date: String): List<Workout> = dao.getWorkoutsForDateDirect(date)
    suspend fun getAllWorkoutsDirect(): List<Workout> = dao.getAllWorkoutsDirect()
    suspend fun insertWorkout(workout: Workout) = dao.insertWorkout(workout)
    suspend fun updateWorkout(workout: Workout) = dao.updateWorkout(workout)
    suspend fun clearWorkoutsForDate(date: String) = dao.clearWorkoutsForDate(date)

    fun getMealsForDate(date: String): Flow<List<Meal>> = dao.getMealsForDate(date)
    suspend fun getMealsForDateDirect(date: String): List<Meal> = dao.getMealsForDateDirect(date)
    suspend fun getMealsFromDateDirect(date: String): List<Meal> = dao.getMealsFromDateDirect(date)
    suspend fun insertMeal(meal: Meal) = dao.insertMeal(meal)
    suspend fun updateMeal(meal: Meal) = dao.updateMeal(meal)
    suspend fun clearMealsForDate(date: String) = dao.clearMealsForDate(date)

    suspend fun insertWeightEntry(entry: WeightHistory) = dao.insertWeightEntry(entry)
    suspend fun insertHabitLog(log: HabitLog) = dao.insertHabitLog(log)

    suspend fun insertUserProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dao.updateUserProfile(profile)
    }

    suspend fun insertHabit(habit: Habit) {
        dao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        dao.updateHabit(habit)
    }

    suspend fun deleteHabit(id: Int) {
        dao.deleteHabit(id)
    }

    suspend fun insertRelapseEvent(event: RelapseEvent) {
        dao.insertRelapseEvent(event)
    }

    suspend fun insertJournalEntry(entry: JournalEntry) {
        dao.insertJournalEntry(entry)
    }

    suspend fun getCheckInForDate(date: String): DailyCheckIn? {
        return dao.getCheckInForDate(date)
    }

    suspend fun insertCheckIn(checkIn: DailyCheckIn) {
        dao.insertCheckIn(checkIn)
    }

    suspend fun getDailyCoachAnalysisForDate(date: String): DailyCoachAnalysis? {
        return dao.getDailyCoachAnalysisForDate(date)
    }

    suspend fun insertDailyCoachAnalysis(analysis: DailyCoachAnalysis) {
        dao.insertDailyCoachAnalysis(analysis)
    }

    suspend fun insertCoachMessage(message: CoachMessage) {
        dao.insertCoachMessage(message)
    }

    suspend fun clearCoachMessages() {
        dao.clearCoachMessages()
    }

    suspend fun insertAddictionClock(clock: AddictionClock) {
        dao.insertAddictionClock(clock)
    }

    suspend fun addXp(amount: Int) {
        val profile = dao.getUserProfileDirect() ?: UserProfile()
        var newXp = profile.xp + amount
        var newLevel = profile.level
        while (newXp >= 1000) {
            newXp -= 1000
            newLevel += 1
        }
        dao.insertUserProfile(profile.copy(xp = newXp, level = newLevel))
    }

    suspend fun preWalkData() {
        if (dao.getAllExercisesDirect().isEmpty()) {
            val defaultExercises = listOf(
                Exercise("ex_face_pull", "Face Pull", "Shoulders/Upper Back", "Beginner", "Cable Machine", "https://www.youtube.com/watch?v=eiO8Jg8S9YI"),
                Exercise("ex_wall_angel", "Wall Angel", "Upper Back/Shoulders", "Beginner", "Bodyweight", "https://www.youtube.com/watch?v=vV7Y_9ZpWRE"),
                Exercise("ex_band_pull_apart", "Band Pull-Apart", "Upper Back", "Beginner", "Resistance Band", "https://www.youtube.com/watch?v=N6C6V_30bDo"),
                Exercise("ex_db_rdl", "Dumbbell Romanian Deadlift", "Hamstrings/Glutes/Lower Back", "Beginner", "Dumbbells", "https://www.youtube.com/watch?v=JCXUYtwSS0U"),
                Exercise("ex_glute_bridge", "Glute Bridge", "Glutes/Core", "Beginner", "Bodyweight", "https://www.youtube.com/watch?v=wPM8co4526Q"),
                Exercise("ex_bird_dog", "Bird Dog", "Core/Lower Back", "Beginner", "Bodyweight", "https://www.youtube.com/watch?v=wiF9KLa1a_o"),
                Exercise("ex_db_row", "Dumbbell Row", "Lats/Upper Back", "Beginner", "Dumbbells", "https://www.youtube.com/watch?v=roCP6697048"),
                Exercise("ex_lat_pulldown", "Lat Pulldown", "Lats/Back", "Beginner", "Cable Machine", "https://www.youtube.com/watch?v=CAwf7n6Luuc"),
                Exercise("ex_goblet_squat", "Goblet Squat", "Quads/Glutes", "Beginner", "Dumbbell", "https://www.youtube.com/watch?v=MeIiGibTCIk"),
                Exercise("ex_pushup", "Incline/Floor Push-up", "Chest/Triceps", "Beginner", "Bodyweight", "https://www.youtube.com/watch?v=JyCG_5l3yec"),
                Exercise("ex_db_shoulder_press", "Seated Dumbbell Shoulder Press", "Shoulders", "Beginner", "Dumbbells", "https://www.youtube.com/watch?v=qEwKCR5JCog"),
                Exercise("ex_plank", "Forearm Plank", "Core", "Beginner", "Bodyweight", "https://www.youtube.com/watch?v=pvIjsGMCwLY"),
                Exercise("ex_cobra", "Cobra Stretch / Back Extension", "Lower Back/Posture", "Beginner", "Bodyweight", "https://www.youtube.com/watch?v=JDcdhTuycOI"),
                Exercise("ex_bb_squat", "Barbell Back Squat", "Quads", "Advanced", "Barbell", "https://www.youtube.com/watch?v=Uv_yQ_780Ge"),
                Exercise("ex_db_curl", "Dumbbell Bicep Curl", "Biceps", "Beginner", "Dumbbells", "https://www.youtube.com/watch?v=ykJmrZ5v0Up"),
                Exercise("ex_bench_press", "Barbell Bench Press", "Chest", "Intermediate", "Barbell", "https://www.youtube.com/watch?v=rT7DgCr-3ps")
            )
            defaultExercises.forEach { dao.insertExercise(it) }
        }

        if (dao.getAllConfidenceChallengesDirect().isEmpty()) {
            val defaultChallenges = listOf(
                ConfidenceChallenge(1, "Maintain eye contact", "Maintain solid, friendly eye contact with someone you speak with for at least 3 seconds.", "Beginner"),
                ConfidenceChallenge(2, "Smile at a stranger", "Smile warmly at a stranger or passerby as you walk down the street.", "Beginner"),
                ConfidenceChallenge(3, "Brief service exchange", "Ask a service worker (cashier, barista, server) how their day is going with genuine warmth.", "Beginner"),
                ConfidenceChallenge(4, "Outfit compliment", "Give a polite, genuine compliment to someone about their outfit, accessory, or style.", "Beginner"),
                ConfidenceChallenge(5, "Ask for directions", "Ask someone for directions or the location of a local spot, even if you already know it.", "Beginner"),
                ConfidenceChallenge(6, "Passerby greetings", "Say a cheerful 'good morning' or 'good afternoon' to at least 3 people you pass by.", "Beginner"),
                ConfidenceChallenge(7, "Hold the door", "Hold the door open for someone, make brief eye contact, and say 'after you' with a smile.", "Beginner"),
                ConfidenceChallenge(8, "Barista advice", "Order a drink or food item you've never tried by asking for the staff's personal recommendation.", "Beginner"),
                ConfidenceChallenge(9, "Casual question", "Ask a cashier or receptionist a simple, pleasant question about their day or work shifts.", "Beginner"),
                ConfidenceChallenge(10, "Start one conversation", "Initiate a pleasant, low-stakes conversation with an acquaintance or a friendly stranger.", "Intermediate"),
                ConfidenceChallenge(11, "Old friend call", "Call a friend, classmate, or relative you haven't spoken with in a while to catch up.", "Beginner"),
                ConfidenceChallenge(12, "Colleague compliment", "Give a sincere, specific work-related or character-related compliment to a colleague.", "Beginner"),
                ConfidenceChallenge(13, "Waiting line chat", "Initiate a brief, lighthearted comment or observation to someone waiting in the same line.", "Intermediate"),
                ConfidenceChallenge(14, "Enthusiastic gratitude", "Give a warm, enthusiastic, and highly expressive 'thank you' to someone who helps you.", "Beginner"),
                ConfidenceChallenge(15, "Open-ended inquiry", "Ask an open-ended question to a group member in a casual social or work setting.", "Intermediate"),
                ConfidenceChallenge(16, "Express opinion", "Voice your opinion confidently on a minor group decision (e.g., where to eat, what time to meet).", "Intermediate"),
                ConfidenceChallenge(17, "Polite rejection", "Say 'no' to an unnecessary or inconvenient request politely but without over-explaining.", "Intermediate"),
                ConfidenceChallenge(18, "Sit next to someone new", "Choose to sit next to someone new in a public common, cafeteria, or workshop space.", "Intermediate"),
                ConfidenceChallenge(19, "5-Minute debate", "Keep a conversation going actively for at least 5 continuous minutes with someone.", "Intermediate"),
                ConfidenceChallenge(20, "Share an interest", "Express a unique personal hobby, passion project, or interest of yours during a chat.", "Intermediate"),
                ConfidenceChallenge(21, "Introduce yourself", "Introduce yourself clearly and confidently to a neighbor, coworker, or new member of a club.", "Intermediate"),
                ConfidenceChallenge(22, "Speak up in group", "Raise your hand and speak up, ask a question, or share an insight in a group session of 3+ people.", "Advanced"),
                ConfidenceChallenge(23, "Tell a story/joke", "Share a brief personal story, a funny anecdote, or a lighthearted joke with colleagues.", "Intermediate"),
                ConfidenceChallenge(24, "Provide meeting advice", "Offer helpful advice, feedback, or a clarifying point when asked in a meeting or study session.", "Advanced"),
                ConfidenceChallenge(25, "Front group response", "Answer a question or make a statement confidently in front of a classroom, seminar, or public group.", "Advanced"),
                ConfidenceChallenge(26, "Invite for coffee", "Ask a colleague, friend, or acquaintance out for lunch, a coffee break, or a casual walk.", "Advanced"),
                ConfidenceChallenge(27, "Ask customer request", "Ask a retail store for a discount, custom option, or a special request with confidence.", "Advanced"),
                ConfidenceChallenge(28, "Public phone call", "Make a professional or energetic phone call while walking in a moderately public but quiet area.", "Advanced"),
                ConfidenceChallenge(29, "Lead brief session", "Lead a minor team discussion, coordinate a study group agenda, or run a quick meeting.", "Advanced"),
                ConfidenceChallenge(30, "Present an idea", "Confidently present an idea, project, pitch, or detailed recommendation in a major meeting or group setting.", "Advanced")
            )
            defaultChallenges.forEach { dao.insertConfidenceChallenge(it) }
        }

        // Pre-populate if empty
        val currentProfile = dao.getUserProfileDirect()
        if (currentProfile == null) {
            dao.insertUserProfile(
                UserProfile(
                    id = 1,
                    name = "Vikas",
                    age = 35,
                    dob = "1991-03-15",
                    birthTime = "14:30",
                    birthPlace = "Delhi, India",
                    weight = 78.4f,
                    height = 178f,
                    neck = 38f,
                    waist = 90f,
                    activityLevel = "Moderate",
                    alcoholFrequency = "Weekly",
                    smokingFrequency = "Daily",
                    sleepHours = 7f,
                    goals = "Gain Muscle,Quit Smoking,Quit Alcohol",
                    level = 3,
                    xp = 650,
                    isOnboarded = true,
                    addictions = "Alcohol,Smoking,Porn,Gambling",
                    zodiacTheme = "Saturn in 6th house (Health & Discipline) aligns with the Pisces-Virgo Lunar Axis. The cosmos signals a powerful phase for cellular and neural purification.",
                    recallScore = 80,
                    readingStreak = 3,
                    speakingPracticeMinutes = 15
                )
            )

            // Dynamic timestamp offsets (ms)
            val currentTime = System.currentTimeMillis()
            val singleDayMs = 24L * 60L * 60L * 1000L

            dao.insertAddictionClock(AddictionClock("Alcohol", currentTime - (5 * singleDayMs)))
            dao.insertAddictionClock(AddictionClock("Smoking", currentTime - (2 * singleDayMs)))
            dao.insertAddictionClock(AddictionClock("Porn", currentTime - (12 * singleDayMs)))
            dao.insertAddictionClock(AddictionClock("Gambling", currentTime - (45 * singleDayMs)))

            // Populate mock check-ins
            dao.insertCheckIn(DailyCheckIn("2026-06-16", mood = 3, energy = 6, sleepQuality = 5, cravings = 3))
            dao.insertCheckIn(DailyCheckIn("2026-06-15", mood = 2, energy = 4, sleepQuality = 4, cravings = 6))
            dao.insertCheckIn(DailyCheckIn("2026-06-14", mood = 4, energy = 8, sleepQuality = 7, cravings = 1))

            // Add standard protocol-aligned habits
            dao.insertHabit(Habit(name = "Warm Water + Jeera/Ajwain", isBadHabit = false, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "Take NAC & Lion's Mane", isBadHabit = false, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "Wall Angels & Band Pull-Aparts", isBadHabit = false, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "Gym Weight Training (45 min)", isBadHabit = false, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "Biozyme Whey Protein", isBadHabit = false, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "Egg Breakfast + B-Complex", isBadHabit = false, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "No Smoking (Nicotine abstinence)", isBadHabit = true, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "No Alcohol (Cold Turkey)", isBadHabit = true, lastCompletedDate = "", streak = 1))
            dao.insertHabit(Habit(name = "Warm Turmeric Milk Sleep Prep", isBadHabit = false, lastCompletedDate = "", streak = 1))

            // Initial coach messages
            dao.insertCoachMessage(
                CoachMessage(
                    role = "model",
                    message = "Good morning Vikas! I am your AI Coach. I have loaded your complete **Total Transformation Protocol (35M RESET)**. You are quitting alcohol and smoking **cold turkey** after 10 years. Today's goal is strictly focused on discipline: stick to the morning schedule, complete your weight training session, and follow your supplement stack. How are you feeling today?",
                    timestamp = currentTime - 50000
                )
            )
        }
    }
}

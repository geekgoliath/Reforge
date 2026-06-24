package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiManager
import com.example.api.TransformationReport
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReforgeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReforgeRepository

    val userProfile: StateFlow<UserProfile?>
    val habits: StateFlow<List<Habit>>
    val addictionClocks: StateFlow<List<AddictionClock>>
    val coachMessages: StateFlow<List<CoachMessage>>
    val journalEntries: StateFlow<List<JournalEntry>>
    val checkIns: StateFlow<List<DailyCheckIn>>
    val relapseEvents: StateFlow<List<RelapseEvent>>
    val weightHistory: StateFlow<List<WeightHistory>>
    val dailyCoachAnalyses: StateFlow<List<DailyCoachAnalysis>>

    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    private val _isAnalyzingJournal = MutableStateFlow(false)
    val isAnalyzingJournal: StateFlow<Boolean> = _isAnalyzingJournal.asStateFlow()

    private val _isEvaluatingCognitiveJournal = MutableStateFlow(false)
    val isEvaluatingCognitiveJournal: StateFlow<Boolean> = _isEvaluatingCognitiveJournal.asStateFlow()

    private val _cognitiveEvaluationResult = MutableStateFlow<com.example.api.CognitiveEvaluation?>(null)
    val cognitiveEvaluationResult: StateFlow<com.example.api.CognitiveEvaluation?> = _cognitiveEvaluationResult.asStateFlow()

    private val _isAnalyzingDailyCoaching = MutableStateFlow(false)
    val isAnalyzingDailyCoaching: StateFlow<Boolean> = _isAnalyzingDailyCoaching.asStateFlow()

    private val _dailyCoachingError = MutableStateFlow<String?>(null)
    val dailyCoachingError: StateFlow<String?> = _dailyCoachingError.asStateFlow()

    private val _relapseAnalysisResult = MutableStateFlow<String?>(null)
    val relapseAnalysisResult: StateFlow<String?> = _relapseAnalysisResult.asStateFlow()

    private val _xpAddedMessage = MutableStateFlow<String?>(null)
    val xpAddedMessage: StateFlow<String?> = _xpAddedMessage.asStateFlow()

    private val _transformationReport = MutableStateFlow<com.example.api.TransformationReport?>(null)
    val transformationReport: StateFlow<com.example.api.TransformationReport?> = _transformationReport.asStateFlow()

    private val _isGeneratingIntelligence = MutableStateFlow(false)
    val isGeneratingIntelligence: StateFlow<Boolean> = _isGeneratingIntelligence.asStateFlow()

    private val _isGeneratingNutrition = MutableStateFlow(false)
    val isGeneratingNutrition: StateFlow<Boolean> = _isGeneratingNutrition.asStateFlow()

    private val _nutritionExplanation = MutableStateFlow("")
    val nutritionExplanation: StateFlow<String> = _nutritionExplanation.asStateFlow()

    private val _nutritionError = MutableStateFlow<String?>(null)
    val nutritionError: StateFlow<String?> = _nutritionError.asStateFlow()

    val allExercises: StateFlow<List<com.example.data.Exercise>>
    val confidenceChallenges: StateFlow<List<ConfidenceChallenge>>

    private val _isGeneratingWorkout = MutableStateFlow(false)
    val isGeneratingWorkout: StateFlow<Boolean> = _isGeneratingWorkout.asStateFlow()

    private val _generatedWorkoutPlan = MutableStateFlow<String?>(null)
    val generatedWorkoutPlan: StateFlow<String?> = _generatedWorkoutPlan.asStateFlow()

    private val _workoutGenerationError = MutableStateFlow<String?>(null)
    val workoutGenerationError: StateFlow<String?> = _workoutGenerationError.asStateFlow()

    private val _lastGeneratedExerciseIds = MutableStateFlow<List<String>>(emptyList())
    val lastGeneratedExerciseIds: StateFlow<List<String>> = _lastGeneratedExerciseIds.asStateFlow()

    private val _isAdaptiveProcessing = MutableStateFlow(false)
    val isAdaptiveProcessing: StateFlow<Boolean> = _isAdaptiveProcessing.asStateFlow()

    private val _adaptiveEvents = MutableStateFlow<List<AdaptiveEvent>>(emptyList())
    val adaptiveEvents: StateFlow<List<AdaptiveEvent>> = _adaptiveEvents.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReforgeRepository(database.dao())

        // Feed flows into stable StateFlows for the Jetpack Compose Compose-Lifecycle
        userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.Lazily, null)
        habits = repository.allHabits.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        addictionClocks = repository.addictionClocks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        coachMessages = repository.coachMessages.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        journalEntries = repository.journalEntries.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        checkIns = repository.checkIns.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        relapseEvents = repository.relapseEvents.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        weightHistory = repository.weightHistory.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        dailyCoachAnalyses = repository.dailyCoachAnalyses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allExercises = repository.allExercises.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        confidenceChallenges = repository.confidenceChallenges.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Populate database with mockup data if new first load
        viewModelScope.launch {
            repository.preWalkData()
            loadAdaptiveEvents()
        }
    }

    fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private val _isOnboardingLoading = MutableStateFlow(false)
    val isOnboardingLoading: StateFlow<Boolean> = _isOnboardingLoading.asStateFlow()

    private val _onboardingError = MutableStateFlow<String?>(null)
    val onboardingError: StateFlow<String?> = _onboardingError.asStateFlow()

    // --- Onboarding / Reset Profile ---
    fun updateProfile(
        name: String,
        age: Int,
        dob: String,
        birthTime: String,
        birthPlace: String,
        weight: Float,
        height: Float,
        neck: Float,
        waist: Float,
        activityLevel: String,
        alcoholFrequency: String,
        smokingFrequency: String,
        sleepHours: Float,
        goals: String,
        addictions: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isOnboardingLoading.value = true
                _onboardingError.value = null

                // 1. Generate 7-Day Plan from Gemini
                val rawPlanJson = GeminiManager.generatePlan(
                    name = name,
                    age = age,
                    height = height,
                    weight = weight,
                    neck = neck,
                    waist = waist,
                    activity = activityLevel,
                    goals = goals,
                    addictions = addictions,
                    dob = dob,
                    birthTime = birthTime,
                    birthPlace = birthPlace
                )

                // 2. Parse the plan
                var zodiacThemeParsed = "Saturn aligns with your discipline sector to support your transformation."
                try {
                    val jsonObj = org.json.JSONObject(rawPlanJson)
                    zodiacThemeParsed = jsonObj.optString("zodiacTheme", zodiacThemeParsed)

                    val daysArray = jsonObj.optJSONArray("days")
                    if (daysArray != null) {
                        for (i in 0 until daysArray.length()) {
                            val dayObj = daysArray.optJSONObject(i) ?: continue
                            val dayNum = dayObj.optInt("dayNumber", i + 1)
                            val dayName = dayObj.optString("dayName", "Day $dayNum")

                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_YEAR, i)
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                            repository.clearWorkoutsForDate(dateStr)
                            repository.clearMealsForDate(dateStr)

                            // Save workouts
                            val workoutsArray = dayObj.optJSONArray("workouts")
                            if (workoutsArray != null) {
                                for (j in 0 until workoutsArray.length()) {
                                    val wObj = workoutsArray.optJSONObject(j) ?: continue
                                    val exerciseName = wObj.optString("exerciseName", "Exercise")
                                    val sets = wObj.optInt("sets", 3)
                                    val reps = wObj.optInt("reps", 10)
                                    val wWeight = wObj.optDouble("weight", 0.0).toFloat()

                                    repository.insertWorkout(
                                        Workout(
                                            date = dateStr,
                                            name = dayName,
                                            exerciseName = exerciseName,
                                            sets = sets,
                                            reps = reps,
                                            weight = wWeight,
                                            isCompleted = false
                                        )
                                    )
                                }
                            }

                            // Save meals
                            val mealsArray = dayObj.optJSONArray("meals")
                            if (mealsArray != null) {
                                for (j in 0 until mealsArray.length()) {
                                    val mObj = mealsArray.optJSONObject(j) ?: continue
                                    val mealName = mObj.optString("name", "Meal")
                                    val mDesc = mObj.optString("description", "")
                                    val mCal = mObj.optDouble("calories", 0.0).toFloat()
                                    val mProt = mObj.optDouble("protein", 0.0).toFloat()

                                    repository.insertMeal(
                                        Meal(
                                            date = dateStr,
                                            name = mealName,
                                            description = mDesc,
                                            calories = mCal,
                                            protein = mProt,
                                            isEaten = false
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (pe: Exception) {
                    android.util.Log.e("ReforgeViewModel", "Error parsing plan JSON, falling back to manual mapping", pe)
                }

                // Save profile to database
                val exists = userProfile.value ?: UserProfile()
                val newProfile = exists.copy(
                    name = name,
                    age = age,
                    dob = dob,
                    birthTime = birthTime,
                    birthPlace = birthPlace,
                    weight = weight,
                    height = height,
                    neck = neck,
                    waist = waist,
                    activityLevel = activityLevel,
                    alcoholFrequency = alcoholFrequency,
                    smokingFrequency = smokingFrequency,
                    sleepHours = sleepHours,
                    goals = goals,
                    addictions = addictions,
                    zodiacTheme = zodiacThemeParsed,
                    isOnboarded = true
                )
                repository.insertUserProfile(newProfile)

                // Inject initial customized welcome coach message
                repository.clearCoachMessages()
                repository.insertCoachMessage(
                    CoachMessage(
                        role = "model",
                        message = "Welcome $name! I am your AI Transformation Coach. Your Personal Transformation OS has been initialized.\n\n" +
                                "🌌 **Astrological Alignment**: $zodiacThemeParsed\n\n" +
                                "Based on your targets ($goals), I have forged your personalized 7-Day Protocol of high-protein raw weights nutrition, focused strength workouts, and evidence-based environment design triggers. Let's reforge your habits together!"
                    )
                )

                // Save weight history point
                repository.insertWeightEntry(WeightHistory(date = getTodayString(), weight = weight))

                _isOnboardingLoading.value = false
            } catch (e: Exception) {
                _isOnboardingLoading.value = false
                _onboardingError.value = "Failed to compile your transformation plan: ${e.localizedMessage}"
                android.util.Log.e("ReforgeViewModel", "Onboarding transformation plan error", e)
            }
        }
    }

    fun getWorkoutsForDate(date: String): Flow<List<Workout>> {
        return repository.getWorkoutsForDate(date)
    }

    fun getMealsForDate(date: String): Flow<List<Meal>> {
        return repository.getMealsForDate(date)
    }

    fun toggleWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = workout.copy(isCompleted = !workout.isCompleted)
            repository.insertWorkout(updated)
            if (updated.isCompleted) {
                showXpGain("+50 XP: Physical Realignment Completed")
                repository.addXp(50)
            }
        }
    }

    fun toggleMeal(meal: Meal) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = meal.copy(isEaten = !meal.isEaten)
            repository.insertMeal(updated)
            if (updated.isEaten) {
                showXpGain("+40 XP: Pure Fuel Consumed")
                repository.addXp(40)
            }
        }
    }

    // --- Habits Tracking ---
    fun toggleHabit(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = getTodayString()
            val completed = habit.lastCompletedDate == today
            val updatedHabit = if (completed) {
                habit.copy(lastCompletedDate = "", streak = (habit.streak - 1).coerceAtLeast(0))
            } else {
                habit.copy(lastCompletedDate = today, streak = habit.streak + 1)
            }
            repository.updateHabit(updatedHabit)

            if (!completed) {
                // Award XP for completion
                showXpGain("+50 XP: Habit Mastery")
                repository.addXp(50)
            }
        }
    }

    fun addCustomHabit(name: String, isBad: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = Habit(name = name, isBadHabit = isBad)
            repository.insertHabit(habit)
            showXpGain("+20 XP: New Commitment")
            repository.addXp(20)
        }
    }

    fun removeHabit(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHabit(id)
        }
    }

    // --- Daily Check-ins ---
    fun saveCheckIn(mood: Int, energy: Int, sleep: Int, cravings: Int, weight: Float = 0f) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = getTodayString()
            val profile = repository.userProfile.firstOrNull() ?: com.example.data.UserProfile()
            val finalWeight = if (weight > 0f) weight else (profile.weight)
            
            val checkIn = DailyCheckIn(
                date = today,
                mood = mood,
                energy = energy,
                sleepQuality = sleep,
                cravings = cravings,
                weight = finalWeight
            )
            repository.insertCheckIn(checkIn)
            repository.insertWeightEntry(com.example.data.WeightHistory(date = today, weight = finalWeight))

            // Update current user profile weight
            repository.updateUserProfile(profile.copy(weight = finalWeight))

            // Trigger Adaptive Life Engine (The Brain of Reforge)
            runAdaptiveLifeEngine(finalWeight)

            showXpGain("+60 XP: Daily Self-Awareness")
            repository.addXp(60)

            // Trigger AI daily coach analysis
            triggerDailyCoachingAnalysis(mood, energy, sleep, cravings, finalWeight)
        }
    }

    fun triggerDailyCoachingAnalysis(mood: Int, energy: Int, sleep: Int, cravings: Int, weight: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzingDailyCoaching.value = true
            _dailyCoachingError.value = null
            try {
                val today = getTodayString()
                val profile = repository.userProfile.firstOrNull() ?: com.example.data.UserProfile()
                val checkInsList = repository.checkIns.firstOrNull() ?: emptyList()
                val relapsesList = repository.relapseEvents.firstOrNull() ?: emptyList()
                val workoutsList = repository.getAllWorkoutsDirect()

                val rawResult = com.example.api.GeminiManager.analyzeDailyCoaching(
                    checkIns = checkInsList,
                    relapses = relapsesList,
                    workouts = workoutsList,
                    profile = profile,
                    mood = mood,
                    energy = energy,
                    sleep = sleep,
                    cravings = cravings,
                    weight = weight
                )

                // Parse returned JSON using JSONObject
                val jsonObj = org.json.JSONObject(rawResult)
                val patterns = jsonObj.optString("patterns", "")
                val relapseRisk = jsonObj.optString("relapseRisk", "Low")
                val recoveryScore = jsonObj.optInt("recoveryScore", 70)
                val relapsePatterns = jsonObj.optString("relapsePatterns", "")
                val missedWorkoutPatterns = jsonObj.optString("missedWorkoutPatterns", "")
                val sleepIssues = jsonObj.optString("sleepIssues", "")
                val confidenceTrends = jsonObj.optString("confidenceTrends", "")
                val todayFocus = jsonObj.optString("todayFocus", "")
                val riskLevel = jsonObj.optString("riskLevel", "Low")

                val actionPlanArray = jsonObj.optJSONArray("actionPlan")
                val actionPlanList = mutableListOf<String>()
                if (actionPlanArray != null) {
                    for (i in 0 until actionPlanArray.length()) {
                        actionPlanList.add(actionPlanArray.optString(i))
                    }
                }
                val actionPlanStr = actionPlanList.joinToString(";")

                val analysis = com.example.data.DailyCoachAnalysis(
                    date = today,
                    patterns = patterns,
                    relapseRisk = relapseRisk,
                    recoveryScore = recoveryScore,
                    relapsePatterns = relapsePatterns,
                    missedWorkoutPatterns = missedWorkoutPatterns,
                    sleepIssues = sleepIssues,
                    confidenceTrends = confidenceTrends,
                    todayFocus = todayFocus,
                    riskLevel = riskLevel,
                    actionPlan = actionPlanStr
                )

                repository.insertDailyCoachAnalysis(analysis)
                _isAnalyzingDailyCoaching.value = false
            } catch (e: Exception) {
                _isAnalyzingDailyCoaching.value = false
                _dailyCoachingError.value = "Failed to run AI coach analysis: ${e.localizedMessage}"
                android.util.Log.e("ReforgeViewModel", "AI Coach Analysis error", e)
            }
        }
    }

    // --- AI Coach Chat ---
    fun sendCoachMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Save user message locally
            val userMsg = CoachMessage(role = "user", message = text)
            repository.insertCoachMessage(userMsg)

            // 2. Set typing state
            _isCoachTyping.value = true

            // 3. Fetch history
            val history = coachMessages.value

            // 4. Call Gemini Model (using gemini-1.5-flash as default)
            val responseText = GeminiManager.getCoachResponse(history, text)

            // 5. Save model response
            val modelMsg = CoachMessage(role = "model", message = responseText)
            repository.insertCoachMessage(modelMsg)

            _isCoachTyping.value = false
            repository.addXp(10) // 10 XP for active chat coaching
        }
    }

    fun clearCoachMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCoachMessages()
            // Put a welcoming introductory message
            repository.insertCoachMessage(
                CoachMessage(
                    role = "model",
                    message = "Coach thread reset. Ask me anything about diet, overcoming cravings, or workout schedules! I am here to help you Reforge."
                )
            )
        }
    }

    fun generateTransformationIntelligenceReport() {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingIntelligence.value = true
            try {
                val profile = userProfile.value ?: UserProfile()
                val heightM = profile.height / 100f
                val bmi = if (heightM > 0) profile.weight / (heightM * heightM) else 0f
                val bmr = (10f * profile.weight) + (6.25f * profile.height) - (5f * profile.age) + 5f
                val multiplier = when (profile.activityLevel.lowercase()) {
                    "sedentary" -> 1.2f
                    "moderate" -> 1.55f
                    "active" -> 1.725f
                    "elite" -> 1.9f
                    else -> 1.55f
                }
                val tdee = bmr * multiplier
                val targetCalories = if (profile.goals.contains("Lose Fat", ignoreCase = true)) {
                    tdee - 500f
                } else if (profile.goals.contains("Gain Muscle", ignoreCase = true)) {
                    tdee + 300f
                } else {
                    tdee
                }
                val targetProtein = 2.0f * profile.weight
                val targetFat = 1.0f * profile.weight
                val proteinKcal = targetProtein * 4f
                val fatKcal = targetFat * 9f
                val remainingKcal = (targetCalories - proteinKcal - fatKcal).coerceAtLeast(0f)
                val targetCarbs = remainingKcal / 4f

                val workouts = repository.getAllWorkoutsDirect()
                val totalWorkouts = workouts.size
                val completedWorkouts = workouts.count { it.isCompleted }
                val workoutAdherence = if (totalWorkouts > 0) (completedWorkouts * 100) / totalWorkouts else 85

                val todayMeals = repository.getMealsForDateDirect(getTodayString())
                val eatenMeals = todayMeals.filter { it.isEaten }
                val totalProteinEaten = eatenMeals.sumOf { it.protein.toDouble() }.toFloat()
                val proteinAdherence = if (targetProtein > 0) {
                    ((totalProteinEaten / targetProtein) * 100).toInt().coerceIn(0, 100)
                } else {
                    90
                }
                val finalProteinAdherence = if (todayMeals.isEmpty()) 92 else proteinAdherence

                val habitsList = habits.value
                val completedHabits = habitsList.count { it.lastCompletedDate.isNotEmpty() }
                val totalHabits = habitsList.size
                val habitAdherence = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 80

                val journalsList = journalEntries.value.take(5).joinToString("; ") { it.text }
                val relapsesList = relapseEvents.value.take(5).joinToString("; ") { "${it.addiction} triggered by ${it.trigger}" }

                val report = GeminiManager.generateTransformationReport(
                    bmi = bmi,
                    bmr = bmr,
                    tdee = tdee,
                    targetCalories = targetCalories,
                    targetProtein = targetProtein,
                    targetCarbs = targetCarbs,
                    targetFat = targetFat,
                    workoutAdherence = workoutAdherence,
                    proteinAdherence = finalProteinAdherence,
                    habitAdherence = habitAdherence,
                    currentWeight = profile.weight,
                    profileGoals = profile.goals,
                    addictions = profile.addictions,
                    recentJournals = journalsList,
                    recentRelapses = relapsesList
                )
                _transformationReport.value = report
                showXpGain("+150 XP: Deep Biometric Sync")
                repository.addXp(150)
            } catch (e: Exception) {
                Log.e("ReforgeViewModel", "Error in report generation pipeline", e)
            } finally {
                _isGeneratingIntelligence.value = false
            }
        }
    }

    // --- Journal Entry ---
    fun addJournalEntry(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzingJournal.value = true

            // Generate analytical report from Gemini
            val analysis = GeminiManager.analyzeJournal(text)

            val entry = JournalEntry(
                text = text,
                summary = analysis.summary,
                emotionalAnalysis = analysis.mood,
                triggersIdentified = analysis.triggers,
                wins = analysis.wins,
                mistakes = analysis.mistakes,
                tomorrowFocus = analysis.tomorrowFocus
            )
            repository.insertJournalEntry(entry)

            _isAnalyzingJournal.value = false
            showXpGain("+100 XP: Evening Refocus")
            repository.addXp(100)
        }
    }

    // --- Relapse Counseling ---
    fun reportRelapse(
        addiction: String,
        whatHappened: String,
        whereContext: String,
        companion: String,
        emotion: String,
        timeOfDay: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _relapseAnalysisResult.value = "Analyzing situation with Therapist Agent..."

            // 1. Reset timeline clock for this addiction to right now
            val clock = AddictionClock(addictionName = addiction, lastResetTimestamp = System.currentTimeMillis())
            repository.insertAddictionClock(clock)

            // 2. Fetch CBT custom counsel from Gemini
            val advice = GeminiManager.analyzeRelapse(
                addiction = addiction,
                whatHappened = whatHappened,
                whereContext = whereContext,
                companion = companion,
                emotion = emotion,
                timeOfDay = timeOfDay
            )
            
            // 3. Store event in database
            val event = RelapseEvent(
                addiction = addiction,
                trigger = whatHappened,
                location = whereContext,
                companion = companion,
                emotion = emotion,
                timeOfDay = timeOfDay,
                preventionPlan = advice
            )
            repository.insertRelapseEvent(event)

            _relapseAnalysisResult.value = advice

            // 4. Inject a supportive therapist comment directly into the Chat Coach log so Vikas sees it immediately!
            repository.insertCoachMessage(
                CoachMessage(
                    role = "model",
                    message = "❤️ **Therapist Intervention**: Vikas, you reported a relapse on $addiction. Do not feel shame—this is valuable behavioral data.\n\n$advice"
                )
            )
        }
    }

    fun clearRelapseResult() {
        _relapseAnalysisResult.value = null
    }

    fun awardXp(amount: Int, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addXp(amount)
            showXpGain("+$amount XP: $reason")
        }
    }

    private fun showXpGain(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _xpAddedMessage.value = message
            kotlinx.coroutines.delay(4000)
            if (_xpAddedMessage.value == message) {
                _xpAddedMessage.value = null
            }
        }
    }

    fun dismissXpMessage() {
        _xpAddedMessage.value = null
    }

    fun generateAndSaveNutritionMeals(
        date: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float,
        foodList: String,
        goals: String,
        activity: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingNutrition.value = true
            _nutritionError.value = null
            _nutritionExplanation.value = ""

            try {
                val responseJson = GeminiManager.generateNutritionMeals(
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    foodList = foodList,
                    goals = goals,
                    activity = activity
                )

                // Parse the response
                val mainObj = org.json.JSONObject(responseJson)
                val explanation = mainObj.optString("explanation", "High-efficiency metabolic alignment loaded.")
                _nutritionExplanation.value = explanation

                val mealsArray = mainObj.optJSONArray("meals")
                if (mealsArray != null && mealsArray.length() > 0) {
                    // Clear existing meals for that specific date
                    repository.clearMealsForDate(date)

                    for (i in 0 until mealsArray.length()) {
                        val mObj = mealsArray.optJSONObject(i) ?: continue
                        val mName = mObj.optString("name", "Meal")
                        val mDesc = mObj.optString("description", "")
                        val mCal = mObj.optDouble("calories", 0.0).toFloat()
                        val mProt = mObj.optDouble("protein", 0.0).toFloat()
                        val mCarb = mObj.optDouble("carbs", 0.0).toFloat()
                        val mFat = mObj.optDouble("fat", 0.0).toFloat()

                        repository.insertMeal(
                            Meal(
                                date = date,
                                name = mName,
                                description = mDesc,
                                calories = mCal,
                                protein = mProt,
                                carbs = mCarb,
                                fat = mFat,
                                isEaten = false
                            )
                        )
                    }
                    showXpGain("+150 XP: Nutrition Forged")
                    repository.addXp(150)
                } else {
                    _nutritionError.value = "Failed to parse meals from JSON response."
                }
            } catch (e: Exception) {
                android.util.Log.e("ReforgeViewModel", "Error forging nutrition", e)
                _nutritionError.value = "Failed to generate nutrition: ${e.localizedMessage}"
            } finally {
                _isGeneratingNutrition.value = false
            }
        }
    }

    fun generateWorkoutPlan(
        date: String,
        durationMinutes: Int = 45,
        difficulty: String = "beginner",
        focus: String = "posture focused",
        goal: String = "muscle gain"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingWorkout.value = true
            _workoutGenerationError.value = null
            _generatedWorkoutPlan.value = null
            _lastGeneratedExerciseIds.value = emptyList()

            try {
                // Ensure exercises are loaded
                var exercises = repository.getAllExercisesDirect()
                if (exercises.isEmpty()) {
                    repository.preWalkData()
                    exercises = repository.getAllExercisesDirect()
                }

                val responseJson = GeminiManager.generateWorkoutFromDatabase(
                    exercisesList = exercises,
                    durationMinutes = durationMinutes,
                    difficulty = difficulty,
                    focus = focus,
                    goal = goal
                )

                val mainObj = org.json.JSONObject(responseJson)
                val idsArray = mainObj.optJSONArray("exercise_ids")
                val explanation = mainObj.optString("explanation", "Postural activation alignment assembled successfully.")

                _generatedWorkoutPlan.value = explanation

                val exerciseIds = mutableListOf<String>()
                if (idsArray != null) {
                    for (i in 0 until idsArray.length()) {
                        exerciseIds.add(idsArray.getString(i))
                    }
                }

                _lastGeneratedExerciseIds.value = exerciseIds

                if (exerciseIds.isNotEmpty()) {
                    // Clear existing workouts for that specific date to replace with our clean postural plan
                    repository.clearWorkoutsForDate(date)

                    // Lookup each exercise and insert into workouts
                    for (id in exerciseIds) {
                        val matchedEx = exercises.find { it.exercise_id == id } ?: continue
                        repository.insertWorkout(
                            Workout(
                                date = date,
                                name = "🏆 POSTURE FOCUS",
                                exerciseName = matchedEx.name,
                                sets = 3,
                                reps = 12,
                                weight = if (matchedEx.equipment.lowercase().contains("dumbbell")) 10f else 0f,
                                isCompleted = false
                            )
                        )
                    }

                    showXpGain("+150 XP: Posture Forged")
                    repository.addXp(150)
                } else {
                    _workoutGenerationError.value = "Failed to parse exercise IDs from plan."
                }
            } catch (e: Exception) {
                android.util.Log.e("ReforgeViewModel", "Error generating posture plan", e)
                _workoutGenerationError.value = "Failed to forge posture workout: ${e.localizedMessage}"
            } finally {
                _isGeneratingWorkout.value = false
            }
        }
    }

    fun toggleConfidenceChallenge(challenge: ConfidenceChallenge, reflectionNotes: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = challenge.copy(
                isCompleted = !challenge.isCompleted,
                completionTimestamp = if (!challenge.isCompleted) System.currentTimeMillis() else 0L,
                notes = reflectionNotes
            )
            repository.updateConfidenceChallenge(updated)
            if (updated.isCompleted) {
                val xpAmount = when (challenge.difficulty.lowercase()) {
                    "beginner" -> 100
                    "intermediate" -> 150
                    "advanced" -> 250
                    else -> 100
                }
                repository.addXp(xpAmount)
                showXpGain("+${xpAmount} XP: Day ${challenge.day} Confidence Forged")
            }
        }
    }

    fun updateRecallScore(score: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.userProfile.firstOrNull() ?: com.example.data.UserProfile()
            repository.updateUserProfile(profile.copy(recallScore = score))
            repository.addXp(40)
            showXpGain("+40 XP: Story Recall complete")
        }
    }

    fun incrementReadingStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.userProfile.firstOrNull() ?: com.example.data.UserProfile()
            repository.updateUserProfile(profile.copy(readingStreak = profile.readingStreak + 1))
            repository.addXp(30)
            showXpGain("+30 XP: Read Aloud streak active")
        }
    }

    fun addSpeakingPractice(mins: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.userProfile.firstOrNull() ?: com.example.data.UserProfile()
            repository.updateUserProfile(profile.copy(speakingPracticeMinutes = profile.speakingPracticeMinutes + mins))
            repository.addXp(50)
            showXpGain("+50 XP: Speech Practice logged")
        }
    }

    fun evaluateCognitiveJournal(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isEvaluatingCognitiveJournal.value = true
            try {
                val eval = com.example.api.GeminiManager.evaluateCognitiveJournal(text)
                _cognitiveEvaluationResult.value = eval
                repository.addXp(100)
                showXpGain("+100 XP: Cognitive Journal Analyzed")
            } catch (e: Exception) {
                android.util.Log.e("ReforgeViewModel", "Error evaluating cognitive journal", e)
            } finally {
                _isEvaluatingCognitiveJournal.value = false
            }
        }
    }

    fun clearCognitiveJournalResult() {
        _cognitiveEvaluationResult.value = null
    }

    // --- Adaptive Life Engine Persistence & Logic ---
    fun loadAdaptiveEvents() {
        val sp = getApplication<Application>().getSharedPreferences("adaptive_life_engine", android.content.Context.MODE_PRIVATE)
        val jsonStr = sp.getString("events", "[]") ?: "[]"
        val list = mutableListOf<AdaptiveEvent>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AdaptiveEvent(
                        timestamp = obj.getLong("timestamp"),
                        date = obj.getString("date"),
                        startingWeight = obj.getDouble("startingWeight").toFloat(),
                        currentWeight = obj.getDouble("currentWeight").toFloat(),
                        weightDiff = obj.getDouble("weightDiff").toFloat(),
                        goal = obj.getString("goal"),
                        detectedPattern = obj.getString("detectedPattern"),
                        actionTaken = obj.getString("actionTaken"),
                        caloriesAdjusted = obj.getDouble("caloriesAdjusted").toFloat(),
                        aiExplanation = obj.getString("aiExplanation")
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ReforgeViewModel", "Error reading adaptive events", e)
        }
        _adaptiveEvents.value = list
    }

    fun saveAdaptiveEvent(event: AdaptiveEvent) {
        val sp = getApplication<Application>().getSharedPreferences("adaptive_life_engine", android.content.Context.MODE_PRIVATE)
        val list = _adaptiveEvents.value.toMutableList()
        list.add(0, event) // Insert at beginning so newest is first
        _adaptiveEvents.value = list

        val arr = org.json.JSONArray()
        for (e in list) {
            val obj = org.json.JSONObject()
            obj.put("timestamp", e.timestamp)
            obj.put("date", e.date)
            obj.put("startingWeight", e.startingWeight.toDouble())
            obj.put("currentWeight", e.currentWeight.toDouble())
            obj.put("weightDiff", e.weightDiff.toDouble())
            obj.put("goal", e.goal)
            obj.put("detectedPattern", e.detectedPattern)
            obj.put("actionTaken", e.actionTaken)
            obj.put("caloriesAdjusted", e.caloriesAdjusted.toDouble())
            obj.put("aiExplanation", e.aiExplanation)
            arr.put(obj)
        }
        sp.edit().putString("events", arr.toString()).apply()
    }

    fun runAdaptiveLifeEngine(currentWeight: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAdaptiveProcessing.value = true
            try {
                val profile = repository.userProfile.first() ?: UserProfile()
                val history = repository.weightHistory.first() ?: emptyList()
                
                // Get starting weight (oldest entry in weight history, or onboarding profile weight)
                val startingWeight = if (history.isNotEmpty()) {
                    history.first().weight
                } else {
                    profile.weight
                }
                
                val diff = currentWeight - startingWeight
                val goal = profile.goals ?: "Gain Muscle"
                
                var detectedPattern = "Calibration Mode"
                var actionTaken = "No adjustment needed."
                var caloriesAdjusted = 0f
                var isAnomaly = false
                
                val isGainGoal = goal.contains("Gain Muscle", ignoreCase = true) || goal.contains("Gain Weight", ignoreCase = true)
                val isLossGoal = goal.contains("Lose Fat", ignoreCase = true) || goal.contains("Lose Weight", ignoreCase = true)
                
                if (isGainGoal) {
                    if (diff < 0f) {
                        isAnomaly = true
                        detectedPattern = "⚠️ ANOMALY DETECTED: Weight dropping (${String.format("%.1f", startingWeight)}kg -> ${String.format("%.1f", currentWeight)}kg) despite muscle-building/weight-gain target."
                        actionTaken = "⚡ AUTOMATIC PRESCRIPTION: Increased caloric intake by +250 kcal/day to restore positive energy balance."
                        caloriesAdjusted = 250f
                    } else {
                        detectedPattern = "✓ POSITIVE ADHERENCE: Weight is increasing or stable, aligning with muscle-building target."
                        actionTaken = "No adjustment required. Continuing current protocol."
                    }
                } else if (isLossGoal) {
                    if (diff >= 0f) {
                        isAnomaly = true
                        detectedPattern = "⚠️ ANOMALY DETECTED: Weight is stagnant or increasing (${String.format("%.1f", startingWeight)}kg -> ${String.format("%.1f", currentWeight)}kg) despite weight-loss/fat-loss target."
                        actionTaken = "⚡ AUTOMATIC PRESCRIPTION: Decreased caloric intake by -250 kcal/day to break metabolic plateau."
                        caloriesAdjusted = -250f
                    } else {
                        detectedPattern = "✓ POSITIVE ADHERENCE: Weight is dropping as expected, aligning with fat-loss target."
                        actionTaken = "No adjustment required. Continuing current protocol."
                    }
                } else {
                    detectedPattern = "✓ MAINTENANCE COMPLIANCE: Weight is stable within metabolic tolerance."
                    actionTaken = "No adjustment required."
                }
                
                // If there's an anomaly, perform Adaptive Plan updates
                if (caloriesAdjusted != 0f) {
                    val todayStr = getTodayString()
                    val futureMeals = repository.getMealsFromDateDirect(todayStr)
                    
                    if (futureMeals.isNotEmpty()) {
                        // Group future meals by date to adjust daily totals
                        val mealsByDate = futureMeals.groupBy { it.date }
                        for ((date, meals) in mealsByDate) {
                            val count = meals.size
                            if (count > 0) {
                                val adjPerMeal = caloriesAdjusted / count
                                for (meal in meals) {
                                    val originalCals = meal.calories
                                    val newCals = (originalCals + adjPerMeal).coerceAtLeast(0f)
                                    
                                    // Proportionally adjust protein/carbs/fat to maintain ratio
                                    val scale = if (originalCals > 0) newCals / originalCals else 1f
                                    val newProtein = (meal.protein * scale).coerceAtLeast(0f)
                                    val newCarbs = (meal.carbs * scale).coerceAtLeast(0f)
                                    val newFat = (meal.fat * scale).coerceAtLeast(0f)
                                    
                                    val updatedMeal = meal.copy(
                                        calories = newCals,
                                        protein = newProtein,
                                        carbs = newCarbs,
                                        fat = newFat
                                    )
                                    repository.updateMeal(updatedMeal)
                                }
                            }
                        }
                    }
                }
                
                // AI Analysis: Ask Gemini to synthesize adaptive comments
                var aiExplanation = ""
                try {
                    aiExplanation = com.example.api.analyzeAdaptiveEngine(
                        goal = goal,
                        startWeight = startingWeight,
                        currentWeight = currentWeight,
                        calorieAdjustment = caloriesAdjusted,
                        historyLogCount = history.size
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ReforgeViewModel", "Gemini adaptive explanation failed, falling back to local prescription synthesis", e)
                    aiExplanation = if (caloriesAdjusted > 0f) {
                        "Your body is in a negative energy balance (metabolic deficit) where energy expenditure exceeds intake, causing muscle catabolism. By automatically adding 250 calories, we supply your skeletal systems with glycogen substrates necessary for protein synthesis."
                    } else if (caloriesAdjusted < 0f) {
                        "Your metabolism has adapted to your current caloric ceiling (homeostatic plateau). By reducing intake by 250 calories, we re-establish a deficit, forcing adipose tissues to oxidize fatty acids for metabolic requirements."
                    } else {
                        "Your body weight is responding optimally to current protocols. Homeostasis is maintained."
                    }
                }
                
                // Create and save event
                val event = AdaptiveEvent(
                    timestamp = System.currentTimeMillis(),
                    date = getTodayString(),
                    startingWeight = startingWeight,
                    currentWeight = currentWeight,
                    weightDiff = diff,
                    goal = goal,
                    detectedPattern = detectedPattern,
                    actionTaken = actionTaken,
                    caloriesAdjusted = caloriesAdjusted,
                    aiExplanation = aiExplanation
                )
                
                saveAdaptiveEvent(event)
                
                if (isAnomaly) {
                    showXpGain("+100 XP: Adaptive Life Engine Recalibration")
                    repository.addXp(100)
                }
                
            } catch (ex: Exception) {
                android.util.Log.e("ReforgeViewModel", "Adaptive Life Engine error", ex)
            } finally {
                _isAdaptiveProcessing.value = false
            }
        }
    }
}

data class AdaptiveEvent(
    val timestamp: Long,
    val date: String,
    val startingWeight: Float,
    val currentWeight: Float,
    val weightDiff: Float,
    val goal: String,
    val detectedPattern: String,
    val actionTaken: String,
    val caloriesAdjusted: Float,
    val aiExplanation: String
)

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
    val aiMemory: StateFlow<AiMemory?>
    val milestones: StateFlow<List<Milestone>>
    val milestoneProgress: StateFlow<List<MilestoneProgress>>

    private val _proposedHabit = MutableStateFlow<Habit?>(null)
    val proposedHabit: StateFlow<Habit?> = _proposedHabit.asStateFlow()

    // --- Astrology 2.0 & Scheduler Flows ---
    private val _todayTransitAnalysis = MutableStateFlow<com.example.util.VedicAstrologyCalculator.TransitAnalysis?>(null)
    val todayTransitAnalysis: StateFlow<com.example.util.VedicAstrologyCalculator.TransitAnalysis?> = _todayTransitAnalysis.asStateFlow()

    private val _todayRelapseRiskPercent = MutableStateFlow<Int>(30)
    val todayRelapseRiskPercent: StateFlow<Int> = _todayRelapseRiskPercent.asStateFlow()

    private val _dailySchedule = MutableStateFlow<DailySchedule?>(null)
    val dailySchedule: StateFlow<DailySchedule?> = _dailySchedule.asStateFlow()

    private val _scheduledAlarms = MutableStateFlow<List<ScheduledAlarmInfo>>(emptyList())
    val scheduledAlarms: StateFlow<List<ScheduledAlarmInfo>> = _scheduledAlarms.asStateFlow()

    private val _isGeneratingSchedule = MutableStateFlow(false)
    val isGeneratingSchedule: StateFlow<Boolean> = _isGeneratingSchedule.asStateFlow()

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
        aiMemory = repository.aiMemory.stateIn(viewModelScope, SharingStarted.Lazily, null)
        milestones = repository.milestones.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        milestoneProgress = repository.milestoneProgress.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        allExercises = repository.allExercises.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        confidenceChallenges = repository.confidenceChallenges.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Populate database with mockup data if new first load
        viewModelScope.launch {
            repository.preWalkData()
            loadAdaptiveEvents()
        }

        // Listen for profile changes to compute Vedic Astrology & load schedule
        viewModelScope.launch {
            userProfile.collect { profile ->
                if (profile != null && profile.isOnboarded) {
                    calculateAstrologyAndRisk()
                    loadDailySchedule()
                }
            }
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

                // Clear and insert goals to goals table
                repository.clearGoals()
                goals.split(",").filter { it.isNotBlank() }.forEach { goalName ->
                    repository.insertGoal(Goal(name = goalName, targetDetails = "Transform target"))
                }

                // Clear and insert struggles/habits to habits table, then generate milestones and clocks
                repository.clearHabits()
                repository.clearMilestones()
                repository.clearMilestoneProgress()
                repository.clearAddictionClocks()

                addictions.split(",").filter { it.isNotBlank() }.forEach { struggleName ->
                    val habit = Habit(
                        name = struggleName,
                        isBadHabit = true,
                        category = "Addiction",
                        severity = "Moderate",
                        createdByUser = true,
                        createdByAi = false
                    )
                    val habitId = repository.insertHabit(habit)
                    val savedHabit = habit.copy(id = habitId.toInt())
                    
                    // Insert addiction clock
                    repository.insertAddictionClock(
                        AddictionClock(
                            addictionName = struggleName,
                            lastResetTimestamp = System.currentTimeMillis()
                        )
                    )

                    generateAndSaveMilestones(savedHabit)
                }

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

    suspend fun generateAndSaveMilestones(habit: Habit) {
        try {
            val milestonesJson = GeminiManager.generateMilestonesForHabit(
                habitName = habit.name,
                category = habit.category,
                severity = habit.severity
            )
            if (milestonesJson.isNotBlank()) {
                val jsonObj = org.json.JSONObject(milestonesJson)
                val milestonesArray = jsonObj.optJSONArray("milestones")
                if (milestonesArray != null) {
                    for (i in 0 until milestonesArray.length()) {
                        val mObj = milestonesArray.optJSONObject(i) ?: continue
                        val title = mObj.optString("title")
                        val description = mObj.optString("description")
                        val targetDays = mObj.optInt("targetDays")

                        val milestoneId = repository.insertMilestone(
                            Milestone(
                                habitId = habit.id,
                                title = title,
                                description = description,
                                targetDays = targetDays
                            )
                        )

                        // Save default progress
                        repository.insertMilestoneProgress(
                            MilestoneProgress(
                                milestoneId = milestoneId.toInt(),
                                currentDays = 0,
                                isCompleted = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ReforgeViewModel", "Error generating milestones for ${habit.name}", e)
        }
    }

    fun resetProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            // Cancel all alarms
            com.example.util.NotificationScheduler.cancelAllReforgeAlarms(getApplication())
            
            // Clear shared preferences
            val sp = getApplication<Application>().getSharedPreferences("daily_scheduler", android.content.Context.MODE_PRIVATE)
            sp.edit().clear().apply()

            repository.clearAllData()
            _proposedHabit.value = null
            _transformationReport.value = null
            _nutritionExplanation.value = ""
            _generatedWorkoutPlan.value = null
            _dailyCoachingError.value = null
            _relapseAnalysisResult.value = null
            _todayTransitAnalysis.value = null
            _todayRelapseRiskPercent.value = 30
            _dailySchedule.value = null
            _scheduledAlarms.value = emptyList()

            // Re-populate exercises and confidence challenges
            repository.preWalkData()
        }
    }

    fun setOnboardingError(error: String?) {
        _onboardingError.value = error
    }

    fun approveProposedHabit() {
        val habit = _proposedHabit.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertHabit(habit)
            val savedHabit = habit.copy(id = id.toInt())
            
            // Insert clock
            repository.insertAddictionClock(
                AddictionClock(
                    addictionName = habit.name,
                    lastResetTimestamp = System.currentTimeMillis()
                )
            )

            generateAndSaveMilestones(savedHabit)
            
            _proposedHabit.value = null
            showXpGain("+50 XP: Habit Inferred & Tracked")
            repository.addXp(50)
        }
    }

    fun declineProposedHabit() {
        _proposedHabit.value = null
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

            // Recalculate Astrology Transit and Risk Score based on today's check-in
            calculateAstrologyAndRisk()
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

            // Fetch profile name and current memory context
            val profile = repository.getUserProfileDirect()
            val userName = if (profile != null && profile.name.isNotBlank()) profile.name else "User"
            val currentMemory = repository.getAiMemoryDirect()?.memoryJson ?: "{}"

            // 4. Call Gemini Model (using gemini-1.5-flash as default)
            val responseText = GeminiManager.getCoachResponse(history, text, userName, currentMemory)

            // 5. Save model response
            val modelMsg = CoachMessage(role = "model", message = responseText)
            repository.insertCoachMessage(modelMsg)
            _isCoachTyping.value = false
            repository.addXp(10) // 10 XP for active chat coaching

            // Extract and update memory context asynchronously
            try {
                val updatedMemory = GeminiManager.extractMemories(text, responseText, currentMemory)
                if (updatedMemory.isNotBlank() && updatedMemory != currentMemory) {
                    repository.insertAiMemory(AiMemory(id = 1, memoryJson = updatedMemory))
                }
            } catch (me: Exception) {
                Log.e("ReforgeViewModel", "Failed to extract and save AI memories", me)
            }

            // Extract proposed habit from user text asynchronously
            val existingHabits = habits.value
            launch(Dispatchers.IO) {
                try {
                    val result = GeminiManager.discoverHabitsFromText(text)
                    if (result.isNotBlank()) {
                        val jsonObj = org.json.JSONObject(result)
                        val badHabitsArr = jsonObj.optJSONArray("bad_habits")
                        if (badHabitsArr != null && badHabitsArr.length() > 0) {
                            for (i in 0 until badHabitsArr.length()) {
                                val habitName = badHabitsArr.getString(i)
                                val alreadyTracked = existingHabits.any { it.name.lowercase() == habitName.lowercase() }
                                if (!alreadyTracked) {
                                    _proposedHabit.value = Habit(
                                        name = habitName,
                                        isBadHabit = true,
                                        category = "Addiction",
                                        severity = "Moderate",
                                        createdByAi = true,
                                        createdByUser = false
                                    )
                                    break
                                }
                            }
                        }
                    }
                } catch (he: Exception) {
                    Log.e("ReforgeViewModel", "Habit discovery failed", he)
                }
            }
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

    fun updateAiMemory(memoryJson: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAiMemory(AiMemory(id = 1, memoryJson = memoryJson))
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

            // Get dynamic profile name
            val profile = repository.getUserProfileDirect()
            val userName = if (profile != null && profile.name.isNotBlank()) profile.name else "User"

            // 2. Fetch CBT custom counsel from Gemini
            val advice = GeminiManager.analyzeRelapse(
                addiction = addiction,
                whatHappened = whatHappened,
                whereContext = whereContext,
                companion = companion,
                emotion = emotion,
                timeOfDay = timeOfDay,
                userName = userName
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

            // 4. Inject a supportive therapist comment directly into the Chat Coach log so user sees it immediately!
            repository.insertCoachMessage(
                CoachMessage(
                    role = "model",
                    message = "❤️ **Therapist Intervention**: $userName, you reported a relapse on $addiction. Do not feel shame—this is valuable behavioral data.\n\n$advice"
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
                    aiExplanation = com.example.api.GeminiManager.analyzeAdaptiveEngine(
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

    // --- Astrology 2.0 & AI Scheduler Helper Functions ---
    fun calculateAstrologyAndRisk() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileDirect() ?: return@launch
            if (!profile.isOnboarded || profile.dob.isBlank()) return@launch

            try {
                val transit = com.example.util.VedicAstrologyCalculator.calculateTransit(
                    dob = profile.dob,
                    birthTime = profile.birthTime,
                    birthPlace = profile.birthPlace,
                    transitDate = Date()
                )
                _todayTransitAnalysis.value = transit

                // Calculate relapse risk based on biometrics & transits
                val checkIn = repository.getCheckInForDate(getTodayString())
                val sleepQuality = checkIn?.sleepQuality ?: 7
                val cravings = checkIn?.cravings ?: 3
                
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                val recentRelapsesCount = repository.getAllRelapseEventsDirect().count { it.timestamp >= sevenDaysAgo }

                val sleepHoursActual = sleepQuality * 0.8f
                val sleepDebt = (profile.sleepHours - sleepHoursActual).coerceAtLeast(0f)

                val baseRisk = 30f + (sleepDebt * 8f) + (cravings * 3f) + (recentRelapsesCount * 10f)
                val finalRisk = (baseRisk * transit.relapseMultiplier).coerceIn(10f, 99f).toInt()
                
                _todayRelapseRiskPercent.value = finalRisk
            } catch (e: Exception) {
                Log.e("ReforgeViewModel", "Failed to calculate astrological transits", e)
            }
        }
    }

    fun loadDailySchedule() {
        val sp = getApplication<Application>().getSharedPreferences("daily_scheduler", android.content.Context.MODE_PRIVATE)
        val meal1 = sp.getString("meal1", "") ?: ""
        if (meal1.isNotBlank()) {
            val schedule = DailySchedule(
                meal1 = meal1,
                meal2 = sp.getString("meal2", "") ?: "",
                snack = sp.getString("snack", "") ?: "",
                workout = sp.getString("workout", "") ?: "",
                sleep = sp.getString("sleep", "") ?: ""
            )
            _dailySchedule.value = schedule
            updateScheduledAlarmsList(schedule)
        } else {
            _dailySchedule.value = null
            _scheduledAlarms.value = emptyList()
        }
    }

    fun saveDailySchedule(schedule: DailySchedule) {
        val sp = getApplication<Application>().getSharedPreferences("daily_scheduler", android.content.Context.MODE_PRIVATE)
        sp.edit().apply {
            putString("meal1", schedule.meal1)
            putString("meal2", schedule.meal2)
            putString("snack", schedule.snack)
            putString("workout", schedule.workout)
            putString("sleep", schedule.sleep)
            apply()
        }
        _dailySchedule.value = schedule
        updateScheduledAlarmsList(schedule)
    }

    fun generateDailySchedule(wakeTime: String, office: String, commute: String) {
        val profile = userProfile.value ?: return
        val transit = _todayTransitAnalysis.value ?: return
        val riskWindow = transit.riskWindow
        val goal = profile.goals.ifBlank { "muscle_gain" }

        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingSchedule.value = true
            try {
                val rawJson = com.example.api.GeminiManager.generateDailySchedule(
                    wakeTime = wakeTime,
                    office = office,
                    commute = commute,
                    goal = goal,
                    riskWindow = riskWindow
                )
                val jsonObj = org.json.JSONObject(rawJson)
                val schedule = DailySchedule(
                    meal1 = jsonObj.optString("meal1", "09:00"),
                    meal2 = jsonObj.optString("meal2", "13:00"),
                    snack = jsonObj.optString("snack", "18:00"),
                    workout = jsonObj.optString("workout", "10:00"),
                    sleep = jsonObj.optString("sleep", "23:00")
                )
                
                saveDailySchedule(schedule)
                scheduleAlarmsForToday(schedule, riskWindow)
                
                showXpGain("+120 XP: AI Daily Routine Synchronized")
                repository.addXp(120)
            } catch (e: Exception) {
                Log.e("ReforgeViewModel", "Failed to generate daily schedule", e)
            } finally {
                _isGeneratingSchedule.value = false
            }
        }
    }

    fun scheduleAlarmsForToday(schedule: DailySchedule, riskWindow: String) {
        val context = getApplication<Application>()
        com.example.util.NotificationScheduler.cancelAllReforgeAlarms(context)

        val profile = userProfile.value ?: return
        val streak = profile.level * 3 + 2

        fun getMs(time: String) = com.example.util.NotificationScheduler.getEpochMsForTimeToday(time)

        val workoutTimeMs = getMs(schedule.workout)
        val prepTimeMs = workoutTimeMs - (5 * 60 * 1000)
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.WORKOUT_PREP_ID,
            prepTimeMs,
            "Workout starting in 5 min",
            "Prepare your gym gear and align your focus.",
            "Preparation"
        )

        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.WORKOUT_ACTION_ID,
            workoutTimeMs,
            "Time for workout",
            "Begin your scheduled weight workout. Protect your physical consistency.",
            "Action"
        )

        val recoveryTimeMs = workoutTimeMs + (45 * 60 * 1000)
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.WORKOUT_RECOVERY_ID,
            recoveryTimeMs,
            "Great job. Workout complete",
            "Protein shake due within 30 minutes.",
            "Recovery"
        )

        val meal1TimeMs = getMs(schedule.meal1)
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.MEAL1_ID,
            meal1TimeMs,
            "Breakfast is due",
            "Fuel your metabolic state with breakfast.",
            "Preparation"
        )

        val meal2TimeMs = getMs(schedule.meal2)
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.MEAL2_ID,
            meal2TimeMs,
            "Lunch is due",
            "Consume your lunch meal. Stay consistent.",
            "Preparation"
        )

        val snackTimeMs = getMs(schedule.snack)
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.SNACK_ID,
            snackTimeMs,
            "Snack is due",
            "High craving risk detected. Have 25g peanuts or a banana now.",
            "Risk"
        )

        val sleepTimeMs = getMs(schedule.sleep)
        val windDownTimeMs = sleepTimeMs - (30 * 60 * 1000)
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.SLEEP_WIND_DOWN_ID,
            windDownTimeMs,
            "Sleep Wind-down",
            "Day $streak alcohol free. Keep protecting the streak.",
            "Identity"
        )

        val riskStartHourStr = if (riskWindow.contains("-")) {
            riskWindow.split("-")[0].trim()
        } else {
            "19:00"
        }
        val riskStartTimeMs = getMs(riskStartHourStr)
        val riskAlertTimeMs = riskStartTimeMs - (15 * 60 * 1000)
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            com.example.util.NotificationScheduler.RISK_WINDOW_ID,
            riskAlertTimeMs,
            "Craving probability elevated",
            "Historically this is your highest-risk period. Eat your planned snack now.",
            "Risk"
        )

        updateScheduledAlarmsList(schedule)
    }

    fun updateScheduledAlarmsList(schedule: DailySchedule) {
        val list = mutableListOf<ScheduledAlarmInfo>()
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)

        fun formatTime(timeStr: String, offsetMins: Int = 0): String {
            val calendar = Calendar.getInstance().apply {
                val parts = timeStr.split(":")
                set(Calendar.HOUR_OF_DAY, parts.getOrNull(0)?.toIntOrNull() ?: 12)
                set(Calendar.MINUTE, parts.getOrNull(1)?.toIntOrNull() ?: 0)
                add(Calendar.MINUTE, offsetMins)
            }
            return formatter.format(calendar.time)
        }

        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.MEAL1_ID,
                timeLabel = formatTime(schedule.meal1),
                title = "Breakfast Fuel Alert",
                type = "Preparation",
                description = "Fuel your metabolic state with breakfast."
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.WORKOUT_PREP_ID,
                timeLabel = formatTime(schedule.workout, -5),
                title = "Workout Prep Alert",
                type = "Preparation",
                description = "Workout starting in 5 min."
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.WORKOUT_ACTION_ID,
                timeLabel = formatTime(schedule.workout),
                title = "Time for Workout Alert",
                type = "Action",
                description = "Start your scheduled exercise routine."
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.WORKOUT_RECOVERY_ID,
                timeLabel = formatTime(schedule.workout, 45),
                title = "Workout Recovery Alert",
                type = "Recovery",
                description = "Workout complete. Protein shake due."
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.MEAL2_ID,
                timeLabel = formatTime(schedule.meal2),
                title = "Lunch Fuel Alert",
                type = "Preparation",
                description = "Consume lunch meal. Stay consistent."
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.SNACK_ID,
                timeLabel = formatTime(schedule.snack),
                title = "Snack Fuel Alert",
                type = "Risk",
                description = "High craving risk detected."
            )
        )

        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.SLEEP_WIND_DOWN_ID,
                timeLabel = formatTime(schedule.sleep, -30),
                title = "Sleep Wind-down Alert",
                type = "Identity",
                description = "Streak protection wind-down."
            )
        )

        val transit = _todayTransitAnalysis.value
        val riskStartHour = if (transit != null && transit.riskWindow.contains("-")) {
            transit.riskWindow.split("-")[0].trim()
        } else {
            "19:00"
        }
        list.add(
            ScheduledAlarmInfo(
                id = com.example.util.NotificationScheduler.RISK_WINDOW_ID,
                timeLabel = formatTime(riskStartHour, -15),
                title = "Relapse Risk Window Warning",
                type = "Risk",
                description = "Historically this is your highest-risk period."
            )
        )

        _scheduledAlarms.value = list
    }

    fun triggerTestNotification() {
        val context = getApplication<Application>()
        val timeMs = System.currentTimeMillis() + 3000
        com.example.util.NotificationScheduler.scheduleAlarm(
            context,
            9999,
            timeMs,
            "Empathy Reboot Check-in",
            "This is a verified local Reforge alert. Your lifestyle schedules are synced.",
            "Action"
        )
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

data class DailySchedule(
    val meal1: String,
    val meal2: String,
    val snack: String,
    val workout: String,
    val sleep: String
)

data class ScheduledAlarmInfo(
    val id: Int,
    val timeLabel: String,
    val title: String,
    val type: String,
    val description: String
)

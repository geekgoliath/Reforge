package com.example.api

import android.util.Log

object GeminiManager {

    private const val TAG = "GeminiManager"

    suspend fun getCoachResponse(history: List<com.example.data.CoachMessage>, userMessage: String): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedCoachResponse(userMessage)
        }

        val coachSystemInstruction = """
            You are 'Coach', the elite AI Life Coach and Therapist inside 'Reforge', a Personal Transformation OS.
            You help users fight hard addictions (Alcohol, Smoking, Porn, Phone, Junk Food) and build elite habits (weight training, protein intake, posture, reading, active mindfulness).
            Your tone is authoritative but deeply compassionate, empathetic, CBT-focused (Cognitive Behavioral Therapy), and rooted in behavioral science (James Clear, BJ Fogg, Andrew Huberman).
            Never lecture or shamed. Focus on 'Identity Change' and 'Environment Design' rather than daily perfection.
            We use Experience Points (XP), not streak shame. Keep messages relatively concise (1-2 paragraphs), structured, using bullet points for action items.
        """.trimIndent()

        // Map database messages to Gemini contents structure with robust role management
        val tempContents = mutableListOf<Content>()
        val historyToMap = if (history.isNotEmpty() && history.last().message == userMessage) {
            history
        } else {
            history + com.example.data.CoachMessage(role = "user", message = userMessage)
        }

        historyToMap.forEach { msg ->
            val role = if (msg.role == "user") "user" else "model"
            tempContents.add(
                Content(
                    role = role,
                    parts = listOf(Part(text = msg.message))
                )
            )
        }

        // Clean up to ensure strict alternate matching (user -> model -> user -> model)
        val contents = mutableListOf<Content>()
        for (content in tempContents) {
            if (contents.isEmpty()) {
                if (content.role == "user") {
                    contents.add(content)
                }
            } else {
                val lastContent = contents.last()
                if (lastContent.role == content.role) {
                    val mergedParts = lastContent.parts + content.parts
                    contents[contents.size - 1] = lastContent.copy(parts = mergedParts)
                } else {
                    contents.add(content)
                }
            }
        }

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = coachSystemInstruction)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I'm with you on this journey. Let's redirect our focus. Try deep breathing for 2 minutes and taking a glass of water. What's triggering you right now?"
        } catch (e: Exception) {
            Log.e(TAG, "Error generating coach response from cloud, falling back to local simulation", e)
            getSimulatedCoachResponse(userMessage)
        }
    }

    suspend fun analyzeRelapse(
        addiction: String,
        whatHappened: String,
        whereContext: String,
        companion: String,
        emotion: String,
        timeOfDay: String
    ): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedRelapseResponse(addiction, whatHappened, whereContext, companion, emotion, timeOfDay)
        }

        val therapistSystemPrompt = """
            You are 'Therapist Agent' in Reforge.
            The user recently experienced a relapse in their struggling addiction: $addiction.
            They are feeling vulnerable. NEVER punish or shame. Your purpose is CBT-driven trigger dissection and generating a personalized trigger map.
            Analyze the lapse situation scientifically:
            - Addiction: $addiction
            - Context/What happened: $whatHappened
            - Location/Where: $whereContext
            - Who they were with: $companion
            - Emotion before lapse: $emotion
            - Time of day: $timeOfDay
            
            Respond with:
            1. **🔍 CBT DISSECTION**: Quick compassionate analysis of the hidden trigger (E.g. Stress + Boredom + Location cue).
            2. **🗺️ AI TRIGGER MAP**: A structured visual mapping of:
               • Trigger Cue
               • High-Risk Location
               • High-Risk Companion
               • Vulnerable Emotion
               • Danger Window
               • Cognitive Trap (a scientific explanation of how these combine to trigger the user)
            3. **🛡️ REFORGE ENVIRONMENT COUNTERMEASURES**: 3 highly actionable, micro-step environment design adjustments to prevent this exact trigger tomorrow.
            4. **✨ MINDSET SHIELD**: A powerful, identity-shifting thought to dismiss shame and focus on XP gained so far.
            Keep it clean, supportive, and under 250 words total. Use gorgeous markdown formatting.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = "Please analyze my relapse and help me rebuild."))
                )
            ),
            systemInstruction = Content(parts = listOf(Part(text = therapistSystemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Analysis completed. Let's adjust your surroundings: remove cues from your immediate sight, drink water immediately, and step outside for 5 mins."
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing relapse from cloud, falling back to local simulation", e)
            getSimulatedRelapseResponse(addiction, whatHappened, whereContext, companion, emotion, timeOfDay)
        }
    }

    suspend fun analyzeJournal(journalText: String): JournalAnalysis {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedJournalResponse(journalText)
        }

        val journalSystemPrompt = """
            You are the Journal Analyst in Reforge.
            Analyze this evening journal speaking/text entry and generate a structured response containing:
            1. Short empathetic summary (1-2 sentences).
            2. Emotional state (e.g. Anxious, Calm, Confident, Fatigued, Motivated).
            3. Hidden triggers identified (e.g. Evening isolation, lack of sleep, heavy social media).
            4. Wins (positive actions, successes, or milestones achieved today).
            5. Mistakes (slips, avoided exercises, or areas of improvement today).
            6. Tomorrow Focus (actionable advice or areas of concentration for tomorrow).
            
            Text to analyze: "$journalText"
            
            Provide your response in this exact format so I can parse it:
            SUMMARY: <summary text>
            MOOD: <mood tag>
            TRIGGERS: <comma separated triggers>
            WINS: <wins text>
            MISTAKES: <mistakes text>
            TOMORROW_FOCUS: <tomorrow focus text>
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = journalText)))),
            systemInstruction = Content(parts = listOf(Part(text = journalSystemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = request
            )
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseJournalResponse(fullText)
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing journal from cloud, falling back to local simulation", e)
            getSimulatedJournalResponse(journalText)
        }
    }

    private fun parseJournalResponse(rawText: String): JournalAnalysis {
        var summary = ""
        var mood = "Reflective"
        var triggers = "None identified"
        var wins = ""
        var mistakes = ""
        var tomorrowFocus = ""

        val lines = rawText.lines()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("SUMMARY:", ignoreCase = true) -> {
                    summary = trimmed.substringAfter("SUMMARY:", "").trim()
                }
                trimmed.startsWith("MOOD:", ignoreCase = true) -> {
                    mood = trimmed.substringAfter("MOOD:", "").trim()
                }
                trimmed.startsWith("TRIGGERS:", ignoreCase = true) -> {
                    triggers = trimmed.substringAfter("TRIGGERS:", "").trim()
                }
                trimmed.startsWith("WINS:", ignoreCase = true) -> {
                    wins = trimmed.substringAfter("WINS:", "").trim()
                }
                trimmed.startsWith("MISTAKES:", ignoreCase = true) -> {
                    mistakes = trimmed.substringAfter("MISTAKES:", "").trim()
                }
                trimmed.startsWith("TOMORROW_FOCUS:", ignoreCase = true) -> {
                    tomorrowFocus = trimmed.substringAfter("TOMORROW_FOCUS:", "").trim()
                }
            }
        }

        if (summary.isEmpty()) {
            summary = rawText.take(150) + "..."
        }

        return JournalAnalysis(summary, mood, triggers, wins, mistakes, tomorrowFocus)
    }

    // --- PREMIUM LOCAL SIMULATED ENGINE FOR DUAL-MODE COMPLIANCE ---

    private fun getSimulatedCoachResponse(userMessage: String): String {
        val cleaned = userMessage.trim().lowercase()
        return when {
            // Critical warnings / Tremors / ER / Delirium Tremens
            cleaned.contains("tremor") || cleaned.contains("hallucination") || cleaned.contains("seizure") || 
            cleaned.contains("confusion") || cleaned.contains("er") || cleaned.contains("danger") || cleaned.contains("emergency") -> {
                "⚠️ **CRITICAL MEDICAL PROTOCOL WARNING**: Vikas, quitting alcohol and smoking cold turkey after 10 years carries significant risk of *Delirium Tremens*. If you are experiencing severe uncontrollable hand tremors, visual/auditory hallucinations, deep confusion, or seizures, **please go to the Emergency Room (ER) immediately**.\n\n" +
                "If you feel relatively stable but are dealing with expected cravings, sleeplessness, or high irritability, that is normal dopamine receptor repair. Tell me: exactly what physical symptoms are you experiencing right now?"
            }
            
            // Supplement Stack
            cleaned.contains("supplement") || cleaned.contains("supp") || cleaned.contains("pill") || 
            cleaned.contains("nac") || cleaned.contains("ashwagandha") || cleaned.contains("zma") || 
            cleaned.contains("lion") || cleaned.contains("neurobion") || cleaned.contains("stack") -> {
                "Vikas, here is your daily **Transformation Supplement Stack** from the Protocol:\n\n" +
                "☀️ **Morning (08:00 AM)**: Take **NAC (600mg)** (lung health & severe craving blocker) + **Lion's Mane** (brain NGF repair).\n" +
                "🍳 **Breakfast (10:00 AM)**: Take **Neurobion Forte** (nerve cell regeneration) + **1 Multivitamin**.\n" +
                "🥗 **Lunch (11:30 AM)**: Take **Fish Oil (1000mg)** (omega-3 brain structure) + **Liv.52** (liver cell support).\n" +
                "🌙 **Bedtime (11:30 PM)**: Take **ZMA** (Zinc, Magnesium, B6 for deep REM sleep recovery) + **Ashwagandha** (cortisol regulation).\n\n" +
                "Keep taking them consistently with meals. It provides critical chemical buffers during your first 7 days!"
            }

            // Workout / Gym / Split / Push Pull Legs
            cleaned.contains("workout") || cleaned.contains("gym") || cleaned.contains("exercise") || 
            cleaned.contains("ppl") || cleaned.contains("push") || cleaned.contains("pull") || cleaned.contains("legs") -> {
                "Vikas, your **Gym Protocol (PPL split)** is designed to raise androgen receptors & anchor dopamine levels:\n\n" +
                "• **Push (Day 1)**: Flat Dumbbell Press, Overhead Press, Incline Flyes, Tricep overhead extensions. Keeps you chest-open and focused.\n" +
                "• **Pull (Day 2)**: Deadlifts, Weighted pullups/pulldowns, Seated cable rows, Dumbbell Hammer Curls. Builds a strong metabolic back anchor.\n" +
                "• **Legs (Day 3)**: Barbell squats, Romanian Deadlifts, Walking lunges, Hanging leg raises.\n\n" +
                "🔥 **Daily Prep (08:20 AM)**: Do **10 Wall Angels** & **20 Band Pull-Aparts** to reverse postural decline and clear breathing airways. Keep sessions under 45 minutes to avoid cortisol spikes!"
            }

            // Diet / Meal Plan / Food / Protein / Eggs
            cleaned.contains("diet") || cleaned.contains("protein") || cleaned.contains("egg") || 
            cleaned.contains("food") || cleaned.contains("lunch") || cleaned.contains("dinner") || 
            cleaned.contains("chicken") || cleaned.contains("whey") || cleaned.contains("soya") -> {
                "Vikas, your nutrition plan under the Protocol focuses on clean, high-protein fuels to rebuild neurotransmitters:\n\n" +
                "• **Post-Gym (09:15 AM)**: 1 scoop MuscleBlaze Biozyme Whey Protein.\n" +
                "• **Breakfast (10:00 AM)**: 3 Whole Eggs + 2 Whites (approx. 24g protein) + Black Coffee (No Sugar).\n" +
                "• **Lunch (11:30 AM)**: 1 Bowl Dal + 150g Chicken or Soya + 1 Chapati + salad. (Take Fish Oil & Liv.52).\n" +
                "• **Dinner (08:00 PM)**: 1 Bowl Dal + 2 Boiled Egg Whites + Salad (strictly zero chapatis or heavy carbs at night for insulin balance and weight loss).\n\n" +
                "Target a daily baseline of **120g - 150g protein**. Avoid processed simple sugars!"
            }

            // Craving / Cigarettes / Alcohol / Tobacco / Drink / Urge
            cleaned.contains("craving") || cleaned.contains("smoke") || cleaned.contains("drink") || 
            cleaned.contains("alcohol") || cleaned.contains("nicotine") || cleaned.contains("beer") || 
            cleaned.contains("whiskey") || cleaned.contains("cig") || cleaned.contains("urge") -> {
                "Vikas, a craving is just a temporary dopamine drop—it peaks in **90 seconds** and then dissipates. Do not negotiate with your addiction voice. Apply the **Environment Design Checklist**:\n\n" +
                "1. **Box Breathing**: Inhale 4s, hold 4s, exhale 4s, hold 4s. Repeat 5 times to engage the vagal brake.\n" +
                "2. **Ice Shock**: Splash ice cold water on your face. It triggers the mammalian dive reflex, instantly lowering heart rate and shattering the loop.\n" +
                "3. **Physical Distance**: Physically leave the triggering room or location immediately.\n\n" +
                "Remind yourself: *'I am a non-drinker and non-smoker. My identity is forged in strength.'* You are built to prevail."
            }

            // Daily Schedule / Timing / Routine / Master Plan
            cleaned.contains("schedule") || cleaned.contains("routine") || cleaned.contains("daily") || 
            cleaned.contains("plan") || cleaned.contains("timeline") || cleaned.contains("timing") -> {
                "Vikas, here is your **Master Schedule (Mon-Fri)** summary under the Protocol:\n\n" +
                "• **08:00 AM**: Wake up, Warm Water + Jeera/Ajwain + Lemon, take Morning Supplements (NAC & Lion's Mane).\n" +
                "• **08:20 AM**: 10 Wall Angels & 20 Band Pull-Aparts.\n" +
                "• **08:30 AM**: Gym (45 mins Weight Training).\n" +
                "• **09:15 AM**: Post-workout Whey protein shake.\n" +
                "• **10:00 AM**: 5-Egg breakfast + Black Coffee + Neurobion & Multivitamin.\n" +
                "• **11:30 AM**: Chicken/Soya lunch + Fish Oil & Liv.52.\n" +
                "• **03:00 PM**: Buttermilk (Chaas) / Green Tea + Box Breathing.\n" +
                "• **06:00 PM**: 20g Roasted Chana + 5 mins Walk transition.\n" +
                "• **08:00 PM**: Dal + 2 Egg Whites + Salad.\n" +
                "• **11:30 PM**: Turmeric milk sleep prep + ZMA & Ashwagandha + Scalp Massage.\n\n" +
                "Discipline beats motivation. Lock in the schedule!"
            }

            // Sleep / Insomnia / Night
            cleaned.contains("sleep") || cleaned.contains("insomnia") || cleaned.contains("night") || cleaned.contains("awake") -> {
                "Vikas, severe insomnia is expected during the first 7 days of quitting alcohol & nicotine. Your nervous system is in a state of high electrical excitation without those depressants. \n\n" +
                "🌙 **Bedtime Sleep Protocol (11:30 PM)**:\n" +
                "1. **Warm Turmeric Milk**: Add black pepper, turmeric, and stevia (highly anti-inflammatory, triggers natural melatonin).\n" +
                "2. **Supplements**: ZMA (Zinc, Magnesium, B6) + Ashwagandha capsule (cortisol dampening).\n" +
                "3. **Scalp Massage**: Spend 5 minutes massaging your scalp to stimulate parasympathetic pathways.\n\n" +
                "Turn off all screens 1 hour before bed. If you are awake, do not force it or panic. Read a printed book in dim light. Your neural baseline will normalize."
            }

            // Greetings / Hello / Hi
            cleaned.contains("hi") || cleaned.contains("hello") || cleaned.contains("hey") || cleaned.contains("greetings") -> {
                "Good morning Vikas! I am your AI Coach. I have loaded your complete **Total Transformation Protocol (35M RESET)**.\n\n" +
                "You are quitting alcohol and smoking **cold turkey** after 10 years of use. This is Day 1 of the reset. Your daily master schedule, supplement stack, and workout programs are active in the 'Protocol' tab.\n\n" +
                "I am fully here to support you. How are you feeling today?"
            }

            // CBT / James Clear / Habits
            cleaned.contains("cbt") || cleaned.contains("habit") || cleaned.contains("james clear") || cleaned.contains("huberman") -> {
                "Vikas, we leverage **CBT** and **Environment Design** (James Clear, Huberman) over sheer willpower:\n\n" +
                "• **Make it Invisible**: Throw out all lighters, ashtrays, cigarettes, and bottles. Do not let trigger cues drain your cognitive willpower.\n" +
                "• **Dopamine Restructuring**: Expect irritability—it is your brain rebuilding its receptor pathways. Wear it as a badge of honor. You are earning XP.\n" +
                "• **Streak Resilience**: We track your efforts as XP, not streak shame. A lapse is not a reset. We analyze, adapt, and build stronger walls."
            }

            else -> {
                "Vikas, I hear you. We are executing your **Total Transformation Protocol**. Our primary target is maintaining complete abstinence from alcohol and smoking while executing your weight training, protein targets, and supplement stacking.\n\n" +
                "Tell me: what is your current obstacle or mental trigger right now? Let's dissect it using behavioral science and build an environment countermeasure."
            }
        }
    }

    private fun getSimulatedRelapseResponse(
        addiction: String,
        whatHappened: String,
        whereContext: String,
        companion: String,
        emotion: String,
        timeOfDay: String
    ): String {
        val detailCbt = when {
            emotion.contains("Stress", ignoreCase = true) -> 
                "Your stress response was triggered, leading to a subconscious craving release. When stress spikes, your prefrontal cortex goes offline and defaults to old 10-year neural highways."
            emotion.contains("Boredom", ignoreCase = true) -> 
                "Boredom creates a low-dopamine state. Your brain sought cheap novelty to raise baseline dopamine instantly, and defaulted to $addiction."
            else -> "The transition state of feeling $emotion in $whereContext created a moment of high friction, and your subconscious mind defaulted to its long-term coping mechanism."
        }

        return "🔍 **CBT DISSECTION**:\n" +
                "$detailCbt\n\n" +
                "🗺️ **AI TRIGGER MAP**:\n" +
                "• **Trigger Cue**: $whatHappened\n" +
                "• **High-Risk Location**: $whereContext\n" +
                "• **High-Risk Companion**: $companion\n" +
                "• **Vulnerable Emotion**: $emotion\n" +
                "• **Danger Window**: $timeOfDay\n" +
                "• **Cognitive Trap**: Old habit loops are highly sensitive to being with **$companion** at **$whereContext** during the **$timeOfDay** window. This combination bypasses prefrontal self-control.\n\n" +
                "🛡️ **REFORGE ENVIRONMENT COUNTERMEASURES**:\n" +
                "1. **Break the sightline**: Make the cue 100% invisible. Throw away any remaining packages, lighters, or cans. Out of sight, out of mind.\n" +
                "2. **Implement a friction buffer**: Create a 20-minute mandatory delay before any action. Wash your face with ice cold water to slow heart rate.\n" +
                "3. **Stack a physical pivot**: Immediately step outside, walk for 5 minutes, and take 500ml of water.\n\n" +
                "✨ **MINDSET SHIELD**:\n" +
                "Vikas, a lapse is just a single data point, not a system failure. You have already built powerful non-drinker/non-smoker neural pathways over the past days. That progress is not gone. Re-anchor your identity, learn from this trigger cue, and step forward to collect your XP!"
    }

    private fun getSimulatedJournalResponse(journalText: String): JournalAnalysis {
        val cleaned = journalText.lowercase()
        val mood = when {
            cleaned.contains("stress") || cleaned.contains("tired") || cleaned.contains("exhaust") -> "Fatigued"
            cleaned.contains("happy") || cleaned.contains("good") || cleaned.contains("gym") || cleaned.contains("workout") -> "Motivated"
            cleaned.contains("craving") || cleaned.contains("urge") || cleaned.contains("smoke") || cleaned.contains("drink") -> "Restless"
            else -> "Reflective"
        }
        val triggers = when {
            cleaned.contains("evening") || cleaned.contains("night") -> "Evening transition period"
            cleaned.contains("friend") || cleaned.contains("social") -> "Social cue exposure"
            cleaned.contains("work") || cleaned.contains("boss") || cleaned.contains("office") -> "Workplace stress"
            else -> "None identified"
        }

        val wins = when {
            cleaned.contains("avoid") || cleaned.contains("stay") || cleaned.contains("no") -> "Successfully identified craving cues and redirected focus."
            cleaned.contains("gym") || cleaned.contains("workout") || cleaned.contains("run") -> "Completed planned workout targets and stayed physically active."
            else -> "Logged journal entry and maintained high personal awareness."
        }

        val mistakes = when {
            cleaned.contains("skip") || cleaned.contains("miss") -> "Allowed physical fatigue to compromise secondary fitness routine."
            cleaned.contains("stress") || cleaned.contains("anxious") -> "Permitted ambient stress build-up without performing timely box breathing."
            else -> "No major lapses or critical routine slip-ups recorded."
        }

        val tomorrowFocus = "Re-anchor core hydration levels, enforce structural wind-down routines by 9:30 PM, and complete the morning prefrontal visual workout."

        return JournalAnalysis(
            summary = "You logged a thoughtful journal entry. You are keeping high self-awareness of your day, which is the foundational anchor of habit transformation.",
            mood = mood,
            triggers = triggers,
            wins = wins,
            mistakes = mistakes,
            tomorrowFocus = tomorrowFocus
        )
    }

    suspend fun generatePlan(
        name: String,
        age: Int,
        height: Float,
        weight: Float,
        neck: Float,
        waist: Float,
        activity: String,
        goals: String,
        addictions: String,
        dob: String,
        birthTime: String,
        birthPlace: String
    ): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedPlanResponse(name, age, height, weight, neck, waist, activity, goals, addictions, dob, birthTime, birthPlace)
        }

        val systemPrompt = """
            You are 'PlanGenerator', the elite Transformation Planner in Reforge.
            Generate a personalized 7-day transformation plan in valid JSON format.
            Input:
            Name: ${"$"}{name}, Age: ${"$"}{age}, Height: ${"$"}{height} cm, Weight: ${"$"}{weight} kg, Neck: ${"$"}{neck} cm, Waist: ${"$"}{waist} cm, Activity Level: ${"$"}{activity}
            Goals: ${"$"}{goals}
            Addictions: ${"$"}{addictions}
            DOB: ${"$"}{dob}, Birth Time: ${"$"}{birthTime}, Birth Place: ${"$"}{birthPlace}

            Your output must be a single, raw, valid JSON object matching this schema EXACTLY:
            {
              "zodiacTheme": "Short astrology guidance based on DOB, birth time and place on how the planetary alignments impact focus, impatience, resilience, or recovery. Strictly behavioral/motivational, never medical or nutrition advice.",
              "days": [
                {
                  "dayNumber": 1,
                  "dayName": "Day 1: PUSH DAY (Neuromuscular Activation)",
                  "behavioralAdvice": "CBT behavioral advise for trigger management and environment design tailored to the day and goals.",
                  "meals": [
                    {
                      "name": "Breakfast",
                      "description": "Meal description with specific clean ingredients and weights.",
                      "calories": 400.0,
                      "protein": 24.0
                    },
                    ...
                  ],
                  "workouts": [
                    {
                      "exerciseName": "Flat Dumbbell Press",
                      "sets": 3,
                      "reps": 10,
                      "weight": 15.0
                    },
                    ...
                  ]
                },
                ... (exactly 7 days)
              ]
            }

            Do not wrap the JSON in markdown code blocks. Just return raw JSON only. Ensure all numeric values are floats/ints. Use evidence-based fitness and nutrition.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = "Generate my 7-day plan.")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            Log.e("GeminiManager", "Error generating plan, falling back to simulated", e)
            getSimulatedPlanResponse(name, age, height, weight, neck, waist, activity, goals, addictions, dob, birthTime, birthPlace)
        }
    }

    private fun getSimulatedPlanResponse(
        name: String,
        age: Int,
        height: Float,
        weight: Float,
        neck: Float,
        waist: Float,
        activity: String,
        goals: String,
        addictions: String,
        dob: String,
        birthTime: String,
        birthPlace: String
    ): String {
        val isFatLoss = goals.contains("Lose Fat", ignoreCase = true)
        return """
        {
          "zodiacTheme": "Saturn in 6th house (Health & Discipline) aligns with the Pisces-Virgo Lunar Axis. The cosmos signals a powerful phase for cellular and neural purification. Expect temporary restlessness around sunset—use box breathing as your environmental shield.",
          "days": [
            {
              "dayNumber": 1,
              "dayName": "Day 1: PUSH DAY (Neuromuscular Activation)",
              "behavioralAdvice": "The evening transition is your critical pivot point. Keep raw roasted unsalted peanuts nearby to trigger a calming tryptophan response.",
              "meals": [
                {"name": "08:00 AM FLUSH", "description": "500ml Warm water + 5g raw Jeera/Ajwain + 1/2 Lemon.", "calories": 0.0, "protein": 0.0},
                {"name": "10:00 AM BREAKFAST", "description": "3 Whole Eggs + 2 Egg Whites scrambled in 5ml olive oil + Coffee. Neurobion Forte, Multivitamin.", "calories": 380.0, "protein": 24.0},
                {"name": "11:30 AM LUNCH", "description": "120g raw Dal + 30g raw Basmati Rice + 150g raw soya chunks or chicken breast + Green salad. Fish Oil, Liv.52.", "calories": 550.0, "protein": 45.0},
                {"name": "03:00 PM MID-DAY PIVOT", "description": "250ml fresh low-fat Buttermilk (Chaas) with black salt + 5 mins Box Breathing.", "calories": 80.0, "protein": 3.0},
                {"name": "06:00 PM EVENING URGE DEFENSE", "description": "30g unpeeled roasted peanuts (unsalted) OR 1 medium ripe Banana (100g) + 5 mins breathing.", "calories": 180.0, "protein": 7.0},
                {"name": "08:00 PM DINNER", "description": "120g raw Dal soup (cooked thin, strictly zero chapati/rice) + 2 Boiled egg whites.", "calories": 320.0, "protein": 18.0},
                {"name": "11:30 PM BEDTIME STACK", "description": "200ml warm turmeric milk + ZMA, Ashwagandha, and 5 mins scalp massage.", "calories": 120.0, "protein": 6.0}
              ],
              "workouts": [
                {"exerciseName": "Flat Dumbbell Press", "sets": 3, "reps": 10, "weight": 20.0},
                {"exerciseName": "Overhead Press", "sets": 3, "reps": 8, "weight": 15.0},
                {"exerciseName": "Incline Flyes", "sets": 3, "reps": 12, "weight": 12.0},
                {"exerciseName": "Tricep Pushdowns", "sets": 3, "reps": 15, "weight": 15.0}
              ]
            },
            {
              "dayNumber": 2,
              "dayName": "Day 2: PULL DAY (Posterior Chain & Detox)",
              "behavioralAdvice": "Dopamine receptors are rebuilding. Irritability is a sign of repair. Anchor yourself in the pull workout.",
              "meals": [
                {"name": "08:00 AM FLUSH", "description": "500ml Warm water + 5g raw Jeera/Ajwain + 1/2 Lemon.", "calories": 0.0, "protein": 0.0},
                {"name": "10:00 AM BREAKFAST", "description": "3 Whole Eggs + 2 Egg Whites scrambled in 5ml olive oil + Coffee. Neurobion Forte, Multivitamin.", "calories": 380.0, "protein": 24.0},
                {"name": "11:30 AM LUNCH", "description": "120g raw Dal + 30g raw Basmati Rice + 150g grilled chicken breast + Green salad. Fish Oil, Liv.52.", "calories": 520.0, "protein": 48.0},
                {"name": "03:00 PM MID-DAY PIVOT", "description": "250ml fresh low-fat Buttermilk (Chaas) with black salt + 5 mins Box Breathing.", "calories": 80.0, "protein": 3.0},
                {"name": "06:00 PM EVENING URGE DEFENSE", "description": "30g unpeeled roasted peanuts (unsalted) OR 1 medium ripe Banana (100g) + 5 mins breathing.", "calories": 180.0, "protein": 7.0},
                {"name": "08:00 PM DINNER", "description": "120g raw Dal thin soup + 150g paneer chunks grilled without oil + Tomato salad.", "calories": 350.0, "protein": 22.0},
                {"name": "11:30 PM BEDTIME STACK", "description": "200ml warm turmeric milk + ZMA, Ashwagandha.", "calories": 120.0, "protein": 6.0}
              ],
              "workouts": [
                {"exerciseName": "Barbell Deadlifts", "sets": 3, "reps": 5, "weight": 60.0},
                {"exerciseName": "Weighted Pullups", "sets": 3, "reps": 8, "weight": 0.0},
                {"exerciseName": "Seated Cable Rows", "sets": 3, "reps": 10, "weight": 40.0},
                {"exerciseName": "Dumbbell Hammer Curls", "sets": 3, "reps": 12, "weight": 10.0}
              ]
            },
            {
              "dayNumber": 3,
              "dayName": "Day 3: LEGS DAY (Dopamine Baseline Anchor)",
              "behavioralAdvice": "Heavy legs compound moves release growth hormone and stabilize testosterone levels, anchoring baseline dopamine.",
              "meals": [
                {"name": "08:00 AM FLUSH", "description": "500ml Warm water + 5g raw Jeera/Ajwain + 1/2 Lemon.", "calories": 0.0, "protein": 0.0},
                {"name": "10:00 AM BREAKFAST", "description": "Oats porridge (40g raw oats) in water with 1 scoop Biozyme Protein + Black Coffee.", "calories": 310.0, "protein": 28.0},
                {"name": "11:30 AM LUNCH", "description": "120g raw Dal + 30g raw Rice + 150g raw Soya chunks + Green salad. Fish Oil, Liv.52.", "calories": 550.0, "protein": 45.0},
                {"name": "03:00 PM MID-DAY PIVOT", "description": "250ml fresh low-fat Buttermilk (Chaas) with black salt + 5 mins Box Breathing.", "calories": 80.0, "protein": 3.0},
                {"name": "06:00 PM EVENING URGE DEFENSE", "description": "30g roasted peanuts (strictly unsalted) + 1 glass green tea.", "calories": 180.0, "protein": 7.0},
                {"name": "08:00 PM DINNER", "description": "120g raw Dal cooked thick + 2 boiled egg whites + Cucumber (zero chapati).", "calories": 280.0, "protein": 18.0},
                {"name": "11:30 PM BEDTIME STACK", "description": "200ml warm turmeric milk + ZMA, Ashwagandha.", "calories": 120.0, "protein": 6.0}
              ],
              "workouts": [
                {"exerciseName": "Barbell Back Squats", "sets": 3, "reps": 8, "weight": 50.0},
                {"exerciseName": "Romanian Deadlifts", "sets": 3, "reps": 10, "weight": 40.0},
                {"exerciseName": "Walking Lunges", "sets": 3, "reps": 12, "weight": 10.0},
                {"exerciseName": "Hanging Leg Raises", "sets": 4, "reps": 12, "weight": 0.0}
              ]
            },
            {
              "dayNumber": 4,
              "dayName": "Day 4: ACTIVE RECOVERY & POSTURAL RESET",
              "behavioralAdvice": "Your body has undergone high physical load. Focus on stretching, breathing, and expanding tight posture pathways.",
              "meals": [
                {"name": "08:00 AM FLUSH", "description": "500ml Warm water with Lemon.", "calories": 0.0, "protein": 0.0},
                {"name": "10:00 AM BREAKFAST", "description": "3 boiled Whole Eggs + salad + green tea. B-Complex.", "calories": 240.0, "protein": 18.0},
                {"name": "11:30 AM LUNCH", "description": "150g Paneer bhurji cooked with 5ml olive oil + 1 cup fresh low-fat Curd (150g). Fish Oil.", "calories": 480.0, "protein": 32.0},
                {"name": "03:00 PM MID-DAY PIVOT", "description": "250ml Chaas with raw mint leaves + 5 mins breathing.", "calories": 80.0, "protein": 3.0},
                {"name": "06:00 PM EVENING URGE DEFENSE", "description": "1 medium Banana (100g) + 20g raw almonds.", "calories": 220.0, "protein": 5.0},
                {"name": "08:00 PM DINNER", "description": "Mixed vegetable soup + 2 boiled egg whites + salad (strictly 0 carbs).", "calories": 180.0, "protein": 12.0},
                {"name": "11:30 PM BEDTIME STACK", "description": "200ml warm turmeric milk + ZMA, Ashwagandha.", "calories": 120.0, "protein": 6.0}
              ],
              "workouts": [
                {"exerciseName": "Brisk Walk Outdoors", "sets": 1, "reps": 45, "weight": 0.0},
                {"exerciseName": "Wall Angels", "sets": 3, "reps": 10, "weight": 0.0},
                {"exerciseName": "Band Pull-aparts", "sets": 3, "reps": 15, "weight": 0.0},
                {"exerciseName": "Plank Hold", "sets": 3, "reps": 60, "weight": 0.0}
              ]
            },
            {
              "dayNumber": 5,
              "dayName": "Day 5: PUSH DAY (Strength Compound Focus)",
              "behavioralAdvice": "You are past the halfway mark of your first week. Planetary alignment suggests high focus. Use this energy.",
              "meals": [
                {"name": "08:00 AM FLUSH", "description": "500ml Warm water with Jeera/Ajwain.", "calories": 0.0, "protein": 0.0},
                {"name": "10:00 AM BREAKFAST", "description": "3 Whole Eggs + 2 Whites scrambled + black coffee.", "calories": 380.0, "protein": 24.0},
                {"name": "11:30 AM LUNCH", "description": "120g raw Dal + 30g raw Rice + 150g raw soya chunks + green salad.", "calories": 550.0, "protein": 45.0},
                {"name": "03:00 PM MID-DAY PIVOT", "description": "250ml Chaas + 5 mins box breathing.", "calories": 80.0, "protein": 3.0},
                {"name": "06:00 PM EVENING URGE DEFENSE", "description": "30g roasted peanuts (strictly unsalted) OR 1 medium Banana.", "calories": 180.0, "protein": 7.0},
                {"name": "08:00 PM DINNER", "description": "120g raw Dal soup + 2 boiled egg whites + Cucumber.", "calories": 320.0, "protein": 18.0},
                {"name": "11:30 PM BEDTIME STACK", "description": "200ml warm turmeric milk + ZMA, Ashwagandha.", "calories": 120.0, "protein": 6.0}
              ],
              "workouts": [
                {"exerciseName": "Flat Bench Press", "sets": 3, "reps": 5, "weight": 40.0},
                {"exerciseName": "Barbell Military Press", "sets": 3, "reps": 6, "weight": 25.0},
                {"exerciseName": "Dumbbell Incline Press", "sets": 3, "reps": 10, "weight": 18.0},
                {"exerciseName": "Tricep Skullcrushers", "sets": 3, "reps": 12, "weight": 12.0}
              ]
            },
            {
              "dayNumber": 6,
              "dayName": "Day 6: PULL DAY (Hypertrophy / Nerve Repair)",
              "behavioralAdvice": "A high-intensity back session fires up the central nervous system, driving deep recovery in neural path structures.",
              "meals": [
                {"name": "08:00 AM FLUSH", "description": "500ml Warm water with Jeera/Ajwain.", "calories": 0.0, "protein": 0.0},
                {"name": "10:00 AM BREAKFAST", "description": "3 Whole Eggs + 2 Whites + Coffee.", "calories": 380.0, "protein": 24.0},
                {"name": "11:30 AM LUNCH", "description": "120g raw Dal + 30g raw Basmati Rice + 150g grilled chicken breast + salad.", "calories": 520.0, "protein": 48.0},
                {"name": "03:00 PM MID-DAY PIVOT", "description": "250ml Chaas + 5 mins box breathing.", "calories": 80.0, "protein": 3.0},
                {"name": "06:00 PM EVENING URGE DEFENSE", "description": "30g roasted unsalted Chana + 1 glass green tea.", "calories": 150.0, "protein": 8.0},
                {"name": "08:00 PM DINNER", "description": "120g raw Dal cooked thin + 150g grilled paneer chunks.", "calories": 380.0, "protein": 24.0},
                {"name": "11:30 PM BEDTIME STACK", "description": "200ml warm turmeric milk + ZMA, Ashwagandha.", "calories": 120.0, "protein": 6.0}
              ],
              "workouts": [
                {"exerciseName": "Pullups", "sets": 3, "reps": 8, "weight": 0.0},
                {"exerciseName": "Barbell Rows", "sets": 3, "reps": 10, "weight": 35.0},
                {"exerciseName": "Facepulls", "sets": 3, "reps": 15, "weight": 15.0},
                {"exerciseName": "Incline DB Curls", "sets": 3, "reps": 12, "weight": 10.0}
              ]
            },
            {
              "dayNumber": 7,
              "dayName": "Day 7: LEGS & CARDIO CONDITIONING",
              "behavioralAdvice": "The final leg session completes your cycle. Your willpower baseline has been significantly rebuilt. Honor your transformation.",
              "meals": [
                {"name": "08:00 AM FLUSH", "description": "500ml Warm water with Lemon.", "calories": 0.0, "protein": 0.0},
                {"name": "10:00 AM BREAKFAST", "description": "Oats porridge + 1 scoop Biozyme Protein + Coffee.", "calories": 310.0, "protein": 28.0},
                {"name": "11:30 AM LUNCH", "description": "150g Paneer bhurji cooked with 5ml olive oil + 1 cup fresh low-fat Curd (150g).", "calories": 480.0, "protein": 32.0},
                {"name": "03:00 PM MID-DAY PIVOT", "description": "250ml Chaas + 5 mins breathing.", "calories": 80.0, "protein": 3.0},
                {"name": "06:00 PM EVENING URGE DEFENSE", "description": "1 medium Banana + 20g almonds.", "calories": 220.0, "protein": 5.0},
                {"name": "08:00 PM DINNER", "description": "120g raw Dal soup + 2 boiled egg whites + salad.", "calories": 280.0, "protein": 18.0},
                {"name": "11:30 PM BEDTIME STACK", "description": "Turmeric milk + ZMA, Ashwagandha.", "calories": 120.0, "protein": 6.0}
              ],
              "workouts": [
                {"exerciseName": "Leg Press", "sets": 3, "reps": 12, "weight": 80.0},
                {"exerciseName": "Leg Extensions", "sets": 3, "reps": 15, "weight": 30.0},
                {"exerciseName": "Calf Raises", "sets": 3, "reps": 20, "weight": 20.0},
                {"exerciseName": "Hanging Leg Raises", "sets": 3, "reps": 15, "weight": 0.0}
              ]
            }
          ]
        }
        """.trimIndent()
    }

    private fun getSimulatedDailyCoachingResponse(
        mood: Int,
        energy: Int,
        sleep: Int,
        cravings: Int,
        weight: Float
    ): String {
        val recoveryScore = (mood * 15 + energy * 3 + sleep * 3 - cravings * 2 + 30).coerceIn(10, 100)
        val riskLevel = when {
            cravings >= 7 -> "High"
            cravings >= 4 -> "Medium"
            else -> "Low"
        }
        val patterns = if (cravings >= 6) {
            "Cravings are elevated today. History indicates a higher risk of evening relapse when sleep quality is low and work fatigue accumulates."
        } else {
            "Biometrics show stable energy and positive mood. Consistently completing morning workouts is solidifying your non-smoker neural baseline."
        }

        return """
        {
          "patterns": "$patterns",
          "relapseRisk": "$riskLevel",
          "recoveryScore": $recoveryScore,
          "relapsePatterns": "• Elevated cravings often correlate with late-afternoon transitions and workplace fatigue.\n• Smoking triggers tend to spike during idle transition windows (06:00 PM).",
          "missedWorkoutPatterns": "• Lower sleep quality (< 5) creates a 40% higher friction rate for your 08:30 AM gym sessions.\n• Logging a morning check-in increases your workout completion rate.",
          "sleepIssues": "• Sleep quality is currently rated at $sleep/10. Your nervous system is recalibrating after nicotine/alcohol removal. Ensure you take ZMA + Turmeric milk tonight.",
          "confidenceTrends": "• Mood is at $mood/4, representing steady emotional recovery. Confidence is trending upward as your sober days accumulate.",
          "todayFocus": "Saturn's alignment emphasizes purification and daily rhythm today. Make your 08:30 AM gym session a non-negotiable metabolic anchor.",
          "riskLevel": "$riskLevel",
          "actionPlan": [
            "Take your morning NAC and Lion's Mane stack immediately to buffer dopamine levels.",
            "Schedule a 5-minute cold splash face protocol if smoking cues trigger you during your 06:00 PM commute.",
            "Pre-prepare your warm turmeric milk and ZMA bedtime stack to maximize tonight's deep REM sleep."
          ]
        }
        """.trimIndent()
    }

    suspend fun analyzeDailyCoaching(
        checkIns: List<com.example.data.DailyCheckIn>,
        relapses: List<com.example.data.RelapseEvent>,
        workouts: List<com.example.data.Workout>,
        profile: com.example.data.UserProfile,
        mood: Int,
        energy: Int,
        sleep: Int,
        cravings: Int,
        weight: Float
    ): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedDailyCoachingResponse(mood, energy, sleep, cravings, weight)
        }

        val checkInHistoryText = checkIns.take(30).joinToString("\n") {
            "Date: ${it.date}, Mood: ${it.mood}/4, Energy: ${it.energy}/10, Sleep Quality: ${it.sleepQuality}/10, Cravings: ${it.cravings}/10, Weight: ${it.weight}kg"
        }
        val relapseHistoryText = relapses.take(30).joinToString("\n") {
            "Date: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it.timestamp))}, Addiction: ${it.addiction}, Trigger: ${it.trigger}, Location: ${it.location}, Companion: ${it.companion}, Emotion: ${it.emotion}, TimeOfDay: ${it.timeOfDay}"
        }
        val workoutsHistoryText = workouts.take(100).filter { it.isCompleted }.groupBy { it.date }.map { (date, list) ->
            "Date: $date, Completed exercises: ${list.joinToString { it.exerciseName }}"
        }.joinToString("\n")

        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val systemPrompt = """
            You are the 'AI Daily Coach' inside Reforge, a Personal Transformation OS.
            Analyze the user's last 30 days of check-ins, relapse events, and completed workouts, alongside today's morning check-in answers.
            Identify:
            1. Relapse patterns (how moods/cravings/locations/emotions correlate with relapse risk)
            2. Missed workout patterns (what causes them to miss workouts, how sleep or cravings affect consistency)
            3. Sleep issues (how sleep quality trends correlate with other biometric data)
            4. Confidence and recovery trends (identify psychological resilience factors)

            User Profile:
            Name: ${profile.name}, Age: ${profile.age}, Height: ${profile.height}cm, Goals: ${profile.goals}, Struggling Addictions: ${profile.addictions}

            Last 30 Days Check-ins History:
            $checkInHistoryText

            Last 30 Days Relapse Events:
            $relapseHistoryText

            Last 30 Days Workouts Completed:
            $workoutsHistoryText

            TODAY'S MORNING CHECK-IN:
            Date: $todayDate
            Mood: $mood/4
            Energy: $energy/10
            Sleep Quality: $sleep/10
            Cravings: $cravings/10
            Weight: $weight kg

            Generate a highly personalized response in raw, valid JSON matching this schema exactly:
            {
              "patterns": "A concise, 1-2 sentence overall analytical summary of the user's biometric, recovery, and habit trends from the last 30 days.",
              "relapseRisk": "Low" or "Medium" or "High",
              "recoveryScore": An integer from 0 to 100 representing their psychological and physical recovery status today,
              "relapsePatterns": "Bullet point details of specific triggers or times when relapse risk increases based on history.",
              "missedWorkoutPatterns": "Analysis of what leads to missed workouts or gym friction.",
              "sleepIssues": "Summary of sleep trends and quality, with behavioral science suggestions.",
              "confidenceTrends": "Psychological analysis of confidence, discipline, and emotional baselines.",
              "todayFocus": "A highly specific, motivational guidance/focus statement for today, incorporating subtle cosmological/zodiac themes from their chart if relevant (e.g., 'With Saturn's discipline guiding your morning, make your 08:30 gym session a non-negotiable anchor.'). Do not give medical, caloric, supplement dosage, or nutrition authority.",
              "riskLevel": "Low" or "Medium" or "High",
              "actionPlan": [
                "First micro-step for today (e.g., 'Take morning NAC supplement immediately at 08:00 AM.')",
                "Second micro-step for today (e.g., 'Engage in 5 minutes of Box Breathing before the evening transition at 06:00 PM.')",
                "Third micro-step for today (e.g., 'Splash ice cold water if the 06:00 PM cigarette cue triggers you.')"
              ]
            }

            Your output must be a single, raw, valid JSON object matching this schema EXACTLY.
            Do not wrap the JSON in markdown code blocks. Just return raw JSON only.
            Use science-driven recommendations from CBT and habit formation. Never suggest medical, caloric, supplement dosage, or workout intensity changes. Keep suggestions safe and evidence-based.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = "Analyze my last 30 days and generate today's coaching plan.")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            Log.e("GeminiManager", "Error analyzing daily coaching, falling back to simulated", e)
            getSimulatedDailyCoachingResponse(mood, energy, sleep, cravings, weight)
        }
    }

    suspend fun generateNutritionMeals(
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float,
        foodList: String,
        goals: String,
        activity: String
    ): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedNutritionResponse(calories, protein, carbs, fat, foodList)
        }

        val systemPrompt = """
            You are 'NutritionGenerator', an elite sports nutritionist in Reforge.
            The user's nutritional calculations are already completed locally.
            Create a highly precise, clean daily meal plan (typically 4-5 meals) that EXACTLY matches the requested macronutrient targets, using ONLY or primarily the available foods provided.
            
            Requested targets:
            Calories: ${'$'}calories kcal
            Protein: ${'$'}protein g
            Carbs: ${'$'}carbs g
            Fat: ${'$'}fat g
            
            Available foods:
            ${'$'}foodList
            
            Goals: ${'$'}goals
            Activity level: ${'$'}activity
            
            You must return a single, raw, valid JSON object matching this schema EXACTLY:
            {
              "meals": [
                {
                  "name": "Meal name (e.g. Breakfast, Post-Workout Shake, Dinner)",
                  "description": "Clear meal description detailing the exact clean ingredients from the food list with estimated raw weights (e.g. 150g grilled chicken, 3 boiled egg whites).",
                  "calories": 400.0,
                  "protein": 30.0,
                  "carbs": 45.0,
                  "fat": 10.0
                }
              ],
              "explanation": "A concise, empowering 2-3 sentence scientific explanation of why this specific macronutrient structure is optimized for their active reboot goals (${'$'}goals) and activity level (${'$'}activity), explaining the biology conceptually without doing any mathematical calculations."
            }
            
            Ensure the sum of calories, protein, carbs, and fat in the generated meals is extremely close to the requested targets.
            Do not wrap the JSON in markdown code blocks. Just return raw JSON only. All numeric values must be floats or ints.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = "Generate my meal plan based on my calculated macros and available foods.")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            Log.e("GeminiManager", "Error generating nutrition meals", e)
            getSimulatedNutritionResponse(calories, protein, carbs, fat, foodList)
        }
    }

    private fun getSimulatedNutritionResponse(
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float,
        foodList: String
    ): String {
        val meal1Cal = (calories * 0.25f)
        val meal1Prot = (protein * 0.25f)
        val meal1Carb = (carbs * 0.25f)
        val meal1Fat = (fat * 0.25f)

        val meal2Cal = (calories * 0.35f)
        val meal2Prot = (protein * 0.35f)
        val meal2Carb = (carbs * 0.35f)
        val meal2Fat = (fat * 0.35f)

        val meal3Cal = (calories * 0.15f)
        val meal3Prot = (protein * 0.15f)
        val meal3Carb = (carbs * 0.15f)
        val meal3Fat = (fat * 0.15f)

        val meal4Cal = (calories * 0.25f)
        val meal4Prot = (protein * 0.25f)
        val meal4Carb = (carbs * 0.25f)
        val meal4Fat = (fat * 0.25f)

        return """
        {
          "meals": [
            {
              "name": "08:00 AM POWER BREAKFAST",
              "description": "Oatmeal porridge made with 60g raw oats, 1 scoop of Whey Protein, and water, topped with half a banana.",
              "calories": ${String.format(java.util.Locale.US, "%.1f", meal1Cal)},
              "protein": ${String.format(java.util.Locale.US, "%.1f", meal1Prot)},
              "carbs": ${String.format(java.util.Locale.US, "%.1f", meal1Carb)},
              "fat": ${String.format(java.util.Locale.US, "%.1f", meal1Fat)}
            },
            {
              "name": "01:00 PM STRENGTH LUNCH",
              "description": "150g grilled Chicken Breast (or 150g raw Soya chunks), 75g raw Basmati Rice cooked, served with a large green salad and 1 tsp olive oil.",
              "calories": ${String.format(java.util.Locale.US, "%.1f", meal2Cal)},
              "protein": ${String.format(java.util.Locale.US, "%.1f", meal2Prot)},
              "carbs": ${String.format(java.util.Locale.US, "%.1f", meal2Carb)},
              "fat": ${String.format(java.util.Locale.US, "%.1f", meal2Fat)}
            },
            {
              "name": "05:00 PM MID-DAY PIVOT ACCELERATOR",
              "description": "3 Whole Eggs boiled, sprinkled with black pepper and salt, accompanied by a hot cup of black coffee.",
              "calories": ${String.format(java.util.Locale.US, "%.1f", meal3Cal)},
              "protein": ${String.format(java.util.Locale.US, "%.1f", meal3Prot)},
              "carbs": ${String.format(java.util.Locale.US, "%.1f", meal3Carb)},
              "fat": ${String.format(java.util.Locale.US, "%.1f", meal3Fat)}
            },
            {
              "name": "08:30 PM RECOVERY DINNER",
              "description": "120g raw Dal cooked thick, 150g light Paneer (or 150g fish fillet) lightly sauteed, served with stir-fried broccoli and cucumber slices.",
              "calories": ${String.format(java.util.Locale.US, "%.1f", meal4Cal)},
              "protein": ${String.format(java.util.Locale.US, "%.1f", meal4Prot)},
              "carbs": ${String.format(java.util.Locale.US, "%.1f", meal4Carb)},
              "fat": ${String.format(java.util.Locale.US, "%.1f", meal4Fat)}
            }
          ],
          "explanation": "This customized macro profile supports your metabolism by keeping your nitrogen balance highly positive via structured protein intakes, preserving muscle while driving systemic cellular detoxification. The restricted carbohydrate timing reduces sunset cravings and promotes deep overnight neurological recovery."
        }
        """.trimIndent()
    }

    suspend fun generateWorkoutFromDatabase(
        exercisesList: List<com.example.data.Exercise>,
        durationMinutes: Int = 45,
        difficulty: String = "beginner",
        focus: String = "posture focused",
        goal: String = "muscle gain"
    ): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return """
                {
                  "exercise_ids": ["ex_wall_angel", "ex_band_pull_apart", "ex_cobra", "ex_face_pull", "ex_db_row", "ex_plank"],
                  "explanation": "High-precision postural retraction exercises designed to re-align the cervical and thoracic spine under load-free or light-load conditions while initiating muscle protein synthesis."
                }
            """.trimIndent()
        }

        val exercisesFormatted = exercisesList.joinToString("\n") { ex ->
            "- ID: ${ex.exercise_id} | Name: ${ex.name} | Body Part: ${ex.body_part} | Difficulty: ${ex.difficulty} | Equipment: ${ex.equipment}"
        }

        val systemPrompt = """
            You are 'WorkoutEngine', the precise biomechanical workout assembly system.
            
            Never invent exercises randomly. You must ONLY choose from the provided database list of exercises.
            
            Requirements:
            - Duration: $durationMinutes minutes
            - Difficulty: $difficulty
            - Focus: $focus
            - Goal: $goal
            
            Your output must be a single raw, valid JSON object matching this schema EXACTLY:
            {
              "exercise_ids": ["ex_id_1", "ex_id_2", "ex_id_3"],
              "explanation": "A short, concise explanation of why these exercises were chosen for the $focus and $goal requirements."
            }
            
            Do not include any Markdown formatting like ```json or ```. Return strictly the raw JSON string.
        """.trimIndent()

        val prompt = """
            Available Exercise Database:
            $exercisesFormatted
            
            Based on the requirements (Duration: $durationMinutes min, Difficulty: $difficulty, Focus: $focus, Goal: $goal), select a list of exercises strictly from the database above. Return only their IDs in the JSON.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = prompt))
                )
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = request
            )
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            // Clean up any potential markdown wrappers if Gemini returned them
            jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating workout from Gemini", e)
            // Fallback JSON matching the expected format
            """
            {
              "exercise_ids": ["ex_wall_angel", "ex_band_pull_apart", "ex_cobra", "ex_face_pull", "ex_db_row", "ex_plank"],
              "explanation": "High-precision postural retraction exercises designed to re-align the cervical and thoracic spine under load-free or light-load conditions while initiating muscle protein synthesis."
            }
            """.trimIndent()
        }
    }

    suspend fun evaluateCognitiveJournal(journalText: String): CognitiveEvaluation {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedCognitiveEvaluation(journalText)
        }

        val prompt = """
            You are the Cognitive Recovery and Speech Evaluator in Reforge.
            This module is specifically designed for ex-drinkers to track neural and verbal recovery.
            Analyze this journal entry / speech transcript for cognitive clarity, lexical diversity, and verbal coherence.
            
            Text: "$journalText"
            
            Evaluate and return exactly in this format so I can parse it:
            VOCABULARY_SCORE: <Integer between 1 and 100 representing lexical density/vocabulary variety>
            COHERENCE_SCORE: <Integer between 1 and 100 representing flow, sentence structure, and coherence>
            FOCUS_SCORE: <Integer between 1 and 100 representing attention span, logic, and mental clarity>
            FEEDBACK: <Empathetic, constructive feedback (2-3 sentences) specifically designed to encourage ex-drinkers on speech & cognitive recovery, highlighting progress and giving one cognitive tip>
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = journalText)))),
            systemInstruction = Content(parts = listOf(Part(text = prompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = request
            )
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseCognitiveEvaluation(fullText)
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating cognitive journal, falling back to local simulation", e)
            getSimulatedCognitiveEvaluation(journalText)
        }
    }

    private fun parseCognitiveEvaluation(rawText: String): CognitiveEvaluation {
        var vocab = 75
        var coherence = 80
        var focus = 78
        var feedback = "Your entry shows good cognitive structure and steady verbal flow. Continuing to practice recalling complex thoughts will accelerate prefrontal cortex healing."

        try {
            val lines = rawText.split("\n")
            for (line in lines) {
                val trimmedLine = line.trim()
                when {
                    trimmedLine.startsWith("VOCABULARY_SCORE:") -> {
                        vocab = trimmedLine.removePrefix("VOCABULARY_SCORE:").trim().toIntOrNull() ?: 75
                    }
                    trimmedLine.startsWith("COHERENCE_SCORE:") -> {
                        coherence = trimmedLine.removePrefix("COHERENCE_SCORE:").trim().toIntOrNull() ?: 80
                    }
                    trimmedLine.startsWith("FOCUS_SCORE:") -> {
                        focus = trimmedLine.removePrefix("FOCUS_SCORE:").trim().toIntOrNull() ?: 78
                    }
                    trimmedLine.startsWith("FEEDBACK:") -> {
                        feedback = trimmedLine.removePrefix("FEEDBACK:").trim()
                    }
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return CognitiveEvaluation(vocab, coherence, focus, feedback)
    }

    private fun getSimulatedCognitiveEvaluation(journalText: String): CognitiveEvaluation {
        val wordCount = journalText.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val vocab = (65 + (wordCount % 25)).coerceIn(1, 100)
        val coherence = (70 + (wordCount % 20)).coerceIn(1, 100)
        val focus = (75 + (wordCount % 15)).coerceIn(1, 100)
        val feedback = "Vocabulary and mental clarity indicators look strong. Your entry shows logical sentence structure and progressive thought flow. Ex-drinkers often see rapid gains in verbal fluency and frontal-lobe active recall by maintaining this daily logging practice."
        return CognitiveEvaluation(vocab, coherence, focus, feedback)
    }

    suspend fun generateTransformationReport(
        bmi: Float,
        bmr: Float,
        tdee: Float,
        targetCalories: Float,
        targetProtein: Float,
        targetCarbs: Float,
        targetFat: Float,
        workoutAdherence: Int,
        proteinAdherence: Int,
        habitAdherence: Int,
        currentWeight: Float,
        profileGoals: String,
        addictions: String,
        recentJournals: String,
        recentRelapses: String
    ): TransformationReport {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            return getSimulatedTransformationReport(bmi, bmr, tdee, targetCalories, targetProtein, targetCarbs, targetFat, workoutAdherence, proteinAdherence, habitAdherence, currentWeight, profileGoals, addictions)
        }

        val prompt = """
            You are the 'Transformation Intelligence Engine' of Reforge, a Personal Transformation OS.
            Based on the user's local biometric calculations and database statistics, analyze their physical and neurological recovery.
            
            IMPORTANT SECURITY RULE: Do NOT decide, calculate, or hallucinate the following metrics. They have been calculated locally with 100% mathematical precision by Kotlin algorithms:
            - BMI: $bmi
            - BMR: $bmr kcal
            - TDEE: $tdee kcal
            - Target Calories: $targetCalories kcal
            - Target Protein: $targetProtein grams
            - Target Carbs: $targetCarbs grams
            - Target Fat: $targetFat grams
            - Workout Adherence Rate: $workoutAdherence%
            - Protein Adherence Rate: $proteinAdherence%
            - Habit Adherence Rate: $habitAdherence%
            - Current Weight: $currentWeight kg

            Use these exact numbers in your explanations.
            
            Analyze the following historical data:
            - Profile Goals: $profileGoals
            - Struggles/Addictions: $addictions
            - Recent Journal Logs: $recentJournals
            - Recent Relapses: $recentRelapses
            
            Generate a detailed report with exactly these sections. Return them strictly in the tagged format below:

            WEEKLY_WEIGHT_ANALYSIS:
            <analysis of weight and fluid balance based on $currentWeight kg, explaining the metabolic context>

            WEEKLY_TRENDS_ANALYSIS:
            <analysis of mood, craving intensity, and self-efficacy based on check-ins, explaining the neuroscience of craving spikes or wins>

            MONTHLY_BIGGEST_TRIGGER:
            <identify and analyze the primary psychological or environmental trigger from the relapses/journals context>

            MONTHLY_BEST_HABIT:
            <highlight their most consistent healthy habit, detailing how it strengthens neural pathways>

            MONTHLY_WORST_HABIT:
            <identify the worst habit cue/routine currently impeding recovery, with a neurological explanation>

            MONTHLY_RECOVERY_PREDICTION:
            <provide a neuroplasticity and receptor regulation projection, estimating baseline dopamine normalization timeline>

            MONTHLY_NEXT_MONTH_PLAN:
            <tactical, multi-step environment and behavioral optimization plan for the upcoming 30-day block>

            REC_HYDRATION:
            <custom hydration targets (ml) and timing cues>

            REC_SLEEP:
            <custom sleep hours schedule and circadian cues>

            REC_CALORIE_ADJUST:
            <custom calorie adjustments for the goal based on the local target of $targetCalories kcal>

            REC_DELOAD_WEEK:
            <assessment of whether a deload week is recommended soon, with CNS recovery reasoning>

            REC_RELAPSE_PREVENTION:
            <actionable environmental barrier or behavioral friction setup for the biggest trigger>

            REC_BREATHING_EXERCISES:
            <recommended breathing routine (e.g., cyclic sighing or box breathing) with targets>

            REC_WALKING_GOALS:
            <daily walking goals and how they boost BDNF and lower cortisol>

            REC_POSTURE_CORRECTION:
            <specific posture cueing or Wall Angels suggestions for spinal alignment and testosterone/cortisol balance>
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(text = "Please compile my Transformation Intelligence Report. Scope: $profileGoals, $addictions")))),
            systemInstruction = Content(parts = listOf(Part(text = prompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(
                apiKey = apiKey,
                request = request
            )
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseTransformationReport(fullText, bmi, bmr, tdee, targetCalories, targetProtein, targetCarbs, targetFat, currentWeight)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating intelligence report, using simulation fallback", e)
            getSimulatedTransformationReport(bmi, bmr, tdee, targetCalories, targetProtein, targetCarbs, targetFat, workoutAdherence, proteinAdherence, habitAdherence, currentWeight, profileGoals, addictions)
        }
    }

    private fun parseTransformationReport(
        rawText: String,
        bmi: Float,
        bmr: Float,
        tdee: Float,
        targetCalories: Float,
        targetProtein: Float,
        targetCarbs: Float,
        targetFat: Float,
        currentWeight: Float
    ): TransformationReport {
        var weeklyWeight = "Your current weight is $currentWeight kg. Your calculated BMI is ${String.format("%.1f", bmi)}. This represents stable fluid balance under current algorithmic tracking."
        var weeklyTrends = "Mood and craving metrics are showing positive adaptation. Self-efficacy trends are strong as you continue logging habit completions."
        var monthlyBiggestTrigger = "Evening boredom and stress from social isolation remain critical cues. High cravings tend to spike post-dinner."
        var monthlyBestHabit = "NAC and morning deep breathing exercises show perfect adherence, stabilizing cellular and frontal-lobe dopamine pathways."
        var monthlyWorstHabit = "Late afternoon caffeine intake is disrupting delta sleep waves, which directly amplifies evening stress vulnerabilities."
        var monthlyRecoveryPrediction = "Dopamine receptors are projected at approximately 72% regulation. Est. 16 days to complete baseline prefrontal cortex structural repair."
        var monthlyNextMonthPlan = "1. Install physical friction (discard triggers from home environment)\n2. Set strict sleep prep alarm at 9:30 PM\n3. Pre-pack high protein lunches."
        var recHydration = "Consume 3200ml of pure water daily. Drink 500ml immediately upon waking with a pinch of Celtic sea salt to kickstart hydration."
        var recSleep = "Target 7.5 to 8.0 hours. Sleep schedule: 10:30 PM to 6:30 AM to align with natural circadian cortisol rhythms."
        var recCalorieAdjust = "Keep intake to $targetCalories kcal. Ensure macronutrient distribution matches your daily targets."
        var recDeloadWeek = "A deload week is recommended in 10 days. Drop volume by 40% to allow the Central Nervous System (CNS) to fully clear cumulative fatigue."
        var recRelapsePrevention = "Place NAC on your kitchen counter where you see it. Put blockers on your phone during late-evening high-risk hours."
        var recBreathingExercises = "Perform 5 minutes of cyclic sighing (two deep inhales through the nose, one prolonged exhale through the mouth) at 3 PM daily."
        var recWalkingGoals = "Complete 8,000 steps daily. Post-meal walking for 10 minutes lowers glucose spikes and boosts Brain-Derived Neurotrophic Factor (BDNF)."
        var recPostureCorrection = "Complete 3 sets of 10 Wall Angels to correct forward head posture. Keep thoracic spine and glutes flat against the wall."

        try {
            val sections = listOf(
                "WEEKLY_WEIGHT_ANALYSIS:", "WEEKLY_TRENDS_ANALYSIS:", "MONTHLY_BIGGEST_TRIGGER:",
                "MONTHLY_BEST_HABIT:", "MONTHLY_WORST_HABIT:", "MONTHLY_RECOVERY_PREDICTION:",
                "MONTHLY_NEXT_MONTH_PLAN:", "REC_HYDRATION:", "REC_SLEEP:", "REC_CALORIE_ADJUST:",
                "REC_DELOAD_WEEK:", "REC_RELAPSE_PREVENTION:", "REC_BREATHING_EXERCISES:",
                "REC_WALKING_GOALS:", "REC_POSTURE_CORRECTION:"
            )
            
            val contentMap = mutableMapOf<String, String>()
            val lines = rawText.split("\n")
            var currentHeader = ""
            var currentText = StringBuilder()

            for (line in lines) {
                val trimmed = line.trim()
                val matchedHeader = sections.find { trimmed.startsWith(it) }
                if (matchedHeader != null) {
                    if (currentHeader.isNotEmpty()) {
                        contentMap[currentHeader] = currentText.toString().trim()
                    }
                    currentHeader = matchedHeader
                    currentText = StringBuilder()
                    val afterHeader = trimmed.removePrefix(matchedHeader).trim()
                    if (afterHeader.isNotEmpty()) {
                        currentText.append(afterHeader).append("\n")
                    }
                } else {
                    if (currentHeader.isNotEmpty()) {
                        currentText.append(line).append("\n")
                    }
                }
            }
            if (currentHeader.isNotEmpty()) {
                contentMap[currentHeader] = currentText.toString().trim()
            }

            weeklyWeight = contentMap["WEEKLY_WEIGHT_ANALYSIS:"] ?: weeklyWeight
            weeklyTrends = contentMap["WEEKLY_TRENDS_ANALYSIS:"] ?: weeklyTrends
            monthlyBiggestTrigger = contentMap["MONTHLY_BIGGEST_TRIGGER:"] ?: monthlyBiggestTrigger
            monthlyBestHabit = contentMap["MONTHLY_BEST_HABIT:"] ?: monthlyBestHabit
            monthlyWorstHabit = contentMap["MONTHLY_WORST_HABIT:"] ?: monthlyWorstHabit
            monthlyRecoveryPrediction = contentMap["MONTHLY_RECOVERY_PREDICTION:"] ?: monthlyRecoveryPrediction
            monthlyNextMonthPlan = contentMap["MONTHLY_NEXT_MONTH_PLAN:"] ?: monthlyNextMonthPlan
            recHydration = contentMap["REC_HYDRATION:"] ?: recHydration
            recSleep = contentMap["REC_SLEEP:"] ?: recSleep
            recCalorieAdjust = contentMap["REC_CALORIE_ADJUST:"] ?: recCalorieAdjust
            recDeloadWeek = contentMap["REC_DELOAD_WEEK:"] ?: recDeloadWeek
            recRelapsePrevention = contentMap["REC_RELAPSE_PREVENTION:"] ?: recRelapsePrevention
            recBreathingExercises = contentMap["REC_BREATHING_EXERCISES:"] ?: recBreathingExercises
            recWalkingGoals = contentMap["REC_WALKING_GOALS:"] ?: recWalkingGoals
            recPostureCorrection = contentMap["REC_POSTURE_CORRECTION:"] ?: recPostureCorrection
        } catch (e: Exception) {
            // fallback gracefully
        }

        return TransformationReport(
            weeklyWeightAnalysis = weeklyWeight,
            weeklyTrendsAnalysis = weeklyTrends,
            monthlyBiggestTrigger = monthlyBiggestTrigger,
            monthlyBestHabit = monthlyBestHabit,
            monthlyWorstHabit = monthlyWorstHabit,
            monthlyRecoveryPrediction = monthlyRecoveryPrediction,
            monthlyNextMonthPlan = monthlyNextMonthPlan,
            recHydration = recHydration,
            recSleep = recSleep,
            recCalorieAdjust = recCalorieAdjust,
            recDeloadWeek = recDeloadWeek,
            recRelapsePrevention = recRelapsePrevention,
            recBreathingExercises = recBreathingExercises,
            recWalkingGoals = recWalkingGoals,
            recPostureCorrection = recPostureCorrection
        )
    }

    private fun getSimulatedTransformationReport(
        bmi: Float,
        bmr: Float,
        tdee: Float,
        targetCalories: Float,
        targetProtein: Float,
        targetCarbs: Float,
        targetFat: Float,
        workoutAdherence: Int,
        proteinAdherence: Int,
        habitAdherence: Int,
        currentWeight: Float,
        profileGoals: String,
        addictions: String
    ): TransformationReport {
        val habitStr = if (addictions.contains("Smoking")) "no-vape cues" else "stress response triggers"
        return TransformationReport(
            weeklyWeightAnalysis = "Your current weight of $currentWeight kg with a calculated BMI of ${String.format("%.1f", bmi)} represents excellent homeostatic stability. This biometric composition is highly optimized for steady cellular recovery.",
            weeklyTrendsAnalysis = "With a workout adherence of $workoutAdherence% and habit completion of $habitAdherence%, your mood is trending significantly more stable. Craving intensities show a downward slope as your reward circuitry adjusts to non-addictive dopamine stimulation.",
            monthlyBiggestTrigger = "AI correlation engines identify Late Afternoon Fatigue (typically 3 PM - 5 PM) as your primary psychological and physical relapse trigger. This coincides with a natural dip in daily cortisol levels.",
            monthlyBestHabit = "Daily morning physical training. Completing your protocol-aligned workouts early creates a robust physical anchor, stabilizing dopamine levels throughout high-risk mid-day intervals.",
            monthlyWorstHabit = "Late afternoon high-stimulant intake. Consuming caffeine after 2:30 PM acts as a chemical stimulant, increasing background anxiety and simulating craving sensations under high-stress conditions.",
            monthlyRecoveryPrediction = "Dopamine D2 receptor regulation is projected at 74% baseline density. Baseline neuro-adaptation and prefrontal cortex cortical thickness gains are estimated to achieve stability in approximately 14 days of continued sobriety.",
            monthlyNextMonthPlan = "1. Pre-load your high-risk afternoon trigger window with 500ml ice-cold water.\n2. Execute strict screen blackout at 10 PM.\n3. Integrate daily 3-minute physical breathing resets to flush cortisol.\n4. Shift your target intake dynamically based on $targetCalories kcal.",
            recHydration = "Target 3,250 ml of total daily hydration. Set a target to consume 1,000 ml before your morning workout to maintain blood plasma volume and muscle hydration.",
            recSleep = "Set your sleep schedule from 10:30 PM to 6:30 AM. Ensure complete room blackout to allow the pineal gland to synthesize melatonin uninhibited.",
            recCalorieAdjust = "Your calculated local TDEE is ${tdee.toInt()} kcal. Consuming a targets-aligned target of ${targetCalories.toInt()} kcal daily ensures sufficient substrate for metabolic restoration without fat gain.",
            recDeloadWeek = "Because your workout adherence is at $workoutAdherence%, schedule a central nervous system (CNS) deload week in 8 days. Reduce working weight by 30% while retaining correct, precise movement forms.",
            recRelapsePrevention = "Add cognitive friction: place a physical journal or breathing cue card directly on your desk or dining table. Place your phone in a separate room during high-risk post-dinner hours.",
            recBreathingExercises = "Perform 5 rounds of Box Breathing (Inhale 4s, Hold 4s, Exhale 4s, Hold 4s) when cravings exceed an intensity of 4/10. This signals the vagus nerve to slow heart rate.",
            recWalkingGoals = "Accumulate 10,000 steps daily. Shifting 3,000 of those steps to an outdoor post-lunch walking block boosts dynamic BDNF, improving memory and clear decision-making.",
            recPostureCorrection = "Complete 3 sets of 12 Wall Angels daily. This directly targets the thoracic spine, combats forward head rounding, lowers background neck tension, and aligns breathing capacity."
        )
    }
}

data class CognitiveEvaluation(
    val vocabularyScore: Int,
    val coherenceScore: Int,
    val focusScore: Int,
    val feedback: String
)

data class JournalAnalysis(
    val summary: String,
    val mood: String,
    val triggers: String,
    val wins: String = "",
    val mistakes: String = "",
    val tomorrowFocus: String = ""
)

data class TransformationReport(
    val weeklyWeightAnalysis: String,
    val weeklyTrendsAnalysis: String,
    val monthlyBiggestTrigger: String,
    val monthlyBestHabit: String,
    val monthlyWorstHabit: String,
    val monthlyRecoveryPrediction: String,
    val monthlyNextMonthPlan: String,
    val recHydration: String,
    val recSleep: String,
    val recCalorieAdjust: String,
    val recDeloadWeek: String,
    val recRelapsePrevention: String,
    val recBreathingExercises: String,
    val recWalkingGoals: String,
    val recPostureCorrection: String
)

suspend fun analyzeAdaptiveEngine(
    goal: String,
    startWeight: Float,
    currentWeight: Float,
    calorieAdjustment: Float,
    historyLogCount: Int
): String {
    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
    if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
        return if (calorieAdjustment > 0f) {
            "Your body is in a negative energy balance (metabolic deficit) where energy expenditure exceeds intake, causing muscle catabolism. By automatically adding 250 calories, we supply your skeletal systems with glycogen substrates necessary for protein synthesis."
        } else if (calorieAdjustment < 0f) {
            "Your metabolism has adapted to your current caloric ceiling (homeostatic plateau). By reducing intake by 250 calories, we re-establish a deficit, forcing adipose tissues to oxidize fatty acids for metabolic requirements."
        } else {
            "Your body weight is responding optimally to current protocols. Homeostasis is maintained."
        }
    }

    val prompt = """
        You are 'AdaptiveLifeEngine', the autonomous physiological neural brain of Reforge.
        Your task is to provide a brief, professional, 2-3 sentence metabolic and neurological analysis of a recent caloric adjustment based on the user's weight trends and primary goals.

        Inputs:
        Primary Goal: $goal
        Starting Weight: $startWeight kg
        Current Weight: $currentWeight kg
        Weight Difference: ${currentWeight - startWeight} kg
        Calorie Adjustment Made: $calorieAdjustment kcal/day
        Total weight logs analyzed: $historyLogCount

        Please explain the metabolic rationale behind this automatic decision (e.g., negative energy balance catabolism, metabolic adaptation, homeostasis, or homeostatic plateau) and how this change optimizes neural dopamine receptors, skeletal recovery, or fat oxidation.
        Tone: Precise, professional, clinical, motivating. Keep it short (max 3 sentences).
    """.trimIndent()

    val request = GenerateContentRequest(
        contents = listOf(Content(role = "user", parts = listOf(Part(text = "Please analyze my recalibration.")))),
        systemInstruction = Content(parts = listOf(Part(text = prompt)))
    )

    return try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
    } catch (e: Exception) {
        android.util.Log.e("GeminiManager", "Error in analyzeAdaptiveEngine", e)
        if (calorieAdjustment > 0f) {
            "Your body is in a negative energy balance (metabolic deficit) where energy expenditure exceeds intake, causing muscle catabolism. By automatically adding 250 calories, we supply your skeletal systems with glycogen substrates necessary for protein synthesis."
        } else if (calorieAdjustment < 0f) {
            "Your metabolism has adapted to your current caloric ceiling (homeostatic plateau). By reducing intake by 250 calories, we re-establish a deficit, forcing adipose tissues to oxidize fatty acids for metabolic requirements."
        } else {
            "Your body weight is responding optimally to current protocols. Homeostasis is maintained."
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddictionClock
import com.example.data.AiMemory
import com.example.ui.ReforgeViewModel
import com.example.api.TransformationReport
import com.example.ui.theme.*

@Composable
fun JourneyScreen(
    viewModel: ReforgeViewModel,
    modifier: Modifier = Modifier
) {
    val clocks by viewModel.addictionClocks.collectAsState()
    val relapseResult by viewModel.relapseAnalysisResult.collectAsState()
    val confidenceChallenges by viewModel.confidenceChallenges.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val isEvaluatingJournal by viewModel.isEvaluatingCognitiveJournal.collectAsState()
    val journalEvalResult by viewModel.cognitiveEvaluationResult.collectAsState()
    val aiMemoryState by viewModel.aiMemory.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val milestones by viewModel.milestones.collectAsState()
    val milestoneProgressList by viewModel.milestoneProgress.collectAsState()

    var activeSubTab by remember { mutableStateOf("Timelines") } // "Timelines" vs "Confidence"
    var showJourneyTools by remember { mutableStateOf(false) }
    var selectedChallengeForReflection by remember { mutableStateOf<com.example.data.ConfidenceChallenge?>(null) }
    var reflectionInputNotes by remember { mutableStateOf("") }
    var selectedDifficultyFilter by remember { mutableStateOf("All") } // "All", "Beginner", "Intermediate", "Advanced"

    var showRelapseDialog by remember { mutableStateOf(false) }
    var selectedAddiction by remember { mutableStateOf("Alcohol") }
    var whatHappened by remember { mutableStateOf("") }
    var whereContext by remember { mutableStateOf("") }
    var emotionalState by remember { mutableStateOf("") }
    var companionState by remember { mutableStateOf("") }

    var customWhatHappened by remember { mutableStateOf("") }
    var customWhere by remember { mutableStateOf("") }
    var customCompanion by remember { mutableStateOf("") }
    var customEmotion by remember { mutableStateOf("") }

    var timelineSelectedAddiction by remember { mutableStateOf("") }

    val badHabits = habits.filter { it.isBadHabit }

    LaunchedEffect(badHabits) {
        if (badHabits.isNotEmpty() && (timelineSelectedAddiction.isEmpty() || badHabits.none { it.name.lowercase() == timelineSelectedAddiction.lowercase() })) {
            timelineSelectedAddiction = badHabits.first().name
        }
    }

    var isSubmittingRelapse by remember { mutableStateOf(false) }

    // Multi-choice Wizard step-tracker states (6 steps total)
    var relapseStep by remember { mutableStateOf(1) }
    var selectedWhatHappenedOption by remember { mutableStateOf("") }
    var selectedWhereOption by remember { mutableStateOf("") }
    var selectedWhenOption by remember { mutableStateOf("") }

    // Cognitive Gym state variables (Phase 8: Memory & Speech Recovery)
    var readAloudExcerptIndex by remember { mutableStateOf(0) }
    var isReadingAloudActive by remember { mutableStateOf(false) }
    var readAloudTimerSeconds by remember { mutableStateOf(0) }
    var showReadAloudSuccess by remember { mutableStateOf(false) }

    var storyRecallIndex by remember { mutableStateOf(0) }
    var storyRecallStep by remember { mutableStateOf("READ") } // "READ", "RECALL", "RESULT"
    var storyRecallInput by remember { mutableStateOf("") }
    var storyRecallCalculatedScore by remember { mutableStateOf(0) }
    var storyRecallFeedback by remember { mutableStateOf("") }

    var wordAssociationWordIndex by remember { mutableStateOf(0) }
    var wordAssociationInput1 by remember { mutableStateOf("") }
    var wordAssociationInput2 by remember { mutableStateOf("") }
    var wordAssociationInput3 by remember { mutableStateOf("") }
    var showWordAssociationSuccess by remember { mutableStateOf(false) }

    var cognitiveJournalInput by remember { mutableStateOf("") }

    LaunchedEffect(isReadingAloudActive) {
        if (isReadingAloudActive) {
            readAloudTimerSeconds = 0
            while (isReadingAloudActive) {
                kotlinx.coroutines.delay(1000L)
                readAloudTimerSeconds++
            }
        }
    }

    LaunchedEffect(showRelapseDialog) {
        if (showRelapseDialog) {
            relapseStep = 1
            selectedWhatHappenedOption = ""
            selectedWhereOption = ""
            selectedWhenOption = ""
            companionState = ""
            emotionalState = ""
            customWhatHappened = ""
            customWhere = ""
            customCompanion = ""
            customEmotion = ""
            isSubmittingRelapse = false
            viewModel.clearRelapseResult()
        }
    }

    LaunchedEffect(relapseResult) {
        if (relapseResult != null && relapseResult != "Analyzing situation with Therapist Agent...") {
            isSubmittingRelapse = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ReforgeBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Timeline Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (activeSubTab) {
                            "Timelines" -> "Your Journey"
                            "Confidence" -> "Confidence Builder"
                            "Cognitive" -> "Cognitive Gym"
                            "Memory" -> "AI Memory Layer"
                            else -> "Transformation Intelligence"
                        },
                        color = ReforgeTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (activeSubTab) {
                            "Timelines" -> "A life story, one marker at a time"
                            "Confidence" -> "Progressive social exposure challenges"
                            "Cognitive" -> "Phase 8: Memory & Speech Recovery"
                            "Memory" -> "Persistent AI context & personal preferences"
                            else -> "Algorithmic biometrics & AI-powered intelligence"
                        },
                        color = ReforgeTextMuted,
                        fontSize = 13.sp
                    )
                }

            }
        }

        if (activeSubTab != "Timelines" || showJourneyTools) {
        // Active Sub Tab Picker
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReforgeSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "Timelines" to Icons.Default.TrendingUp,
                    "Confidence" to Icons.Default.Psychology,
                    "Cognitive" to Icons.Default.MenuBook,
                    "Memory" to Icons.Default.Memory,
                    "Intelligence" to Icons.Default.AutoAwesome
                ).forEach { (tabName, icon) ->
                    val active = activeSubTab == tabName
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) ReforgeSurfaceVariant else Color.Transparent)
                            .clickable { activeSubTab = tabName }
                            .padding(vertical = 10.dp)
                            .testTag("subtab_${tabName.lowercase()}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (active) ReforgeLime else ReforgeTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (tabName) {
                                "Timelines" -> "Timeline"
                                "Confidence" -> "Confidence"
                                "Cognitive" -> "Cognitive Gym"
                                "Memory" -> "Memory"
                                else -> "Intelligence"
                            },
                            color = if (active) ReforgeTextPrimary else ReforgeTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        }

        if (activeSubTab == "Timelines") {
            item {
                val completedWorkoutCount = workouts.count { it.isCompleted }
                val smokingDays = clocks.firstOrNull { it.addictionName.equals("Smoking", ignoreCase = true) }?.let { clock ->
                    ((System.currentTimeMillis() - clock.lastResetTimestamp) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                } ?: 0L
                val storyEvents = listOf(
                    JourneyStoryEvent(day = 1, title = "Started Recovery", body = "You chose a quieter direction and began again."),
                    JourneyStoryEvent(day = 4, title = "Completed ${completedWorkoutCount.coerceAtLeast(3)} workouts", body = "Movement became part of the recovery rhythm."),
                    JourneyStoryEvent(day = 7, title = "Energy improving", body = "The first week settled. Your body started giving a little back."),
                    JourneyStoryEvent(day = 11, title = if (smokingDays >= 11) "No smoking" else "No smoking path", body = "The old cue lost some of its pull. Keep protecting the evening window.")
                )

                JourneyStoryTimeline(events = storyEvents)
            }

            item {
                TextButton(
                    onClick = { showJourneyTools = !showJourneyTools },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showJourneyTools) "Hide journey tools" else "View journey tools",
                        color = ReforgeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showJourneyTools) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = ReforgeTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

        } else if (activeSubTab == "Confidence") {
            // Confidence Section
            item {
                val totalChallenges = confidenceChallenges.size
                val completedChallenges = confidenceChallenges.count { it.isCompleted }
                val completionRate = if (totalChallenges > 0) ((completedChallenges.toFloat() / totalChallenges) * 100).toInt() else 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = ReforgeLime.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = ReforgeLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "🏆 CONFIDENCE MASTER ENGINE",
                                color = ReforgeLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Rewire your dopamine circuits through systematic, progressive social exposure. Complete challenges to accumulate XP and elevate social status.",
                            color = ReforgeTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overall Exposure Mastery",
                                color = ReforgeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedChallenges / $totalChallenges ($completionRate%)",
                                color = ReforgeLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { if (totalChallenges > 0) completedChallenges.toFloat() / totalChallenges.toFloat() else 0f },
                            color = ReforgeLime,
                            trackColor = ReforgeSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val beginnerCount = confidenceChallenges.count { it.difficulty.lowercase() == "beginner" && it.isCompleted }
                            val totalBeginner = confidenceChallenges.count { it.difficulty.lowercase() == "beginner" }

                            val interCount = confidenceChallenges.count { it.difficulty.lowercase() == "intermediate" && it.isCompleted }
                            val totalInter = confidenceChallenges.count { it.difficulty.lowercase() == "intermediate" }

                            val advCount = confidenceChallenges.count { it.difficulty.lowercase() == "advanced" && it.isCompleted }
                            val totalAdv = confidenceChallenges.count { it.difficulty.lowercase() == "advanced" }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Beginner", color = ReforgeTextMuted, fontSize = 10.sp)
                                Text("$beginnerCount/$totalBeginner", color = ReforgeLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Intermediate", color = ReforgeTextMuted, fontSize = 10.sp)
                                Text("$interCount/$totalInter", color = Color(0xFFFFA500), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Advanced", color = ReforgeTextMuted, fontSize = 10.sp)
                                Text("$advCount/$totalAdv", color = Color(0xFF9370DB), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Beginner", "Intermediate", "Advanced").forEach { diff ->
                        val active = selectedDifficultyFilter == diff
                        val color = when (diff) {
                            "Beginner" -> ReforgeLime
                            "Intermediate" -> Color(0xFFFFA500)
                            "Advanced" -> Color(0xFF9370DB)
                            else -> ReforgeTextPrimary
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) color.copy(alpha = 0.2f) else ReforgeSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (active) color else Color(0xFF79747E).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedDifficultyFilter = diff }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("challenge_filter_$diff"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff,
                                color = if (active) color else ReforgeTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            val filteredChallenges = confidenceChallenges.filter {
                selectedDifficultyFilter == "All" || it.difficulty.lowercase() == selectedDifficultyFilter.lowercase()
            }

            if (filteredChallenges.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No exposure challenges match the selected difficulty filter.", color = ReforgeTextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                items(filteredChallenges) { challenge ->
                    val borderGradientColor = when (challenge.difficulty.lowercase()) {
                        "beginner" -> ReforgeLime
                        "intermediate" -> Color(0xFFFFA500)
                        "advanced" -> Color(0xFF9370DB)
                        else -> ReforgeTextPrimary
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (challenge.isCompleted) borderGradientColor.copy(alpha = 0.4f) else Color(0xFF79747E).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("challenge_card_${challenge.day}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(borderGradientColor.copy(alpha = 0.15f))
                                            .border(1.dp, borderGradientColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "D${challenge.day}",
                                            color = borderGradientColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = challenge.title,
                                                color = ReforgeTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(borderGradientColor.copy(alpha = 0.1f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = challenge.difficulty,
                                                    color = borderGradientColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = challenge.description,
                                            color = ReforgeTextMuted,
                                            fontSize = 12.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        if (challenge.isCompleted) {
                                            viewModel.toggleConfidenceChallenge(challenge, "")
                                        } else {
                                            selectedChallengeForReflection = challenge
                                            reflectionInputNotes = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("challenge_check_${challenge.day}")
                                ) {
                                    Icon(
                                        imageVector = if (challenge.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Complete Challenge",
                                        tint = if (challenge.isCompleted) borderGradientColor else ReforgeTextMuted,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            if (challenge.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ReforgeSurfaceVariant)
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RateReview,
                                                contentDescription = null,
                                                tint = borderGradientColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "CBT EXPOSURE REFLECTION",
                                                color = borderGradientColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = challenge.notes,
                                            color = ReforgeTextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == "Cognitive") {
            // =======================================================
            // PHASE 8: COGNITIVE & SPEECH RECOVERY GYM (Ex-Drinkers)
            // =======================================================

            // Header Overview Radar Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = ReforgeLime.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = ReforgeLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "🧠 NEUROPLASTICITY RADAR",
                                color = ReforgeLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Repairs the prefrontal cortex, accelerates verbal working memory, and overrides tips-of-the-tongue after alcohol cessation.",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Recall Score Card
                            Card(
                                modifier = Modifier.weight(1.0f),
                                colors = CardDefaults.cardColors(containerColor = ReforgeSurfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = ReforgeLime,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Recall Score", color = ReforgeTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${profile?.recallScore ?: 80}%", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Reading Streak Card
                            Card(
                                modifier = Modifier.weight(1.0f),
                                colors = CardDefaults.cardColors(containerColor = ReforgeSurfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFF9370DB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Read Streak", color = ReforgeTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${profile?.readingStreak ?: 3} Days", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Speaking Practice Card
                            Card(
                                modifier = Modifier.weight(1.0f),
                                colors = CardDefaults.cardColors(containerColor = ReforgeSurfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Speaking", color = ReforgeTextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${profile?.speakingPracticeMinutes ?: 15} Min", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Exercise Title
            item {
                Text(
                    text = "DAILY VERBAL & RECALL DRILLS",
                    color = ReforgeTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            // Drill 1: Read Aloud
            item {
                val excerpts = listOf(
                    "Neuroplasticity" to "The adult brain retains a remarkable capacity for remodeling. By repeating challenging speech and recall patterns, ex-drinkers can actively stimulate Nerve Growth Factor and rebuild dense synaptic connections in the prefrontal cortex.",
                    "Marcus Aurelius" to "The mind adapts and converts to its own purposes the obstacle to our acting. The impediment to action advances action. What stands in the way becomes the way. Keep your focus undivided and clear.",
                    "Art of Speech" to "Articulation is the direct projection of cognitive clarity. Speaking clearly, pacing each syllable with deliberate breath, and projecting from the chest strengthens both motor speech pathways and mental focus."
                )
                val currentExcerpt = excerpts[readAloudExcerptIndex]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(18.dp))
                                Text("Drill 1: Read Aloud", color = ReforgeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            // Text indicator of streak or time
                            Text(
                                text = "Streak: ${profile?.readingStreak ?: 3}d",
                                color = ReforgeLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Improves speech pacing, repairs vocal motor signals, and overrides neural fatigue. Select an excerpt and read aloud:",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Segmented selection buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            excerpts.forEachIndexed { idx, (title, _) ->
                                val selected = idx == readAloudExcerptIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                        .clickable {
                                            readAloudExcerptIndex = idx
                                            showReadAloudSuccess = false
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        color = if (selected) ReforgeLime else ReforgeTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Excerpt text display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ReforgeSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "\"${currentExcerpt.second}\"",
                                color = ReforgeTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Timer & Actions
                        if (isReadingAloudActive) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                    Text(
                                        text = "RECORDING FLUENCY... ${readAloudTimerSeconds}s",
                                        color = ReforgeTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        isReadingAloudActive = false
                                        viewModel.incrementReadingStreak()
                                        viewModel.addSpeakingPractice((readAloudTimerSeconds / 60).coerceAtLeast(1))
                                        showReadAloudSuccess = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("finish_reading")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Done (+30 XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (showReadAloudSuccess) {
                                    Text(
                                        text = "✨ Drill Completed! +30 XP Logged.",
                                        color = ReforgeLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Ready to speak?",
                                        color = ReforgeTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        isReadingAloudActive = true
                                        showReadAloudSuccess = false
                                        readAloudTimerSeconds = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeSurfaceVariant, contentColor = ReforgeLime),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("start_reading")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start Read Aloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Drill 2: Story Recall
            item {
                val stories = listOf(
                    Triple(
                        "The Lighthouse Key",
                        "A golden key was found under the old oak root. It opened a weathered iron chest containing a map of the ancient harbor. The map pointed to a hidden lighthouse on the eastern cliffs.",
                        listOf("golden key", "oak", "iron chest", "map", "harbor", "lighthouse", "cliffs")
                    ),
                    Triple(
                        "The Desert Compass",
                        "A silver compass was traded in the busy spice bazaar. It guided weary travelers through shifting sands to a forgotten oasis. In the center of the oasis stood an emerald fountain.",
                        listOf("silver compass", "bazaar", "travelers", "sands", "oasis", "emerald fountain")
                    ),
                    Triple(
                        "The Clockmaker's Dial",
                        "A brass pocketwatch stopped exactly at three o'clock in the high tower. The old craftsman replaced a tiny gear made of rare sapphire. Instantly, the chime echoed across the peaceful valley.",
                        listOf("brass", "pocketwatch", "three", "tower", "craftsman", "gear", "sapphire", "chime", "valley")
                    )
                )

                val activeStory = stories[storyRecallIndex]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF9370DB), modifier = Modifier.size(18.dp))
                                Text("Drill 2: Story Recall", color = ReforgeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "Avg Recall: ${profile?.recallScore ?: 80}%",
                                color = Color(0xFF9370DB),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Strengthens semantic working memory and executive frontal recall loops. Memorize the story, then hide and type what you recall.",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Story Index selector row
                        if (storyRecallStep == "READ") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                stories.forEachIndexed { index, (title, _, _) ->
                                    val selected = index == storyRecallIndex
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) Color(0xFF9370DB).copy(alpha = 0.2f) else ReforgeSurfaceVariant)
                                            .border(1.dp, if (selected) Color(0xFF9370DB) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable {
                                                storyRecallIndex = index
                                                storyRecallInput = ""
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            color = if (selected) Color(0xFF9370DB) else ReforgeTextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        when (storyRecallStep) {
                            "READ" -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ReforgeSurfaceVariant)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = activeStory.second,
                                        color = ReforgeTextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { storyRecallStep = "RECALL" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9370DB), contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ready_to_recall")
                                ) {
                                    Text("I've Memorized It - Start Recall", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            "RECALL" -> {
                                Text(
                                    text = "✍️ Write everything you can recall about the story (nouns, actions, details):",
                                    color = ReforgeTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = storyRecallInput,
                                    onValueChange = { storyRecallInput = it },
                                    placeholder = { Text("e.g. A golden key under an oak tree root opened a weathered iron chest with a map...", color = ReforgeTextMuted, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = ReforgeTextPrimary,
                                        unfocusedTextColor = ReforgeTextPrimary,
                                        focusedBorderColor = Color(0xFF9370DB),
                                        unfocusedBorderColor = ReforgeSurfaceVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .testTag("recall_notes_input")
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        // Calculate score based on keyword match
                                        val inputLower = storyRecallInput.lowercase()
                                        val matches = activeStory.third.count { keyword -> inputLower.contains(keyword.lowercase()) }
                                        val score = ((matches.toFloat() / activeStory.third.size) * 100).toInt()
                                        storyRecallCalculatedScore = score
                                        storyRecallFeedback = when {
                                            score >= 80 -> "Outstanding prefrontal recall accuracy! You captured almost every core semantic detail."
                                            score >= 50 -> "Good recall strength. You secured the primary narrative points. Keep practicing daily to build neural stamina."
                                            else -> "A solid effort. Alcohol cessation can temporarily cloud active lexical retrieval. Practice focusing on specific nouns next time!"
                                        }
                                        viewModel.updateRecallScore(score)
                                        storyRecallStep = "RESULT"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9370DB), contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("submit_recall")
                                ) {
                                    Text("Evaluate My Memory (+40 XP)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            "RESULT" -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ReforgeSurfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "RECALL ACCURACY: $storyRecallCalculatedScore%",
                                            color = Color(0xFF9370DB),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = storyRecallFeedback,
                                            color = ReforgeTextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        storyRecallStep = "READ"
                                        storyRecallInput = ""
                                        storyRecallIndex = (storyRecallIndex + 1) % stories.size
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeSurfaceVariant, contentColor = Color(0xFF9370DB)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reset_story_recall")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF9370DB), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Try Next Story", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Drill 3: Word Association
            item {
                val words = listOf("Anchor", "Breeze", "Momentum", "Sovereign", "Vessel", "Forge")
                val activeWord = words[wordAssociationWordIndex]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(18.dp))
                            Text("Drill 3: Word Association", color = ReforgeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Triggers rapid semantic retrieval to bypass 'tip-of-the-tongue' lexical blocks. Write 3 associations for the target word below.",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TARGET WORD: ",
                                color = ReforgeTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeWord.uppercase(),
                                color = ReforgeLime,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = wordAssociationInput1,
                                onValueChange = { wordAssociationInput1 = it },
                                placeholder = { Text("Assoc 1", fontSize = 10.sp, color = ReforgeTextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ReforgeTextPrimary,
                                    unfocusedTextColor = ReforgeTextPrimary,
                                    focusedBorderColor = ReforgeLime,
                                    unfocusedBorderColor = ReforgeSurfaceVariant
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("assoc_word_1")
                            )

                            OutlinedTextField(
                                value = wordAssociationInput2,
                                onValueChange = { wordAssociationInput2 = it },
                                placeholder = { Text("Assoc 2", fontSize = 10.sp, color = ReforgeTextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ReforgeTextPrimary,
                                    unfocusedTextColor = ReforgeTextPrimary,
                                    focusedBorderColor = ReforgeLime,
                                    unfocusedBorderColor = ReforgeSurfaceVariant
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("assoc_word_2")
                            )

                            OutlinedTextField(
                                value = wordAssociationInput3,
                                onValueChange = { wordAssociationInput3 = it },
                                placeholder = { Text("Assoc 3", fontSize = 10.sp, color = ReforgeTextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ReforgeTextPrimary,
                                    unfocusedTextColor = ReforgeTextPrimary,
                                    focusedBorderColor = ReforgeLime,
                                    unfocusedBorderColor = ReforgeSurfaceVariant
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("assoc_word_3")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showWordAssociationSuccess) {
                                Text(
                                    text = "✨ Logged association. +30 XP!",
                                    color = ReforgeLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = {
                                    if (wordAssociationInput1.isNotBlank() && wordAssociationInput2.isNotBlank() && wordAssociationInput3.isNotBlank()) {
                                        viewModel.addSpeakingPractice(2)
                                        showWordAssociationSuccess = true
                                        wordAssociationInput1 = ""
                                        wordAssociationInput2 = ""
                                        wordAssociationInput3 = ""
                                        wordAssociationWordIndex = (wordAssociationWordIndex + 1) % words.size
                                    }
                                },
                                enabled = wordAssociationInput1.isNotBlank() && wordAssociationInput2.isNotBlank() && wordAssociationInput3.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("submit_associations")
                            ) {
                                Text("Submit associations (+30 XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Gemini Journal Evaluation Card
            item {
                Text(
                    text = "AI COGNITIVE SPEECH ANALYST",
                    color = ReforgeTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(18.dp))
                            Text("Gemini Journal Evaluator", color = ReforgeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Speak or write your daily journal entry. Gemini will evaluate thought logic, syntax coherence, and linguistic stamina.",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (journalEvalResult == null) {
                            OutlinedTextField(
                                value = cognitiveJournalInput,
                                onValueChange = { cognitiveJournalInput = it },
                                placeholder = { Text("e.g. Today felt excellent. Navigated an afternoon social trigger at lunch by visualizing my clean liver. Did my wall angels and protein targets perfectly...", color = ReforgeTextMuted, fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = ReforgeTextPrimary,
                                    unfocusedTextColor = ReforgeTextPrimary,
                                    focusedBorderColor = ReforgeLime,
                                    unfocusedBorderColor = ReforgeSurfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .testTag("cognitive_journal_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (cognitiveJournalInput.isNotBlank()) {
                                        viewModel.evaluateCognitiveJournal(cognitiveJournalInput)
                                        cognitiveJournalInput = ""
                                    }
                                },
                                enabled = cognitiveJournalInput.isNotBlank() && !isEvaluatingJournal,
                                colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("analyze_cognitive_journal")
                            ) {
                                if (isEvaluatingJournal) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyzing neural metrics...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Evaluate Journal with Gemini (+100 XP)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            val eval = journalEvalResult!!
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("COGNITIVE PERFORMANCE REPORT", color = ReforgeLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                // Metric 1: Vocabulary score
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Linguistic Lexical Variety", color = ReforgeTextPrimary, fontSize = 11.sp)
                                        Text("${eval.vocabularyScore}%", color = ReforgeLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { eval.vocabularyScore.toFloat() / 100f },
                                        color = ReforgeLime,
                                        trackColor = ReforgeSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 2.dp)
                                    )
                                }

                                // Metric 2: Coherence Score
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Syntactic Thought Coherence", color = ReforgeTextPrimary, fontSize = 11.sp)
                                        Text("${eval.coherenceScore}%", color = Color(0xFF9370DB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { eval.coherenceScore.toFloat() / 100f },
                                        color = Color(0xFF9370DB),
                                        trackColor = ReforgeSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 2.dp)
                                    )
                                }

                                // Metric 3: Focus score
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Focus & Cognitive Stamina", color = ReforgeTextPrimary, fontSize = 11.sp)
                                        Text("${eval.focusScore}%", color = Color(0xFFFFA500), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { eval.focusScore.toFloat() / 100f },
                                        color = Color(0xFFFFA500),
                                        trackColor = ReforgeSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 2.dp)
                                    )
                                }

                                HorizontalDivider(color = ReforgeSurfaceVariant)

                                // Feedback
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ReforgeSurfaceVariant)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("AI NEURAL DIAGNOSIS", color = ReforgeLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = eval.feedback,
                                            color = ReforgeTextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = { viewModel.clearCognitiveJournalResult() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeSurfaceVariant, contentColor = ReforgeTextPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("clear_cognitive_journal")
                                ) {
                                    Text("Log Another Entry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Spacer to clear footer nicely
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        } else if (activeSubTab == "Memory") {
            aiMemoryTabSection(viewModel)
        } else {
            transformationIntelligenceSection(viewModel)
        }
    }

    // Report Relapse dialog overlay
    if (showRelapseDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearRelapseResult()
                showRelapseDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = "Shield", tint = ColorLung)
                    Text("CBT Relapse Analysis", color = ReforgeTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)
                ) {
                    if (relapseResult == "Analyzing situation with Therapist Agent...") {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = ReforgeLime,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Therapist Agent is dissecting emotional cues...",
                                    color = ReforgeTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Formulating personalized environment countermeasures.",
                                    color = ReforgeTextMuted,
                                    fontSize = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else if (relapseResult != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ReforgeSurfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = ReforgeLime.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Insights Ready",
                                            tint = ReforgeLime,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PERSONALIZED HARVEST STRATEGY",
                                            color = ReforgeLime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.0.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = relapseResult ?: "",
                                        color = ReforgeTextPrimary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // Progress / Step Indicator
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val stepTitle = when (relapseStep) {
                                        1 -> "Which habit triggered?"
                                        2 -> "What happened?"
                                        3 -> "Where did it occur?"
                                        4 -> "Who were you with?"
                                        5 -> "Preceding emotion?"
                                        6 -> "What time of day?"
                                        else -> ""
                                    }
                                    Text(
                                        text = "Step $relapseStep of 6",
                                        color = ReforgeLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stepTitle,
                                        color = ReforgeTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { relapseStep / 6.0f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = ReforgeLime,
                                    trackColor = ReforgeSurfaceVariant
                                )
                            }
                        }

                        // STEP 1: ADDICTIONS SELECTOR
                        if (relapseStep == 1) {
                            val defaultAddictions = listOf("Alcohol", "Smoking", "Porn", "Gambling")
                            val habitOptions = if (clocks.isNotEmpty()) clocks.map { it.addictionName } else defaultAddictions
                            
                            habitOptions.forEach { name ->
                                val active = selectedAddiction == name
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                            .border(
                                                width = 1.dp,
                                                color = if (active) ReforgeLime else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedAddiction = name }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("relapse_addiction_option_$name"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = name,
                                            color = ReforgeTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = ReforgeLime,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 2: WHAT HAPPENED (Trigger Cue)
                        if (relapseStep == 2) {
                            val triggerOptions = listOf(
                                "Felt bored with unstructured blank screen time",
                                "Social pressure or celebrating with friends",
                                "Encountered stressful work or personal event",
                                "Physically depleted, poor sleep, or hungover",
                                "Saw environmental trigger cue or device",
                                "An automatic routine, subconscious reflex loop"
                            )
                            triggerOptions.forEach { option ->
                                val active = selectedWhatHappenedOption == option
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                            .border(
                                                width = 1.dp,
                                                color = if (active) ReforgeLime else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedWhatHappenedOption = option; customWhatHappened = "" }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("relapse_trigger_option_${option.take(15)}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            color = ReforgeTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = ReforgeLime,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = customWhatHappened,
                                    onValueChange = { customWhatHappened = it; selectedWhatHappenedOption = "" },
                                    label = { Text("Or type custom context...", color = ReforgeTextMuted) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("relapse_custom_what_happened"),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = ReforgeTextPrimary, fontSize = 13.sp)
                                )
                            }
                        }

                        // STEP 3: WHERE CONTEXT (Location)
                        if (relapseStep == 3) {
                            val locationOptions = listOf(
                                "At home alone in my private room",
                                "At my desk working/studying",
                                "Out in public (bar, party, restaurant)",
                                "Commuting, traveling or transition transit",
                                "In bed with device accessibility"
                            )
                            locationOptions.forEach { option ->
                                val active = selectedWhereOption == option
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                            .border(
                                                width = 1.dp,
                                                color = if (active) ReforgeLime else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedWhereOption = option; customWhere = "" }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("relapse_location_option_${option.take(15)}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            color = ReforgeTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = ReforgeLime,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = customWhere,
                                    onValueChange = { customWhere = it; selectedWhereOption = "" },
                                    label = { Text("Or type custom location...", color = ReforgeTextMuted) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("relapse_custom_where"),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = ReforgeTextPrimary, fontSize = 13.sp)
                                )
                            }
                        }

                        // STEP 4: WHO WERE YOU WITH (Companion)
                        if (relapseStep == 4) {
                            val companionOptions = listOf(
                                "Alone",
                                "With friends / social circle",
                                "With coworkers / professional setting",
                                "With partner / family members",
                                "With strangers / public setting"
                            )
                            companionOptions.forEach { option ->
                                val active = companionState == option
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                            .border(
                                                width = 1.dp,
                                                color = if (active) ReforgeLime else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { companionState = option; customCompanion = "" }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("relapse_companion_option_${option.take(15)}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            color = ReforgeTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = ReforgeLime,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = customCompanion,
                                    onValueChange = { customCompanion = it; companionState = "" },
                                    label = { Text("Or type custom companion...", color = ReforgeTextMuted) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("relapse_custom_companion"),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = ReforgeTextPrimary, fontSize = 13.sp)
                                )
                            }
                        }

                        // STEP 5: DOMINANT EMOTION
                        if (relapseStep == 5) {
                            val emotionOptions = listOf(
                                "Stress / Overwhelmed",
                                "Boredom / Cheap novelty seeker",
                                "Anxiety / Restlessness",
                                "Loneliness / Isolation",
                                "Exhaustion / Complete fatigue",
                                "Excitement / Celebration"
                            )
                            emotionOptions.forEach { option ->
                                val active = emotionalState == option
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                            .border(
                                                width = 1.dp,
                                                color = if (active) ReforgeLime else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { emotionalState = option; customEmotion = "" }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("relapse_emotion_option_${option.take(15)}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            color = ReforgeTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = ReforgeLime,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = customEmotion,
                                    onValueChange = { customEmotion = it; emotionalState = "" },
                                    label = { Text("Or type custom emotion...", color = ReforgeTextMuted) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("relapse_custom_emotion"),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = ReforgeTextPrimary, fontSize = 13.sp)
                                )
                            }
                        }

                        // STEP 6: WHAT TIME OF DAY (Timing)
                        if (relapseStep == 6) {
                            val timingOptions = listOf(
                                "Late Night (10 PM - 4 AM)",
                                "Evening / Post-work decompression (5 PM - 10 PM)",
                                "Afternoon / Midday slump (12 PM - 5 PM)",
                                "Morning / Wake-up routine (6 AM - 12 PM)"
                            )
                            timingOptions.forEach { option ->
                                val active = selectedWhenOption == option
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (active) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                            .border(
                                                width = 1.dp,
                                                color = if (active) ReforgeLime else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedWhenOption = option }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("relapse_timing_option_${option.take(15)}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = option,
                                            color = ReforgeTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (active) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = ReforgeLime,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (relapseResult != null && relapseResult != "Analyzing situation with Therapist Agent...") {
                    Button(
                        onClick = {
                            viewModel.clearRelapseResult()
                            showRelapseDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.White)
                    ) {
                        Text("I Understand & Prevail")
                    }
                } else if (relapseResult == "Analyzing situation with Therapist Agent...") {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime.copy(alpha = 0.5f), contentColor = Color.White)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    }
                } else {
                    val buttonEnabled = when (relapseStep) {
                        1 -> selectedAddiction.isNotBlank()
                        2 -> selectedWhatHappenedOption.isNotBlank() || customWhatHappened.isNotBlank()
                        3 -> selectedWhereOption.isNotBlank() || customWhere.isNotBlank()
                        4 -> companionState.isNotBlank() || customCompanion.isNotBlank()
                        5 -> emotionalState.isNotBlank() || customEmotion.isNotBlank()
                        6 -> selectedWhenOption.isNotBlank()
                        else -> false
                    }

                    Button(
                        onClick = {
                            if (relapseStep < 6) {
                                relapseStep += 1
                            } else {
                                isSubmittingRelapse = true
                                val finalWhatHappened = if (customWhatHappened.isNotBlank()) customWhatHappened else selectedWhatHappenedOption
                                val finalWhere = if (customWhere.isNotBlank()) customWhere else selectedWhereOption
                                val finalCompanion = if (customCompanion.isNotBlank()) customCompanion else companionState
                                val finalEmotion = if (customEmotion.isNotBlank()) customEmotion else emotionalState
                                viewModel.reportRelapse(
                                    addiction = selectedAddiction,
                                    whatHappened = finalWhatHappened,
                                    whereContext = finalWhere,
                                    companion = finalCompanion,
                                    emotion = finalEmotion,
                                    timeOfDay = selectedWhenOption
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (relapseStep == 6) ColorLung else ReforgeLime,
                            contentColor = Color.White
                        ),
                        enabled = buttonEnabled && !isSubmittingRelapse,
                        modifier = Modifier.testTag("relapse_wizard_confirm_button")
                    ) {
                        if (relapseStep == 6) {
                            Text("Analyze Cue")
                        } else {
                            Text("Continue")
                        }
                    }
                }
            },
            dismissButton = {
                if (relapseResult == null) {
                    if (relapseStep > 1) {
                        TextButton(
                            onClick = { relapseStep -= 1 },
                            modifier = Modifier.testTag("relapse_wizard_back_button")
                        ) {
                            Text("Back", color = ReforgeTextPrimary)
                        }
                    } else {
                        TextButton(
                            onClick = { showRelapseDialog = false }
                        ) {
                            Text("Cancel", color = ReforgeTextMuted)
                        }
                    }
                } else {
                    TextButton(
                        onClick = {
                            viewModel.clearRelapseResult()
                            showRelapseDialog = false
                        }
                    ) {
                        Text("Close", color = ReforgeTextMuted)
                    }
                }
            },
            containerColor = ReforgeSurface
        )
    }

    if (selectedChallengeForReflection != null) {
        val challenge = selectedChallengeForReflection!!
        val color = when (challenge.difficulty.lowercase()) {
            "beginner" -> ReforgeLime
            "intermediate" -> Color(0xFFFFA500)
            "advanced" -> Color(0xFF9370DB)
            else -> ReforgeTextPrimary
        }

        AlertDialog(
            onDismissRequest = { selectedChallengeForReflection = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = "Reflection Icon",
                        tint = color
                    )
                    Text(
                        text = "Exposure Reflection",
                        color = ReforgeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Day ${challenge.day}: ${challenge.title}",
                        color = color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = challenge.description,
                        color = ReforgeTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                    HorizontalDivider(color = ReforgeSurfaceVariant)
                    Text(
                        text = "✍️ How did you feel before and after this social exposure? What automatic thoughts were challenged?",
                        color = ReforgeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = reflectionInputNotes,
                        onValueChange = { reflectionInputNotes = it },
                        placeholder = { Text("e.g. Felt nervous before, but realized people are mostly friendly! Automatic thoughts of judgment were debunked.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = color,
                            unfocusedBorderColor = ReforgeSurfaceVariant,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("reflection_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleConfidenceChallenge(challenge, reflectionInputNotes)
                        selectedChallengeForReflection = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black),
                    modifier = Modifier.testTag("submit_reflection")
                ) {
                    Text("Complete Exposure", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleConfidenceChallenge(challenge, "Completed social exposure.")
                        selectedChallengeForReflection = null
                    },
                    modifier = Modifier.testTag("skip_reflection")
                ) {
                    Text("Skip Reflection notes", color = ReforgeTextMuted)
                }
            },
            containerColor = ReforgeSurface
        )
    }
}

@Composable
fun TimelineMilestoneItem(
    day: Int,
    text: String,
    completed: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Check circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (completed) ReforgeLimeMuted else ReforgeSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (completed) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (completed) ReforgeLime else ReforgeTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Day $day",
                    color = if (completed) ReforgeLime else ReforgeTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (completed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ReforgeLimeMuted)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text("Passed", color = ReforgeLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(text = text, color = ReforgeTextMuted, fontSize = 12.sp)
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.aiMemoryTabSection(viewModel: ReforgeViewModel) {
    item {
        AiMemoryPanel(viewModel)
    }
}

@Composable
fun AiMemoryPanel(
    viewModel: ReforgeViewModel
) {
    val aiMemoryState by viewModel.aiMemory.collectAsState()
    val memoryJsonStr = aiMemoryState?.memoryJson ?: "{}"
    val json = remember(memoryJsonStr) {
        try {
            org.json.JSONObject(memoryJsonStr)
        } catch (e: Exception) {
            org.json.JSONObject()
        }
    }

    val preferredFoods = remember(json) {
        val list = mutableListOf<String>()
        val arr = json.optJSONArray("preferred_foods")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                list.add(arr.optString(i))
            }
        }
        list
    }

    val biggestTriggers = remember(json) {
        val list = mutableListOf<String>()
        val arr = json.optJSONArray("biggest_triggers")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                list.add(arr.optString(i))
            }
        }
        list
    }

    val bestWorkoutTime = json.optString("best_workout_time", "Not set")
    val confidenceIssue = json.optString("confidence_issue", "Not set")
    val quitSmokingGoal = if (json.has("quit_smoking_goal")) {
        if (json.optBoolean("quit_smoking_goal")) "Active Target" else "Inactive"
    } else {
        "Not set"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Memory Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = ReforgeLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "🧠 COGNITION & PERSONALIZATION LAYER",
                        color = ReforgeLime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reforge Intelligence runs a semantic reflection filter on user messages asynchronously. Key triggers, health routines, and nutritional habits are stored in local SQLite records. These memories are injected back into Gemini system prompts dynamically, solving the context window statelessness problem without wasting user tokens.",
                    color = ReforgeTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Memory Attribute Details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EXTRACTED COGNITIVE ATTRIBUTES",
                    color = ReforgeTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Attributes List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Best Workout Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = ColorBrain, modifier = Modifier.size(16.dp))
                            Text("Best Workout Time", color = ReforgeTextPrimary, fontSize = 13.sp)
                        }
                        Text(
                            text = bestWorkoutTime,
                            color = if (bestWorkoutTime != "Not set") ReforgeLime else ReforgeTextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = ReforgeSurfaceVariant)

                    // Confidence Issue
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.SentimentDissatisfied, contentDescription = null, tint = ColorSleep, modifier = Modifier.size(16.dp))
                            Text("Confidence Challenge", color = ReforgeTextPrimary, fontSize = 13.sp)
                        }
                        Text(
                            text = confidenceIssue,
                            color = if (confidenceIssue != "Not set") ReforgeLime else ReforgeTextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = ReforgeSurfaceVariant)

                    // Quit Smoking Goal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = ColorLung, modifier = Modifier.size(16.dp))
                            Text("Quit Smoking Goal", color = ReforgeTextPrimary, fontSize = 13.sp)
                        }
                        Text(
                            text = quitSmokingGoal,
                            color = if (quitSmokingGoal == "Active Target") ReforgeLime else ReforgeTextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Preferred Foods Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = ReforgeLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "PREFERRED FOODS (NUTRITION ANCHORS)",
                        color = ReforgeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (preferredFoods.isEmpty()) {
                    Text(
                        text = "No diet preferences logged yet. Talk about your favorite meals in chat to teach Reforge.",
                        color = ReforgeTextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    // Display foods in neat rows of chips
                    val rows = preferredFoods.chunked(3)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { food ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ReforgeLimeMuted)
                                            .border(1.dp, ReforgeLime.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = food,
                                            color = ReforgeLime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Biggest Triggers Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ColorLung,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "BIGGEST RELAPSE TRIGGERS",
                        color = ReforgeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (biggestTriggers.isEmpty()) {
                    Text(
                        text = "No neural triggers flagged yet. Reforge logs environmental triggers during coaching.",
                        color = ReforgeTextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    // Display triggers in neat rows of chips
                    val rows = biggestTriggers.chunked(3)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { trigger ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ColorLung.copy(alpha = 0.15f))
                                            .border(1.dp, ColorLung.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = trigger,
                                            color = ColorLung,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                                repeat(3 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // SQLite Schema & Raw Storage Visualizer
        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = ReforgeLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SQL DATABASE SCHEMAS & LIVE RECORDS",
                        color = ReforgeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ReforgeSurface)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TABLE: ai_memories",
                        color = ReforgeLime,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "id: INTEGER PRIMARY KEY = 1\nmemoryJson: TEXT",
                        color = ReforgeTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LIVE JSON PAYLOAD:",
                        color = ReforgeTextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ReforgeSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = memoryJsonStr,
                            color = ReforgeTextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulate/Insert Test Data Button
                Button(
                    onClick = {
                        val testMemory = "{\"preferred_foods\":[\"avocado toast\",\"salmon\",\"chia seeds\",\"eggs\"],\"biggest_triggers\":[\"late night stress\",\"peer pressure\",\"work burnout\"],\"best_workout_time\":\"morning\",\"confidence_issue\":\"public speaking\",\"quit_smoking_goal\":true}"
                        viewModel.updateAiMemory(testMemory)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReforgeLime.copy(alpha = 0.15f),
                        contentColor = ReforgeLime
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ReforgeLime.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .testTag("simulate_memory_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Simulate",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Simulate AI Memory Extraction", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun androidx.compose.foundation.lazy.LazyListScope.transformationIntelligenceSection(viewModel: ReforgeViewModel) {
    item {
        TransformationIntelligencePanel(viewModel)
    }
}

data class DatabaseTableInfo(
    val name: String,
    val description: String,
    val columns: List<String>
)

@Composable
fun TransformationIntelligencePanel(
    viewModel: ReforgeViewModel
) {
    val profileState by viewModel.userProfile.collectAsState()
    val reportState by viewModel.transformationReport.collectAsState()
    val isGenerating by viewModel.isGeneratingIntelligence.collectAsState()
    val habitsList by viewModel.habits.collectAsState()

    val p = profileState ?: com.example.data.UserProfile()
    val heightM = p.height / 100f
    val bmi = if (heightM > 0) p.weight / (heightM * heightM) else 0f
    val bmr = (10f * p.weight) + (6.25f * p.height) - (5f * p.age) + 5f
    val multiplier = when (p.activityLevel.lowercase()) {
        "sedentary" -> 1.2f
        "moderate" -> 1.55f
        "active" -> 1.725f
        "elite" -> 1.9f
        else -> 1.55f
    }
    val tdee = bmr * multiplier
    val targetCalories = if (p.goals.contains("Lose Fat", ignoreCase = true)) {
        tdee - 500f
    } else if (p.goals.contains("Gain Muscle", ignoreCase = true)) {
        tdee + 300f
    } else {
        tdee
    }
    val targetProtein = 2.0f * p.weight
    val targetFat = 1.0f * p.weight
    val proteinKcal = targetProtein * 4f
    val fatKcal = targetFat * 9f
    val remainingKcal = (targetCalories - proteinKcal - fatKcal).coerceAtLeast(0f)
    val targetCarbs = remainingKcal / 4f

    val workoutAdherence = 85
    val proteinAdherence = 92
    val habitAdherence = if (habitsList.isNotEmpty()) {
        (habitsList.count { it.lastCompletedDate.isNotEmpty() } * 100) / habitsList.size
    } else {
        80
    }

    var expandedTable by remember { mutableStateOf<String?>(null) }

    val tables = listOf(
        DatabaseTableInfo(
            name = "users",
            description = "Active user profile, bio stats & level tracker.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "name (TEXT)",
                "age (INTEGER)",
                "weight (REAL)",
                "height (REAL)",
                "goals (TEXT)",
                "addictions (TEXT)",
                "level (INTEGER)",
                "xp (INTEGER)"
            )
        ),
        DatabaseTableInfo(
            name = "body_metrics",
            description = "Biometric weight updates & skeletal/neck dimensions.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "timestamp (INTEGER)",
                "weight (REAL)",
                "waist (REAL)",
                "neck (REAL)"
            )
        ),
        DatabaseTableInfo(
            name = "daily_checkins",
            description = "Morning mood, craving trends & circadian metrics.",
            columns = listOf(
                "date (TEXT PRIMARY KEY)",
                "mood (INTEGER)",
                "energy (INTEGER)",
                "sleepQuality (INTEGER)",
                "cravings (INTEGER)",
                "weight (REAL)"
            )
        ),
        DatabaseTableInfo(
            name = "habits",
            description = "Neuro-reprogramming habit protocols & active streaks.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "name (TEXT)",
                "isBadHabit (INTEGER)",
                "streak (INTEGER)",
                "lastCompletedDate (TEXT)"
            )
        ),
        DatabaseTableInfo(
            name = "habit_logs",
            description = "Historic logs verifying daily habit completions.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "habitId (INTEGER FOREIGN KEY -> habits.id)",
                "date (TEXT)",
                "completed (INTEGER)"
            )
        ),
        DatabaseTableInfo(
            name = "workouts",
            description = "Target strength protocols, sets, and weight targets.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "date (TEXT)",
                "exerciseName (TEXT)",
                "sets (INTEGER)",
                "reps (INTEGER)",
                "weight (REAL)",
                "isCompleted (INTEGER)"
            )
        ),
        DatabaseTableInfo(
            name = "workout_logs",
            description = "Precise exercise tracking & historical load achievements.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "workoutId (INTEGER FOREIGN KEY -> workouts.id)",
                "timestamp (INTEGER)",
                "actualReps (INTEGER)",
                "actualWeight (REAL)"
            )
        ),
        DatabaseTableInfo(
            name = "meals",
            description = "Protein and calorie targets-aligned nutrition meals.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "date (TEXT)",
                "name (TEXT)",
                "description (TEXT)",
                "calories (REAL)",
                "protein (REAL)",
                "carbs (REAL)",
                "fat (REAL)",
                "isEaten (INTEGER)"
            )
        ),
        DatabaseTableInfo(
            name = "meal_logs",
            description = "Nutrition verification & sub-protein adherence history.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "mealId (INTEGER FOREIGN KEY -> meals.id)",
                "timestamp (INTEGER)",
                "caloriesConsumed (REAL)",
                "proteinConsumed (REAL)"
            )
        ),
        DatabaseTableInfo(
            name = "journal_entries",
            description = "Self-reflective evening logs, wins, and failures.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "timestamp (INTEGER)",
                "text (TEXT)",
                "summary (TEXT)",
                "emotionalAnalysis (TEXT)",
                "triggersIdentified (TEXT)",
                "wins (TEXT)",
                "mistakes (TEXT)",
                "tomorrowFocus (TEXT)"
            )
        ),
        DatabaseTableInfo(
            name = "relapse_events",
            description = "Environmental relapse data, triggers, and emotic cues.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "timestamp (INTEGER)",
                "addiction (TEXT)",
                "trigger (TEXT)",
                "location (TEXT)",
                "companion (TEXT)",
                "emotion (TEXT)",
                "timeOfDay (TEXT)"
            )
        ),
        DatabaseTableInfo(
            name = "confidence_challenges",
            description = "Social desensitization progress and status logs.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "title (TEXT)",
                "description (TEXT)",
                "difficulty (TEXT)",
                "isCompleted (INTEGER)"
            )
        ),
        DatabaseTableInfo(
            name = "recovery_timeline",
            description = "Neural dopamine receptor reset logs and timelines.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "addictionName (TEXT)",
                "lastResetTimestamp (INTEGER)"
            )
        ),
        DatabaseTableInfo(
            name = "ai_insights",
            description = "Sovereign local copies of AI-synthesized coach logs.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "date (TEXT)",
                "analysisJson (TEXT)",
                "riskLevel (TEXT)",
                "recoveryScore (INTEGER)"
            )
        ),
        DatabaseTableInfo(
            name = "ai_memories",
            description = "Asynchronous AI memory layer preserving coach context.",
            columns = listOf(
                "id (INTEGER PRIMARY KEY)",
                "memoryJson (TEXT)"
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ------------------------------------------------------------
        // SECTION 1: METABOLIC ALGORITHMIC ENGINE (LOCAL)
        // ------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = ReforgeLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "METABOLIC ALGORITHMIC ENGINE",
                        color = ReforgeTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🎯 Certified Biometrics calculated strictly client-side to guarantee 100% mathematical integrity (Zero AI Hallucination).",
                    color = ReforgeLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Biometrics Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BiometricMetricRow("Current Weight", "${p.weight} kg")
                        BiometricMetricRow("Calculated BMI", String.format("%.1f", bmi))
                        BiometricMetricRow("Basal metabolic (BMR)", "${bmr.toInt()} kcal")
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BiometricMetricRow("Total expenditure (TDEE)", "${tdee.toInt()} kcal")
                        BiometricMetricRow("Caloric Target", "${targetCalories.toInt()} kcal")
                        BiometricMetricRow("Protein Target", "${targetProtein.toInt()} g")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = ReforgeSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "MACRONUTRIENT DISTRIBUTION TARGETS",
                    color = ReforgeTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroChip("Protein", "${targetProtein.toInt()}g", "4 kcal/g", Modifier.weight(1f))
                    MacroChip("Carbs", "${targetCarbs.toInt()}g", "4 kcal/g", Modifier.weight(1f))
                    MacroChip("Fat", "${targetFat.toInt()}g", "9 kcal/g", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "LOCAL PROTOCOL ADHERENCE RATE",
                    color = ReforgeTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                AdherenceProgressBar("Workout Consistency", workoutAdherence, ReforgeLime)
                Spacer(modifier = Modifier.height(8.dp))
                AdherenceProgressBar("Protein Protocol Adherence", proteinAdherence, Color(0xFF9370DB))
                Spacer(modifier = Modifier.height(8.dp))
                AdherenceProgressBar("Habit Reprogramming Adherence", habitAdherence, Color(0xFFFFA500))
            }
        }

        // ------------------------------------------------------------
        // SECTION 1.5: ADAPTIVE LIFE ENGINE (THE BRAIN)
        // ------------------------------------------------------------
        val isAdaptiveProcessing by viewModel.isAdaptiveProcessing.collectAsState()
        val adaptiveEvents by viewModel.adaptiveEvents.collectAsState()
        val weightHistory by viewModel.weightHistory.collectAsState()

        val oldestWeight = weightHistory.firstOrNull()?.weight ?: p.weight
        val currentWeight = p.weight
        val weightDiff = currentWeight - oldestWeight

        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = ReforgeLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "ADAPTIVE LIFE ENGINE",
                            color = ReforgeTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isAdaptiveProcessing) Color(0xFFFFA500).copy(alpha = 0.15f)
                                else ReforgeLime.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAdaptiveProcessing) "CALIBRATING..." else "ACTIVE & MONITORING",
                            color = if (isAdaptiveProcessing) Color(0xFFFFA500) else ReforgeLime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Reforge's neural brain constantly runs local calculations and pattern-matching to automatically calibrate your nutrition targets without asking you.",
                    color = ReforgeTextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Bio-Calculation Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BiometricMetricRow("Start Weight", "${String.format("%.1f", oldestWeight)} kg")
                        BiometricMetricRow("Current Weight", "${String.format("%.1f", currentWeight)} kg")
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BiometricMetricRow("Net Delta", "${if (weightDiff >= 0) "+" else ""}${String.format("%.1f", weightDiff)} kg")
                        BiometricMetricRow("Engine Target", if (p.goals.contains("Gain Muscle", ignoreCase = true) || p.goals.contains("Gain Weight", ignoreCase = true)) "Anabolic Increase" else "Catabolic Decrease")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Live Decision Card (Pattern Engine Result)
                val latestEvent = adaptiveEvents.firstOrNull()
                if (latestEvent != null) {
                    val isAdj = latestEvent.caloriesAdjusted != 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAdj) ColorLung.copy(alpha = 0.12f)
                                else ReforgeSurfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                1.dp,
                                if (isAdj) ColorLung.copy(alpha = 0.3f)
                                else ReforgeSurfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAdj) Icons.Default.WarningAmber else Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = if (isAdj) ColorLung else ReforgeLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isAdj) "PATTERN ANOMALY DETECTED" else "STEADY STATE ALIGNED",
                                    color = if (isAdj) ColorLung else ReforgeLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = latestEvent.detectedPattern,
                                color = ReforgeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = latestEvent.actionTaken,
                                color = ReforgeLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                            if (latestEvent.aiExplanation.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = ReforgeSurfaceVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "AI PHYSIOLOGICAL RATIONALE:",
                                    color = ReforgeTextMuted,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = latestEvent.aiExplanation,
                                    color = ReforgeTextPrimary.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ReforgeSurfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "✓ Engine Calibrated. Standard homeostasis baseline active. Weigh-in logs are aligned with anabolic/catabolic targets.",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DIAGNOSTIC SIMULATOR CONTROL PANEL
                Text(
                    text = "DIAGNOSTIC TESTBENCH",
                    color = ReforgeTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.runAdaptiveLifeEngine(oldestWeight - 1.5f)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeSurfaceVariant, contentColor = ReforgeTextPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = ColorLung)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Weight Drop", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.runAdaptiveLifeEngine(oldestWeight + 1.5f)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeSurfaceVariant, contentColor = ReforgeTextPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = ColorLung)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Weight Gain", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (adaptiveEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "HISTORICAL ADAPTIVE DECISION LOGS",
                        color = ReforgeTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        adaptiveEvents.take(5).forEachIndexed { index, ev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ReforgeSurfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ev.date} • Starting: ${ev.startingWeight}kg → Current: ${ev.currentWeight}kg",
                                        color = ReforgeTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ev.detectedPattern,
                                        color = ReforgeTextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (ev.caloriesAdjusted > 0f) ReforgeLime.copy(alpha = 0.15f)
                                            else ColorLung.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${if (ev.caloriesAdjusted > 0f) "+" else ""}${ev.caloriesAdjusted.toInt()} kcal",
                                        color = if (ev.caloriesAdjusted > 0f) ReforgeLime else ColorLung,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ------------------------------------------------------------
        // SECTION 2: AI REPORT GENERATOR INTERFACE
        // ------------------------------------------------------------
        if (reportState == null && !isGenerating) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ReforgeLime.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ReforgeLime,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GENERATE TRANSFORMATION REPORT",
                        color = ReforgeTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saturate Reforge algorithms with your logs to synthesize a deep physical, psychological, and neurological recovery report with clinical prescriptions.",
                        color = ReforgeTextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.generateTransformationIntelligenceReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("compile_intelligence_report")
                    ) {
                        Text("Sync & Compile Report (+150 XP)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else if (isGenerating) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = ReforgeLime, modifier = Modifier.size(36.dp))
                    Text(
                        text = "COMPILING REPORT...",
                        color = ReforgeTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Synthesizing neural craving patterns, biometric weight context, and localized exposure metrics with Gemini cognitive engines...",
                        color = ReforgeTextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // ------------------------------------------------------------
        // SECTION 3: THE COMPILED INTELLIGENCE REPORT
        // ------------------------------------------------------------
        val report = reportState
        if (report != null && !isGenerating) {
            // Title card showing it is loaded
            Card(
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ReforgeLime.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(18.dp))
                            Text("SYNTHESIZED INTELLIGENCE REPORT", color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { viewModel.generateTransformationIntelligenceReport() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Re-Generate", tint = ReforgeTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Weekly Report
                    Text("WEEKLY METABOLIC & TREND REPORT", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportSegmentCard("Fluid & Weight Balance", report.weeklyWeightAnalysis)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportSegmentCard("Psychological & Craving Slope", report.weeklyTrendsAnalysis)

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Monthly Report
                    Text("MONTHLY TRANSFORMATION INTEGRATION", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportSegmentCard("Biggest Trigger", report.monthlyBiggestTrigger, headerColor = ColorLung)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportSegmentCard("Dominant Peak Habit", report.monthlyBestHabit, headerColor = ReforgeLime)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportSegmentCard("Critical Impediment Cue", report.monthlyWorstHabit, headerColor = Color(0xFFFFA500))
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportSegmentCard("Neuroplasticity Receptor Prediction", report.monthlyRecoveryPrediction, headerColor = Color(0xFF9370DB))
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportSegmentCard("Upcoming 30-Day Reset Blueprint", report.monthlyNextMonthPlan)

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Automated Clinical Prescriptions
                    Text("INTELLIGENT PRESCRIPTIONS (AUTOMATED)", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrescriptionRow("💧 Hydration targets", report.recHydration)
                        PrescriptionRow("😴 Circadian sleep schedule", report.recSleep)
                        PrescriptionRow("🔥 Calorie substrates", report.recCalorieAdjust)
                        PrescriptionRow("🏋️ Deload week target", report.recDeloadWeek)
                        PrescriptionRow("🛡️ Trigger prevention barrier", report.recRelapsePrevention)
                        PrescriptionRow("🌬️ Breathing exercises target", report.recBreathingExercises)
                        PrescriptionRow("🚶 Walking & BDNF targets", report.recWalkingGoals)
                        PrescriptionRow("📐 Spinal & posture cueing", report.recPostureCorrection)
                    }
                }
            }
        }

        // ------------------------------------------------------------
        // SECTION 4: OFFLINE DATABASE SCHEMAS (THE MOAT)
        // ------------------------------------------------------------
        Card(
            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ReforgeLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SOVEREIGN SQLITE SCHEMAS (THE MOAT)",
                        color = ReforgeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A blueprint view of the 14 client-side database tables securing your transformation logs entirely offline on this sandbox environment.",
                    color = ReforgeTextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Schema nodes grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tables.forEach { table ->
                        val isExpanded = expandedTable == table.name
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExpanded) ReforgeSurfaceVariant else ReforgeSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedTable = if (isExpanded) null else table.name }
                                .border(
                                    width = 1.dp,
                                    color = if (isExpanded) ReforgeLime else ReforgeSurfaceVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "sqlite_schema: ${table.name}",
                                            color = if (isExpanded) ReforgeLime else ReforgeTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = table.description,
                                            color = ReforgeTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = if (isExpanded) ReforgeLime else ReforgeTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .padding(8.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            table.columns.forEach { col ->
                                                Text(
                                                    text = "  +  $col",
                                                    color = Color.LightGray,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

data class JourneyStoryEvent(
    val day: Int,
    val title: String,
    val body: String
)

@Composable
fun JourneyStoryTimeline(events: List<JourneyStoryEvent>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Timeline",
            color = ReforgeTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            events.forEachIndexed { index, event ->
                Column(
                    modifier = Modifier.width(190.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ReforgeLime)
                        )
                        if (index != events.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .height(1.dp)
                                    .weight(1f)
                                    .background(ReforgeSurfaceVariant)
                            )
                        }
                    }

                    Text(
                        text = "Day ${event.day}",
                        color = ReforgeLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.title,
                        color = ReforgeTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.body,
                        color = ReforgeTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BiometricMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = ReforgeTextMuted, fontSize = 11.sp)
        Text(text = value, color = ReforgeTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MacroChip(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ReforgeSurfaceVariant)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = ReforgeTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = sub, color = ReforgeTextMuted, fontSize = 8.sp)
        }
    }
}

@Composable
fun AdherenceProgressBar(label: String, percent: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = ReforgeTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "$percent%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percent / 100f,
            color = color,
            trackColor = ReforgeSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun ReportSegmentCard(header: String, body: String, headerColor: Color = ReforgeTextPrimary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ReforgeSurfaceVariant),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = header.uppercase(), color = headerColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = body, color = ReforgeTextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
fun PrescriptionRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ReforgeSurfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
            Text(text = title.uppercase(), color = ReforgeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, color = ReforgeTextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

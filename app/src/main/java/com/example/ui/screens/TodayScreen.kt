package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.ui.ReforgeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun TodayScreen(
    viewModel: ReforgeViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val clocks by viewModel.addictionClocks.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    val dailyAnalyses by viewModel.dailyCoachAnalyses.collectAsState()
    val isAnalyzingDailyCoaching by viewModel.isAnalyzingDailyCoaching.collectAsState()
    val dailyCoachingError by viewModel.dailyCoachingError.collectAsState()

    val transit by viewModel.todayTransitAnalysis.collectAsState()
    val relapseRiskPercent by viewModel.todayRelapseRiskPercent.collectAsState()
    val dailySchedule by viewModel.dailySchedule.collectAsState()
    val scheduledAlarms by viewModel.scheduledAlarms.collectAsState()
    val isGeneratingSchedule by viewModel.isGeneratingSchedule.collectAsState()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var isNewHabitBad by remember { mutableStateOf(false) }
    var showDailyPlanDetails by remember { mutableStateOf(false) }
    var showMoreToday by remember { mutableStateOf(false) }

    var checkInMood by remember { mutableStateOf(3) } // 1: bad, 2: meh, 3: good, 4: great
    var checkInEnergy by remember { mutableStateOf(7) } // 1 to 10
    var checkInSleep by remember { mutableStateOf(7) } // 1 to 10
    var checkInCravings by remember { mutableStateOf(3) } // 1 to 10
    var checkInWeightText by remember { mutableStateOf("") }

    var showPatternsDetail by remember { mutableStateOf(false) }
    val checkedActionItems = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(profile) {
        val currentProfile = profile
        if (currentProfile != null && checkInWeightText.isEmpty()) {
            checkInWeightText = currentProfile.weight.toString()
        }
    }

    // Calculate completion percentage
    val todayDate = viewModel.getTodayString()
    val checkInToday = checkIns.firstOrNull { it.date == todayDate }
    val analysisToday = dailyAnalyses.firstOrNull { it.date == todayDate }
    val totalHabits = habits.size
    val completedHabits = habits.count { it.lastCompletedDate == todayDate }
    val completionPercent = if (totalHabits > 0) ((completedHabits.toFloat() / totalHabits) * 100).toInt() else 0
    val todayScore = analysisToday?.recoveryScore ?: 78
    val focusItems = remember(analysisToday) {
        val coachFocus = analysisToday?.todayFocus?.takeIf { it.isNotBlank() }
        listOf(
            coachFocus ?: "Avoid evening isolation.",
            "Eat protein at 6 PM."
        ).distinct()
    }

    // Get major positive streak status
    val majorStreakText = remember(clocks) {
        val clock = clocks.firstOrNull()
        if (clock != null) {
            val diffMs = System.currentTimeMillis() - clock.lastResetTimestamp
            val days = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
            "$days Days • ${clock.addictionName} Free"
        } else {
            "Reforge Active"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ReforgeBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Welcome Header
        item {
            StaggeredReveal(order = 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning, ${profile?.name?.ifBlank { "User" } ?: "User"} 👋",
                        color = ReforgeTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = { /* Check Alerts */ },
                    modifier = Modifier
                        .testTag("notification_button")
                        .clip(CircleShape)
                        .background(ReforgeSurfaceVariant)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = ReforgeTextPrimary
                    )
                }
            }
            }
        }

        item {
            StaggeredReveal(order = 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ReforgeLime.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val alignmentEnergy = transit?.energyForecast?.morning ?: "High"
                    val alignmentFocus = transit?.suggestedFocus?.takeIf { it.isNotBlank() } ?: "Recovery"
                    val alignmentRisk = transit?.avoidList?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Impulsive decisions"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Score",
                                color = ReforgeTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$todayScore",
                                color = ReforgeTextPrimary,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.OfflineBolt,
                            contentDescription = null,
                            tint = ReforgeLime,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    StaggeredReveal(order = 2) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HorizontalDivider(color = ReforgeSurfaceVariant)
                            TodaySectionHeader("Today's Focus")
                            focusItems.forEach { focus ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ReforgeLime)
                                    )
                                    Text(
                                        text = focus,
                                        color = ReforgeTextPrimary,
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    StaggeredReveal(order = 3) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HorizontalDivider(color = ReforgeSurfaceVariant)
                            TodaySectionHeader("Today's Schedule")
                            UpcomingScheduleRow(title = "Workout", time = "9:55")
                            UpcomingScheduleRow(title = "Lunch", time = "11:55")
                            UpcomingScheduleRow(title = "Snack", time = "2:55")
                        }
                    }

                    StaggeredReveal(order = 4) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HorizontalDivider(color = ReforgeSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TodaySectionHeader("Today's Alignment")
                                TextButton(onClick = { showMoreToday = true }) {
                                    Text("View Details", color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            AlignmentMetricRow(label = "Energy", value = alignmentEnergy)
                            AlignmentMetricRow(label = "Focus", value = alignmentFocus)
                            AlignmentMetricRow(label = "Risk", value = alignmentRisk)
                        }
                    }

                    StaggeredReveal(order = 5) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HorizontalDivider(color = ReforgeSurfaceVariant)
                            TodaySectionHeader("Quick Actions")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickActionPill(
                                    label = "Check-in",
                                    icon = Icons.Default.CheckCircle,
                                    onClick = { showDailyPlanDetails = !showDailyPlanDetails },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickActionPill(
                                    label = "Coach",
                                    icon = Icons.Default.ChatBubble,
                                    onClick = { showMoreToday = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        // Morning Biometric Check-In / Daily Coach Blueprint
        if (showDailyPlanDetails) {
        item {
            val checkInToday = checkIns.firstOrNull { it.date == todayDate }
            val analysisToday = dailyAnalyses.firstOrNull { it.date == todayDate }

            if (checkInToday == null) {
                // Morning Biometric Check-In Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = ReforgeLime.copy(alpha = 0.3f),
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
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = ReforgeLime,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "🌞 MORNING CHECK-IN",
                                color = ReforgeLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Answer 5 quick biometrics to trigger your AI Coach Daily Reset Blueprint.",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Mood Selection
                        Text(
                            text = "How is your Mood?",
                            color = ReforgeTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val moods = listOf(
                                1 to "😭 Bad",
                                2 to "😐 Meh",
                                3 to "🙂 Good",
                                4 to "😄 Great"
                            )
                            moods.forEach { (valNum, label) ->
                                val selected = checkInMood == valNum
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) ReforgeLime else ReforgeSurfaceVariant)
                                        .clickable { checkInMood = valNum }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected) Color.Black else ReforgeTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Energy Level
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Energy Level",
                                color = ReforgeTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$checkInEnergy / 10",
                                color = ReforgeLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = checkInEnergy.toFloat(),
                            onValueChange = { checkInEnergy = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                activeTrackColor = ReforgeLime,
                                inactiveTrackColor = ReforgeSurfaceVariant,
                                thumbColor = ReforgeLime
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. Sleep Quality
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sleep Quality",
                                color = ReforgeTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$checkInSleep / 10",
                                color = ReforgeLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = checkInSleep.toFloat(),
                            onValueChange = { checkInSleep = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                activeTrackColor = ReforgeLime,
                                inactiveTrackColor = ReforgeSurfaceVariant,
                                thumbColor = ReforgeLime
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Craving Intensity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Craving Intensity",
                                color = ReforgeTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$checkInCravings / 10",
                                color = ReforgeLime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = checkInCravings.toFloat(),
                            onValueChange = { checkInCravings = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                activeTrackColor = ReforgeLime,
                                inactiveTrackColor = ReforgeSurfaceVariant,
                                thumbColor = ReforgeLime
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. Weight Input
                        Text(
                            text = "Current Weight (kg)",
                            color = ReforgeTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = checkInWeightText,
                            onValueChange = { checkInWeightText = it },
                            placeholder = { Text("e.g. 78.4") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ReforgeTextPrimary,
                                unfocusedTextColor = ReforgeTextPrimary,
                                focusedBorderColor = ReforgeLime,
                                unfocusedBorderColor = ReforgeSurfaceVariant
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val wFloat = checkInWeightText.toFloatOrNull() ?: profile?.weight ?: 0f
                                viewModel.saveCheckIn(
                                    mood = checkInMood,
                                    energy = checkInEnergy,
                                    sleep = checkInSleep,
                                    cravings = checkInCravings,
                                    weight = wFloat
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_daily_checkin"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Forge Today's Blueprint", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            } else if (isAnalyzingDailyCoaching) {
                // Loading / Analyzing state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = ReforgeLime, modifier = Modifier.size(44.dp))
                        Text(
                            text = "AI Coach compiling resets...",
                            color = ReforgeTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Analyzing your last 30 days of biometrics, relapse triggers, sleep logs, and completed workouts to generate your custom Today Reset Plan.",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (analysisToday != null) {
                // AI Coach Daily Reset Blueprint Card
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflineBolt,
                                    contentDescription = null,
                                    tint = ReforgeLime,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "🧠 AI COACH DAILY RESET",
                                    color = ReforgeLime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                )
                            }

                            // Relapse Risk Pill Badge
                            val riskColor = when (analysisToday.riskLevel) {
                                "High" -> Color.Red
                                "Medium" -> Color(0xFFFFA500)
                                else -> ReforgeLime
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(riskColor.copy(alpha = 0.15f))
                                    .border(1.dp, riskColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Risk: ${analysisToday.riskLevel}",
                                    color = riskColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Today's stats logging indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ReforgeSurfaceVariant)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val moodEmoji = when (checkInToday.mood) {
                                1 -> "😭"
                                2 -> "😐"
                                3 -> "🙂"
                                else -> "😄"
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Mood", color = ReforgeTextMuted, fontSize = 9.sp)
                                Text(moodEmoji, color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Energy", color = ReforgeTextMuted, fontSize = 9.sp)
                                Text("${checkInToday.energy}/10", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sleep", color = ReforgeTextMuted, fontSize = 9.sp)
                                Text("${checkInToday.sleepQuality}/10", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cravings", color = ReforgeTextMuted, fontSize = 9.sp)
                                Text("${checkInToday.cravings}/10", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Weight", color = ReforgeTextMuted, fontSize = 9.sp)
                                Text("${checkInToday.weight} kg", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Recovery Score Progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Daily Recovery Score", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("${analysisToday.recoveryScore}%", color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { analysisToday.recoveryScore.toFloat() / 100f },
                            color = ReforgeLime,
                            trackColor = ReforgeSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Patterns analysis insight
                        Text(
                            text = analysisToday.patterns,
                            color = ReforgeTextPrimary,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Starry Astrological alignment focus
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ReforgeLimeMuted.copy(alpha = 0.2f))
                                .border(1.dp, ReforgeLime.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ReforgeLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text("Cosmic Reflection Focus", color = ReforgeLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = analysisToday.todayFocus,
                                        color = ReforgeTextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action plan list
                        Text(
                            text = "COACH'S DAILY ACTION PLAN",
                            color = ReforgeTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val actions = analysisToday.actionPlan.split(";")
                        actions.forEachIndexed { idx, action ->
                            if (action.isNotBlank()) {
                                val isChecked = checkedActionItems["$todayDate-$idx"] ?: false
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { checkedActionItems["$todayDate-$idx"] = !isChecked }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isChecked) ReforgeLime else ReforgeTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = action,
                                        color = if (isChecked) ReforgeTextMuted else ReforgeTextPrimary,
                                        fontSize = 12.sp,
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else null,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Expandable 30 day detailed analysis
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = ReforgeSurfaceVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPatternsDetail = !showPatternsDetail }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View 30-Day Pattern Recognition",
                                color = ReforgeLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (showPatternsDetail) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = ReforgeLime,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (showPatternsDetail) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Relapse patterns
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Dangerous, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                        Text("Relapse Vulnerability Patterns", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(analysisToday.relapsePatterns, color = ReforgeTextPrimary, fontSize = 11.sp, lineHeight = 14.sp)
                                }

                                // Missed workouts
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(12.dp))
                                        Text("Missed Workout Correlation", color = ReforgeLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(analysisToday.missedWorkoutPatterns, color = ReforgeTextPrimary, fontSize = 11.sp, lineHeight = 14.sp)
                                }

                                // Sleep issues
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = ColorSleep, modifier = Modifier.size(12.dp))
                                        Text("Sleep Issues & Biometrics Correlation", color = ColorSleep, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(analysisToday.sleepIssues, color = ReforgeTextPrimary, fontSize = 11.sp, lineHeight = 14.sp)
                                }

                                // Confidence trends
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ColorBrain, modifier = Modifier.size(12.dp))
                                        Text("Confidence & Psychological Baselines", color = ColorBrain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(analysisToday.confidenceTrends, color = ReforgeTextPrimary, fontSize = 11.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // If today is checked in but we couldn't load analysis for some reason, offer to trigger it again
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ReforgeSurface)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's biometrics logged.", color = ReforgeTextPrimary, fontSize = 12.sp)
                    TextButton(
                        onClick = {
                            viewModel.triggerDailyCoachingAnalysis(
                                mood = checkInToday.mood,
                                energy = checkInToday.energy,
                                sleep = checkInToday.sleepQuality,
                                cravings = checkInToday.cravings,
                                weight = checkInToday.weight
                            )
                        }
                    ) {
                        Text("Analyze Now", color = ReforgeLime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
        }

        if (showMoreToday) {
        // Vedic Astrology 2.0 Transit & Focus Card
        transit?.let { currentTransit ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFBB86FC).copy(alpha = 0.3f), // Premium Cosmic Violet border
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFBB86FC),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "🌌 COSMOLOGICAL TRANSITS (VEDIC 2.0)",
                                color = Color(0xFFBB86FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.0.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val lagnaStr = profile?.zodiacTheme?.substringBefore(" ") ?: "Ascendant"
                        val natalSignStr = profile?.zodiacTheme?.substringAfter("Moon:")?.substringBefore(",")?.trim()?.ifBlank { "Natal Sign" } ?: "Natal Sign"
                        Text(
                            text = "Lagna: $lagnaStr • Natal Moon: $natalSignStr • Today: Moon in ${currentTransit.transitMoonSign}",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // High Risk Window Alert
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Red.copy(alpha = 0.08f))
                                .border(1.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Today's Risk Window: ${currentTransit.riskWindow}",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reason: ${currentTransit.reason}",
                                    color = ReforgeTextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Suggested Focus: ${currentTransit.suggestedFocus}",
                                    color = ReforgeLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Avoid & Recommended Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avoid list
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AVOID TODAY",
                                    color = Color.Red,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                currentTransit.avoidList.forEach { avoidItem ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = avoidItem, color = ReforgeTextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Recommended list
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "RECOMMENDED",
                                    color = ReforgeLime,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                currentTransit.recommendedList.forEach { recItem ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(ReforgeLime)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = recItem, color = ReforgeTextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Relapse Risk Score Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Habit Relapse Risk Score", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("$relapseRiskPercent%", color = if (relapseRiskPercent > 70) Color.Red else ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { relapseRiskPercent.toFloat() / 100f },
                            color = if (relapseRiskPercent > 70) Color.Red else ReforgeLime,
                            trackColor = ReforgeSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Factors: sleep debt • historical patterns • cosmic transit",
                            color = ReforgeTextMuted,
                            fontSize = 9.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Energy Forecast Columns
                        Text(
                            text = "ASTRO ENERGY FORECAST",
                            color = ReforgeTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val forecastItems = listOf(
                                Triple("Morning", currentTransit.energyForecast.morning, ColorSleep),
                                Triple("Afternoon", currentTransit.energyForecast.afternoon, ColorBrain),
                                Triple("Night", currentTransit.energyForecast.night, ColorLung)
                            )
                            forecastItems.forEach { (timeOfDay, level, color) ->
                                val textColor = when (level.lowercase()) {
                                    "high" -> ReforgeLime
                                    "medium" -> Color(0xFFFFA500)
                                    else -> Color(0xFF888888)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ReforgeSurfaceVariant)
                                        .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = timeOfDay, color = ReforgeTextMuted, fontSize = 9.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = level, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Daily Routine Scheduler Card
        item {
            var wakeInput by remember { mutableStateOf("07:00") }
            var officeInput by remember { mutableStateOf("12:30") }
            var commuteInput by remember { mutableStateOf("2 hours") }
            var showTestConfirm by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ReforgeLime.copy(alpha = 0.15f),
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
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = ReforgeLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "AI DAILY RESET SCHEDULER",
                            color = ReforgeLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Automatically optimize meal timing, workout intervals, and sleep schedules based on constraints.",
                        color = ReforgeTextMuted,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input Form Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = wakeInput,
                            onValueChange = { wakeInput = it },
                            label = { Text("Wake Time", fontSize = 10.sp) },
                            placeholder = { Text("07:00") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ReforgeTextPrimary,
                                unfocusedTextColor = ReforgeTextPrimary,
                                focusedBorderColor = ReforgeLime,
                                unfocusedBorderColor = ReforgeSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = officeInput,
                            onValueChange = { officeInput = it },
                            label = { Text("Office Start", fontSize = 10.sp) },
                            placeholder = { Text("12:30") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ReforgeTextPrimary,
                                unfocusedTextColor = ReforgeTextPrimary,
                                focusedBorderColor = ReforgeLime,
                                unfocusedBorderColor = ReforgeSurfaceVariant
                            ),
                            modifier = Modifier.weight(1.1f)
                        )

                        OutlinedTextField(
                            value = commuteInput,
                            onValueChange = { commuteInput = it },
                            label = { Text("Commute", fontSize = 10.sp) },
                            placeholder = { Text("2 hours") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ReforgeTextPrimary,
                                unfocusedTextColor = ReforgeTextPrimary,
                                focusedBorderColor = ReforgeLime,
                                unfocusedBorderColor = ReforgeSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.generateDailySchedule(wakeInput, officeInput, commuteInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGeneratingSchedule) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Compute & Schedule Local Alarms", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Render Schedule Outputs if generated
                    dailySchedule?.let { schedule ->
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "TODAY'S PROTOCOL ROUTINE",
                            color = ReforgeTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ReforgeSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🍳 Breakfast (Meal 1)", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(schedule.meal1, color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("💪 Muscle/Posture Workout", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(schedule.workout, color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🍱 Lunch/Dinner (Meal 2)", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(schedule.meal2, color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🍎 Craving Defense Snack", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(schedule.snack, color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("😴 Bedtime Wind-down", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(schedule.sleep, color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Scheduled alarms list
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "SCHEDULED LOCAL ALERTS (OFFLINE)",
                            color = ReforgeTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            scheduledAlarms.forEach { alarm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = when (alarm.type) {
                                            "Risk" -> Color.Red
                                            "Preparation" -> Color(0xFFFFA500)
                                            "Action" -> ReforgeLime
                                            else -> Color(0xFFBB86FC)
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = alarm.title, color = ReforgeTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = alarm.description, color = ReforgeTextMuted, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = alarm.timeLabel,
                                        color = ReforgeLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Test Notification Trigger
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.triggerTestNotification()
                                    showTestConfirm = true
                                }
                            ) {
                                Icon(Icons.Default.BugReport, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Alerts Immediately", color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (showTestConfirm) {
                                Text("Alert in 3s...", color = ReforgeLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // One calm surface with sections instead of a stack of cards.
        item {
            StaggeredReveal(order = 4) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Today",
                        color = ReforgeTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TodaySectionHeader("Recovery")
                    RecoveryProgressRow(
                        title = "Brain Recovery",
                        percent = 40,
                        icon = Icons.Default.Psychology,
                        color = ColorBrain,
                        subtitle = "Nicotine receptors ending normalization"
                    )
                    RecoveryProgressRow(
                        title = "Lung Recovery",
                        percent = 30,
                        icon = Icons.Default.Favorite,
                        color = ColorLung,
                        subtitle = "Carbon monoxide eliminated from bloodstream"
                    )
                    RecoveryProgressRow(
                        title = "Sleep Recovery",
                        percent = 50,
                        icon = Icons.Default.Brightness3,
                        color = ColorSleep,
                        subtitle = "REM sleeping cycles restabilizing"
                    )

                    HorizontalDivider(color = ReforgeSurfaceVariant)

                    TodaySectionHeader("Workout")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dailySchedule?.workout ?: "Take a 15 min walk after work.",
                            color = ReforgeTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$completionPercent%",
                            color = ReforgeLime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = ReforgeSurfaceVariant)

                    TodaySectionHeader("Nutrition")
                    Text(
                        text = dailySchedule?.snack ?: "Eat protein at 6 PM.",
                        color = ReforgeTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(color = ReforgeSurfaceVariant)

                    TodaySectionHeader("Habits")
                    if (habits.isEmpty()) {
                        Text(
                            text = "No habits yet.",
                            color = ReforgeTextMuted,
                            fontSize = 13.sp
                        )
                    } else {
                        habits.take(4).forEach { habit ->
                            val checked = habit.lastCompletedDate == todayDate
                            val rowScale by animateFloatAsState(
                                targetValue = if (checked) 1.02f else 1f,
                                label = "habit_completion_scale"
                            )
                            val rowGlow by animateColorAsState(
                                targetValue = if (checked) ReforgeSuccess.copy(alpha = 0.14f) else Color.Transparent,
                                label = "habit_completion_glow"
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(rowScale)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rowGlow)
                                    .clickable { viewModel.toggleHabit(habit) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Selection State",
                                    tint = if (checked) ReforgeSuccess else ReforgeTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = habit.name,
                                    color = if (checked) ReforgeTextMuted else ReforgeTextPrimary,
                                    fontSize = 13.sp,
                                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                                    fontWeight = if (checked) FontWeight.Normal else FontWeight.Medium
                                )
                                AnimatedVisibility(
                                    visible = checked,
                                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        text = "+XP",
                                        color = ReforgeSuccess,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showAddHabitDialog = true },
                        modifier = Modifier.testTag("add_custom_habit")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Icon", tint = ReforgeLime, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add habit", color = ReforgeLime, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            }
        }

        // Tomorrow's Cosmic Reset Blueprint
        item {
            Text(
                text = "TOMORROW'S COSMIC RESET",
                color = ReforgeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            var showTomorrowDetails by remember { mutableStateOf(false) }
            val context = androidx.compose.ui.platform.LocalContext.current
            var pdfStatusMessage by remember { mutableStateOf("") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ReforgeLime.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ReforgeLimeMuted),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessHigh,
                                    contentDescription = "Cosmic Sun Icon",
                                    tint = ReforgeLime,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Tomorrow's Master Plan",
                                    color = ReforgeTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Aligned with your identity & zodiac axis",
                                    color = ReforgeTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { showTomorrowDetails = !showTomorrowDetails }
                        ) {
                            Icon(
                                imageVector = if (showTomorrowDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle tomorrow details",
                                tint = ReforgeTextPrimary
                            )
                        }
                    }

                    // Zodiac Brief info always visible
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ReforgeSurfaceVariant)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Cosmic Magic",
                            tint = ReforgeLime,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Pisces/Virgo Lunar Axis Reset • Physical Purge Window Active",
                            color = ReforgeTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showTomorrowDetails) {
                        HorizontalDivider(color = ReforgeSurfaceVariant, thickness = 1.dp)

                        Text(
                            text = "DIET & MEAL MASTER MEASUREMENTS (RAW WEIGHTS):",
                            color = ReforgeLime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        val timeline = listOf(
                            "08:00 AM FLUSH" to "500ml Warm water + 5g raw Jeera/Ajwain + 1/2 Lemon.",
                            "08:20 AM POSTURE" to "10 Wall Angels & 20 Band Pull-Aparts for scapular realignment.",
                            "08:30 AM GYM WORKOUT" to "Day 1 PUSH (Chest, Shoulders, Triceps). Keep under 45 mins limit.",
                            "10:00 AM BREAKFAST" to "3 Whole Eggs + 2 Egg Whites scrambled in 5ml olive oil + Coffee. Supplements: Neurobion Forte, Multivitamin.",
                            "11:30 AM LUNCH" to "120g raw Dal + 30g raw Basmati Rice + 150g raw soya chunks or chicken breast + Green salad. Supplements: Fish Oil, Liv.52.",
                            "03:00 PM MID-DAY PIVOT" to "250ml fresh low-fat Buttermilk (Chaas) with black salt + 5 mins Box Breathing.",
                            "06:00 PM EVENING URGE DEFENSE" to "30g unpeeled roasted peanuts (unsalted) OR 1 medium ripe Banana (100g) + 5 mins breathing.",
                            "08:00 PM DINNER" to "120g raw Dal soup (cooked thin, strictly zero chapati/rice) + 2 Boiled egg whites.",
                            "11:30 PM BEDTIME STACK" to "200ml warm turmeric milk + ZMA, Ashwagandha, and 5 mins scalp massage."
                        )

                        timeline.forEach { (time, action) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ReforgeSurfaceVariant)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = time,
                                        color = ReforgeLime,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = action,
                                    color = ReforgeTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = ReforgeSurfaceVariant, thickness = 1.dp)

                        Button(
                            onClick = {
                                try {
                                    val file = com.example.util.PdfGenerator.generateProtocolPdf(context, profile ?: com.example.data.UserProfile())
                                    pdfStatusMessage = "Success! Saved PDF as '${file.name}' in app folder."
                                } catch (e: Exception) {
                                    pdfStatusMessage = "Error: ${e.localizedMessage}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF icon", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Clean 7-Day Plan PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (pdfStatusMessage.isNotEmpty()) {
                            Text(
                                text = pdfStatusMessage,
                                color = if (pdfStatusMessage.startsWith("Success")) ReforgeLime else Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Tap arrow to expand your detailed raw meal quantities, supplements timings, and workout schedule.",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        }

        // Spacer to clear the bottom UI block correctly
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add custom habit dialog
    if (showAddHabitDialog) {
        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            title = { Text("Build New Habit", color = ReforgeTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newHabitName,
                        onValueChange = { newHabitName = it },
                        label = { Text("Habit Name") },
                        placeholder = { Text("e.g. Read 10 Pages") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("new_habit_name_input")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isNewHabitBad = !isNewHabitBad }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isNewHabitBad,
                            onCheckedChange = { isNewHabitBad = it },
                            colors = CheckboxDefaults.colors(checkedColor = ColorLung)
                        )
                        Column {
                            Text("Bad Habit / Addiction Avoidance", color = ReforgeTextPrimary, fontSize = 14.sp)
                            Text("Enable to track abstinence time instead of daily ticks.", color = ReforgeTextMuted, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newHabitName.isNotBlank()) {
                            viewModel.addCustomHabit(newHabitName, isNewHabitBad)
                            newHabitName = ""
                            showAddHabitDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.White)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHabitDialog = false }) {
                    Text("Cancel", color = ReforgeTextMuted)
                }
            },
            containerColor = ReforgeSurface
        )
    }
}

@Composable
fun TodaySectionHeader(title: String) {
    Text(
        text = title,
        color = ReforgeTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun UpcomingScheduleRow(title: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = ReforgeTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = time,
            color = ReforgeTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AlignmentMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = ReforgeTextMuted,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = ReforgeTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun QuickActionPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ReforgeSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ReforgeLime,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = ReforgeTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StaggeredReveal(
    order: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(order) {
        delay(order * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut()
    ) {
        content()
    }
}

@Composable
fun DisclosureRow(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ReforgeSurface)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = ReforgeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = ReforgeTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = ReforgeLime
        )
    }
}

@Composable
fun RecoveryProgressRow(
    title: String,
    percent: Int,
    icon: ImageVector,
    color: Color,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ReforgeSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "$percent%", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { percent.toFloat() / 100f },
                color = color,
                trackColor = ReforgeSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = ReforgeTextMuted, fontSize = 11.sp)
        }
    }
}

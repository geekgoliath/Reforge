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

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var newHabitName by remember { mutableStateOf("") }
    var isNewHabitBad by remember { mutableStateOf(false) }

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
    val totalHabits = habits.size
    val completedHabits = habits.count { it.lastCompletedDate == todayDate }
    val completionPercent = if (totalHabits > 0) ((completedHabits.toFloat() / totalHabits) * 100).toInt() else 0

    // Get major positive streak status
    val majorStreakText = remember(clocks) {
        val alcoholClock = clocks.find { it.addictionName == "Alcohol" }
        if (alcoholClock != null) {
            val diffMs = System.currentTimeMillis() - alcoholClock.lastResetTimestamp
            val days = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
            "$days Days • Alcohol Free"
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning, ${profile?.name ?: "Vikas"} 👋",
                        color = ReforgeTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = majorStreakText,
                        color = ReforgeLime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
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

        // Morning Biometric Check-In / Daily Coach Blueprint
        item {
            val checkInToday = checkIns.find { it.date == todayDate }
            val analysisToday = dailyAnalyses.find { it.date == todayDate }

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
                                val wFloat = checkInWeightText.toFloatOrNull() ?: profile?.weight ?: 78.4f
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

        // Today's Mission progress card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TODAY'S MISSION",
                        color = ReforgeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Circular Loader
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(100.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { completionPercent.toFloat() / 100f },
                                modifier = Modifier.size(90.dp),
                                color = ReforgeLime,
                                strokeWidth = 8.dp,
                                trackColor = ReforgeSurfaceVariant
                            )
                            Text(
                                text = "$completionPercent%",
                                color = ReforgeTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Habits checklist snippet
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (habits.isEmpty()) {
                                Text(
                                    text = "No habits. Commit to a task to Reforge your system!",
                                    color = ReforgeTextMuted,
                                    fontSize = 13.sp
                                )
                            } else {
                                habits.take(4).forEach { habit ->
                                    val checked = habit.lastCompletedDate == todayDate
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.toggleHabit(habit) }
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Selection State",
                                            tint = if (checked) ReforgeLime else ReforgeTextMuted,
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
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showAddHabitDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeSurfaceVariant, contentColor = ReforgeLime),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_custom_habit"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Icon", tint = ReforgeLime, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Custom Habit", color = ReforgeLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recovery Status list
        item {
            Text(
                text = "RECOVERY STATUS",
                color = ReforgeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // Recovery modules: Brain, Lung, Sleep, dopamine
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RecoveryProgressRow(
                        title = "Brain Recovery",
                        percent = 40,
                        icon = Icons.Default.Psychology,
                        color = ColorBrain,
                        subtitle = "Nicotine receptors ending normalization"
                    )
                    Divider(color = ReforgeSurfaceVariant, thickness = 1.dp)
                    RecoveryProgressRow(
                        title = "Lung Recovery",
                        percent = 30,
                        icon = Icons.Default.Favorite,
                        color = ColorLung,
                        subtitle = "Carbon monoxide eliminated from bloodstream"
                    )
                    Divider(color = ReforgeSurfaceVariant, thickness = 1.dp)
                    RecoveryProgressRow(
                        title = "Sleep Recovery",
                        percent = 50,
                        icon = Icons.Default.Brightness3,
                        color = ColorSleep,
                        subtitle = "REM sleeping cycles restabilizing"
                    )
                }
            }
        }

        // Focus card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ReforgeLimeMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = "Target",
                            tint = ReforgeLime
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S FOCUS",
                            color = ReforgeLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Avoid evening isolation. Take a 15 min walk after work.",
                            color = ReforgeTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
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

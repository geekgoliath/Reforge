package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyCheckIn
import com.example.data.JournalEntry
import com.example.ui.ReforgeViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ReforgeViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val relapseEvents by viewModel.relapseEvents.collectAsState()
    val isJournaling by viewModel.isAnalyzingJournal.collectAsState()

    var showCheckInDialog by remember { mutableStateOf(false) }
    var checkInMood by remember { mutableStateOf(3) } // Defaults to Happy
    var checkInEnergy by remember { mutableStateOf(6f) }
    var checkInSleep by remember { mutableStateOf(6f) }
    var checkInCravings by remember { mutableStateOf(3f) }

    var journalText by remember { mutableStateOf("") }
    var expandedJournalId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ReforgeBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Profile Header
        item {
            Column {
                Text(
                    text = "Profile",
                    color = ReforgeTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You vs Yesterday",
                    color = ReforgeTextMuted,
                    fontSize = 13.sp
                )
            }
        }

        // Profile card with XP metrics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(ReforgeSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile?.name ?: "V").take(1).uppercase(Locale.getDefault()),
                                color = ReforgeLime,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Name and Level details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.name?.ifBlank { "User" } ?: "User",
                                color = ReforgeTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Level ${profile?.level ?: 3}",
                                    color = ReforgeTextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ReforgeLimeMuted)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text("Keep Going 🔥", color = ReforgeLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // XP bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Experience Points", color = ReforgeTextMuted, fontSize = 12.sp)
                        Text(
                            text = "${profile?.xp ?: 650} / 1000 XP",
                            color = ReforgeLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (profile?.xp ?: 650).toFloat() / 1000f },
                        color = ReforgeLime,
                        trackColor = ReforgeSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        // Quick self actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "SELF EVALUATION",
                        color = ReforgeTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )

                    // Daily check-in button trigger
                    Button(
                        onClick = { showCheckInDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_checkin_dialog"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = "Assessment Logo")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Daily Check-In (+60 XP)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Module 4: Nightly AI Journal Analyst
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                var isRecordingSpeech by remember { mutableStateOf(false) }
                var recordingDurationSeconds by remember { mutableStateOf(0) }
                var isTranscribingSpeech by remember { mutableStateOf(false) }
                var selectedSpeechPresetIndex by remember { mutableStateOf(-1) }

                val scope = rememberCoroutineScope()

                val presetTranscripts = listOf(
                    "Today was a massive win! I resisted all vape cravings after lunch by doing some breathwork. My mistake was having coffee too late, which got me restless. Tomorrow focus is zero caffeine after 2 PM.",
                    "Felt super stressed from work today. My trigger was my boss's email. Win: did not touch alcohol or smoke. Mistake: skipped gym. Tomorrow focus: morning workout and hydration.",
                    "Highly focused today! Completed my routines and met targets. Win: fully sober and smoke-free day. Mistake: none. Tomorrow focus: review my goals early and stay consistent."
                )

                // Launch timer during active recording
                LaunchedEffect(isRecordingSpeech) {
                    if (isRecordingSpeech) {
                        recordingDurationSeconds = 0
                        while (isRecordingSpeech) {
                            delay(1000L)
                            recordingDurationSeconds++
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI EVENING JOURNAL",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )

                        if (!isRecordingSpeech && !isTranscribingSpeech) {
                            IconButton(
                                onClick = {
                                    isRecordingSpeech = true
                                    selectedSpeechPresetIndex = -1
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = ReforgeLimeMuted,
                                    contentColor = ReforgeLime
                                ),
                                modifier = Modifier.size(32.dp).testTag("start_voice_journal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Record",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isRecordingSpeech) {
                        // ACTIVE VOICE RECORDING LAYOUT
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ReforgeBg, shape = RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(ColorLung)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "RECORDING SPEECH...",
                                    color = ReforgeTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Simulated Waveform Bars
                            Row(
                                modifier = Modifier.height(40.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val infiniteTransition = rememberInfiniteTransition(label = "wave")
                                val h1 by infiniteTransition.animateFloat(
                                    initialValue = 10f, targetValue = 40f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(400, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "h1"
                                )
                                val h2 by infiniteTransition.animateFloat(
                                    initialValue = 25f, targetValue = 10f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(500, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "h2"
                                )
                                val h3 by infiniteTransition.animateFloat(
                                    initialValue = 15f, targetValue = 35f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(300, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "h3"
                                )
                                val h4 by infiniteTransition.animateFloat(
                                    initialValue = 30f, targetValue = 15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(450, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "h4"
                                )
                                val h5 by infiniteTransition.animateFloat(
                                    initialValue = 8f, targetValue = 38f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(350, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "h5"
                                )

                                Box(modifier = Modifier.width(4.dp).height(h1.dp).clip(RoundedCornerShape(2.dp)).background(ReforgeLime))
                                Box(modifier = Modifier.width(4.dp).height(h2.dp).clip(RoundedCornerShape(2.dp)).background(ReforgeLime))
                                Box(modifier = Modifier.width(4.dp).height(h3.dp).clip(RoundedCornerShape(2.dp)).background(ReforgeLime))
                                Box(modifier = Modifier.width(4.dp).height(h4.dp).clip(RoundedCornerShape(2.dp)).background(ReforgeLime))
                                Box(modifier = Modifier.width(4.dp).height(h5.dp).clip(RoundedCornerShape(2.dp)).background(ReforgeLime))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val minutes = recordingDurationSeconds / 60
                            val seconds = recordingDurationSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                color = ReforgeTextMuted,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        isRecordingSpeech = false
                                        isTranscribingSpeech = true
                                        scope.launch {
                                            delay(1500L) // Transcribe simulation delay
                                            isTranscribingSpeech = false
                                            journalText = if (selectedSpeechPresetIndex in presetTranscripts.indices) {
                                                presetTranscripts[selectedSpeechPresetIndex]
                                            } else {
                                                presetTranscripts.random()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime),
                                    modifier = Modifier.testTag("stop_record_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = ReforgeBg, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Stop & Transcribe", color = ReforgeBg, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { isRecordingSpeech = false },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorLung),
                                    modifier = Modifier.border(1.dp, ColorLung, RoundedCornerShape(50))
                                ) {
                                    Text("Cancel", fontSize = 12.sp)
                                }
                            }
                        }
                    } else if (isTranscribingSpeech) {
                        // TRANSCRIBING AUDIO STATE
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ReforgeBg, shape = RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = ReforgeLime, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Transcribing voice journaling stream...",
                                color = ReforgeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Using localized Speech-to-Text neural model",
                                color = ReforgeTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        // REGULAR TEXT INPUT AND PRESETS
                        Text(
                            text = "Speak or write your daily reflections. Gemini will analyze stressors, emotional triggers, wins, mistakes, and future focus.",
                            color = ReforgeTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Speech-to-Text Presets
                        Text(
                            text = "OR CHOOSE A RECORDED VOICE TRANSCRIPT:",
                            color = ReforgeTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetTranscripts.forEachIndexed { index, transcript ->
                                val label = when (index) {
                                    0 -> "🎯 Sovereign Win"
                                    1 -> "⚠️ Stress Trigger"
                                    else -> "⚡ Sovereign Focus"
                                }
                                val isSelected = selectedSpeechPresetIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                        .clickable {
                                            selectedSpeechPresetIndex = if (isSelected) -1 else index
                                            journalText = if (selectedSpeechPresetIndex == -1) "" else presetTranscripts[index]
                                        }
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) ReforgeLime else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) ReforgeLime else ReforgeTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = journalText,
                            onValueChange = { journalText = it },
                            placeholder = { Text("Today I felt stressed because... I avoided vaping by...", color = ReforgeTextMuted, fontSize = 13.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ReforgeTextPrimary,
                                unfocusedTextColor = ReforgeTextPrimary,
                                focusedBorderColor = ReforgeLime,
                                unfocusedBorderColor = ReforgeSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                                .testTag("journal_input_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (journalText.isNotBlank()) {
                                    viewModel.addJournalEntry(journalText)
                                    journalText = ""
                                    selectedSpeechPresetIndex = -1
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ReforgeSurfaceVariant, contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("analyze_journal_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isJournaling
                        ) {
                            if (isJournaling) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyze & Journal (+100 XP)", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Journal Entries history list
        if (journalEntries.isNotEmpty()) {
            item {
                Text(
                    text = "HISTORICAL REFLECTIONS",
                    color = ReforgeTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(journalEntries) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedJournalId = if (expandedJournalId == entry.id) null else entry.id
                        }
                        .border(
                            width = 1.dp,
                            color = Color(0xFF79747E).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dateString = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                            Text(text = dateString, color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ReforgeLimeMuted)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = entry.emotionalAnalysis.ifEmpty { "Reflective" }, color = ReforgeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = entry.text, color = ReforgeTextMuted, maxLines = if (expandedJournalId == entry.id) Int.MAX_VALUE else 2, fontSize = 12.sp)

                        if (expandedJournalId == entry.id) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = ReforgeSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Gemini Cognitive Overview
                            if (entry.summary.isNotEmpty()) {
                                Text(text = "AI COGNITIVE REFLECTION:", color = ReforgeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(text = entry.summary, color = ReforgeTextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // WINS TODAY
                            if (entry.wins.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Wins", tint = ReforgeLime, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "WINS TODAY", color = ReforgeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = entry.wins, color = ReforgeTextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // AREAS FOR IMPROVEMENT / MISTAKES
                            if (entry.mistakes.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Mistakes", tint = ColorLung, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "AREAS FOR IMPROVEMENT / MISTAKES", color = ColorLung, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = entry.mistakes, color = ReforgeTextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // TRIGGERS IDENTIFIED
                            if (entry.triggersIdentified.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = "Triggers", tint = ColorPorn, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "TRIGGERS IDENTIFIED", color = ColorPorn, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = entry.triggersIdentified, color = ReforgeTextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // TOMORROW'S TARGET FOCUS
                            if (entry.tomorrowFocus.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = "Tomorrow Focus", tint = ColorBrain, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = "TOMORROW'S TARGET FOCUS", color = ColorBrain, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(text = entry.tomorrowFocus, color = ReforgeTextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Relapse history list (Module 3)
        if (relapseEvents.isNotEmpty()) {
            item {
                Text(
                    text = "RELAPSE DISSECTIONS",
                    color = ReforgeTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(relapseEvents) { event ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFF79747E).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${event.addiction} Lapse", color = ColorLung, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            val dateString = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(event.timestamp))
                            Text(text = dateString, color = ReforgeTextMuted, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Context: ${event.trigger} at ${event.location}", color = ReforgeTextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Emotion: ${event.emotion}", color = ReforgeTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = ReforgeSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "THERAPEUTIC REBUILD ACTION PLAN:", color = ReforgeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = event.preventionPlan, color = ReforgeTextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            // TESTING & SYSTEM CONTROLS
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            width = 1.dp,
                            color = ColorLung.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "TESTING & SYSTEM CONTROLS",
                            color = ColorLung,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                        Text(
                            text = "Reset all local telemetry, biometric profiles, workout protocols, habit schedules, and AI memory states back to a completely clean install state.",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = {
                                viewModel.resetProfile()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorLung, contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("reset_profile_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Reset Database",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Reset Profile & Onboarding", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Daily self check-in slider dialog
    if (showCheckInDialog) {
        val faceEmojis = listOf("😡", "😐", "🙂", "😃")
        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = {
                Text("How's your system today?", color = ReforgeTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Mood smiles faces
                    Column {
                        Text("Mood evaluation", color = ReforgeTextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            faceEmojis.forEachIndexed { index, emoji ->
                                val active = checkInMood == (index + 1)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (active) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                        .border(1.dp, if (active) ReforgeLime else Color.Transparent, CircleShape)
                                        .clickable { checkInMood = index + 1 },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    // Sliders
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Energy Level", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${checkInEnergy.toInt()} / 10", color = ReforgeLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = checkInEnergy,
                            onValueChange = { checkInEnergy = it },
                            valueRange = 1f..10f,
                            colors = SliderDefaults.colors(activeTrackColor = ReforgeLime, thumbColor = ReforgeLime, inactiveTrackColor = ReforgeSurfaceVariant)
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sleep Quality", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${checkInSleep.toInt()} / 10", color = ReforgeLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = checkInSleep,
                            onValueChange = { checkInSleep = it },
                            valueRange = 1f..10f,
                            colors = SliderDefaults.colors(activeTrackColor = ReforgeLime, thumbColor = ReforgeLime, inactiveTrackColor = ReforgeSurfaceVariant)
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Cravings Today", color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${checkInCravings.toInt()} / 10", color = ColorLung, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = checkInCravings,
                            onValueChange = { checkInCravings = it },
                            valueRange = 1f..10f,
                            colors = SliderDefaults.colors(activeTrackColor = ColorLung, thumbColor = ColorLung, inactiveTrackColor = ReforgeSurfaceVariant)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCheckIn(
                            mood = checkInMood,
                            energy = checkInEnergy.toInt(),
                            sleep = checkInSleep.toInt(),
                            cravings = checkInCravings.toInt()
                        )
                        showCheckInDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.White)
                ) {
                    Text("Save & Reforge")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDialog = false }) {
                    Text("Close", color = ReforgeTextMuted)
                }
            },
            containerColor = ReforgeSurface
        )
    }
}

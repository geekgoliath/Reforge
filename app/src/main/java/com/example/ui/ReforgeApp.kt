package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.UserProfile
import com.example.ui.screens.*
import com.example.ui.theme.*

sealed class ScreenTab(val route: String, val title: String, val icon: ImageVector) {
    object Today : ScreenTab("today", "Today", Icons.Default.Home)
    object Protocol : ScreenTab("protocol", "Protocol", Icons.Default.Description)
    object Coach : ScreenTab("coach", "Coach", Icons.Default.ChatBubble)
    object Journey : ScreenTab("journey", "Journey", Icons.Default.Autorenew)
    object Profile : ScreenTab("profile", "Profile", Icons.Default.Person)
}

@Composable
fun ReforgeApp(
    viewModel: ReforgeViewModel = viewModel()
) {
    val profile by viewModel.userProfile.collectAsState()
    val xpMessage by viewModel.xpAddedMessage.collectAsState()

    var currentTab by remember { mutableStateOf<ScreenTab>(ScreenTab.Today) }

    if (profile == null) {
        // App is loading DB initial values
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ReforgeBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ReforgeLime)
        }
    } else if (profile?.isOnboarded == false) {
        // Run Onboarding
        OnboardingLayout(viewModel)
    } else {
        // Main App Experience
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = ReforgeSurface,
                    windowInsets = WindowInsets.navigationBars,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        ScreenTab.Today,
                        ScreenTab.Protocol,
                        ScreenTab.Coach,
                        ScreenTab.Journey,
                        ScreenTab.Profile
                    )
                    tabs.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (selected) ReforgeLime else ReforgeTextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    color = if (selected) ReforgeLime else ReforgeTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ReforgeLimeMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.route}")
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            containerColor = ReforgeBg
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Swap layout screen tabs
                when (currentTab) {
                    ScreenTab.Today -> TodayScreen(viewModel)
                    ScreenTab.Protocol -> ProtocolScreen(viewModel)
                    ScreenTab.Coach -> CoachScreen(viewModel)
                    ScreenTab.Journey -> JourneyScreen(viewModel)
                    ScreenTab.Profile -> ProfileScreen(viewModel)
                }

                // Elegant floating Dopamine notification panel for XP gain
                AnimatedVisibility(
                    visible = xpMessage != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReforgeLime),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.dismissXpMessage() }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = xpMessage ?: "",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingLayout(viewModel: ReforgeViewModel) {
    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("35") }
    var weightText by remember { mutableStateOf("78.4") }
    var heightText by remember { mutableStateOf("178") }
    var dobText by remember { mutableStateOf("1991-03-15") }
    var birthTimeText by remember { mutableStateOf("14:30") }
    var birthPlaceText by remember { mutableStateOf("Delhi, India") }

    var neckText by remember { mutableStateOf("38.0") }
    var waistText by remember { mutableStateOf("90.0") }
    var activityLevel by remember { mutableStateOf("Moderate") }
    var alcoholFrequency by remember { mutableStateOf("Weekly") }
    var smokingFrequency by remember { mutableStateOf("Daily") }
    var sleepHoursText by remember { mutableStateOf("7.0") }

    val goalOptions = listOf(
        "Gain Muscle", "Lose Fat", "Quit Smoking", "Quit Alcohol", 
        "Improve Confidence", "Improve Memory", "Better Posture"
    )
    val selectedGoals = remember { mutableStateListOf("Gain Muscle", "Quit Smoking", "Quit Alcohol") }

    val struggleOptions = listOf("Alcohol", "Smoking", "Porn", "Phone Screen", "Sugar Junk")
    val selectedStruggles = remember { mutableStateListOf("Alcohol", "Smoking") }

    val isLoading by viewModel.isOnboardingLoading.collectAsState()
    val errorMsg by viewModel.onboardingError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ReforgeBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(ReforgeLimeMuted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = "Reforge logo",
                    tint = ReforgeLime,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = "Oath of Reforging",
                color = ReforgeTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Configure your identity. Build environment armor against addictions.",
                color = ReforgeTextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // SECTION 1: Cosmological Identity
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReforgeSurface)
                    .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. COSMOLOGICAL IDENTITY",
                    color = ReforgeLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What is your Name?") },
                    placeholder = { Text("Vikas") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ReforgeTextPrimary,
                        unfocusedTextColor = ReforgeTextPrimary,
                        focusedBorderColor = ReforgeLime,
                        unfocusedBorderColor = ReforgeSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_age_input")
                    )

                    OutlinedTextField(
                        value = dobText,
                        onValueChange = { dobText = it },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("YYYY-MM-DD") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1.8f)
                            .testTag("onboarding_dob_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = birthTimeText,
                        onValueChange = { birthTimeText = it },
                        label = { Text("Birth Time") },
                        placeholder = { Text("HH:MM") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_birthtime_input")
                    )

                    OutlinedTextField(
                        value = birthPlaceText,
                        onValueChange = { birthPlaceText = it },
                        label = { Text("Place of Birth") },
                        placeholder = { Text("Delhi, India") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("onboarding_birthplace_input")
                    )
                }
            }

            // SECTION 2: Biometric Composition
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReforgeSurface)
                    .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. BIOMETRIC COMPOSITION",
                    color = ReforgeLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_height_input")
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboarding_weight_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = neckText,
                        onValueChange = { neckText = it },
                        label = { Text("Neck Size (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                    )

                    OutlinedTextField(
                        value = waistText,
                        onValueChange = { waistText = it },
                        label = { Text("Waist Size (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ReforgeTextPrimary,
                            unfocusedTextColor = ReforgeTextPrimary,
                            focusedBorderColor = ReforgeLime,
                            unfocusedBorderColor = ReforgeSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                    )
                }

                Text(text = "Activity Level", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val levels = listOf("Sedentary", "Moderate", "Active", "Elite")
                    levels.forEach { lvl ->
                        val selected = activityLevel == lvl
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) ReforgeLime else ReforgeSurfaceVariant)
                                .clickable { activityLevel = lvl }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lvl,
                                color = if (selected) Color.Black else ReforgeTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // SECTION 3: Behavioral Frequencies
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReforgeSurface)
                    .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3. BEHAVIORAL FREQUENCIES",
                    color = ReforgeLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )

                Text(text = "Alcohol Frequency", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val freqs = listOf("Daily", "Weekly", "Social", "Abstinent")
                    freqs.forEach { frq ->
                        val selected = alcoholFrequency == frq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) ReforgeLime else ReforgeSurfaceVariant)
                                .clickable { alcoholFrequency = frq }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = frq,
                                color = if (selected) Color.Black else ReforgeTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(text = "Smoking Frequency", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val freqs = listOf("Daily", "Weekly", "Social", "Abstinent")
                    freqs.forEach { frq ->
                        val selected = smokingFrequency == frq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) ReforgeLime else ReforgeSurfaceVariant)
                                .clickable { smokingFrequency = frq }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = frq,
                                color = if (selected) Color.Black else ReforgeTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = sleepHoursText,
                    onValueChange = { sleepHoursText = it },
                    label = { Text("Average Sleep Hours") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ReforgeTextPrimary,
                        unfocusedTextColor = ReforgeTextPrimary,
                        focusedBorderColor = ReforgeLime,
                        unfocusedBorderColor = ReforgeSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SECTION 4: Active Reboot Struggles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReforgeSurface)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "4. SELECT ACTIVE REBOOT STRUGGLES:",
                    color = ReforgeLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                struggleOptions.forEach { struggle ->
                    val checked = selectedStruggles.contains(struggle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checked) selectedStruggles.remove(struggle) else selectedStruggles.add(struggle)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                if (checked) selectedStruggles.remove(struggle) else selectedStruggles.add(struggle)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = ReforgeLime)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = struggle, color = ReforgeTextPrimary, fontSize = 13.sp)
                    }
                }
            }

            // SECTION 5: Core Transformation Goals
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReforgeSurface)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF79747E).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "5. CORE TRANSFORMATION TARGETS:",
                    color = ReforgeLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                goalOptions.forEach { goal ->
                    val checked = selectedGoals.contains(goal)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (checked) selectedGoals.remove(goal) else selectedGoals.add(goal)
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                if (checked) selectedGoals.remove(goal) else selectedGoals.add(goal)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = ReforgeLime)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = goal, color = ReforgeTextPrimary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalName = if (name.isBlank()) "Vikas" else name
                    val age = ageText.toIntOrNull() ?: 35
                    val weight = weightText.toFloatOrNull() ?: 78.4f
                    val height = heightText.toFloatOrNull() ?: 178f
                    val neck = neckText.toFloatOrNull() ?: 38f
                    val waist = waistText.toFloatOrNull() ?: 90f
                    val sleep = sleepHoursText.toFloatOrNull() ?: 7f
                    val addictions = selectedStruggles.joinToString(",")
                    val goalsStr = selectedGoals.joinToString(",")

                    viewModel.updateProfile(
                        name = finalName,
                        age = age,
                        dob = dobText,
                        birthTime = birthTimeText,
                        birthPlace = birthPlaceText,
                        weight = weight,
                        height = height,
                        neck = neck,
                        waist = waist,
                        activityLevel = activityLevel,
                        alcoholFrequency = alcoholFrequency,
                        smokingFrequency = smokingFrequency,
                        sleepHours = sleep,
                        goals = goalsStr,
                        addictions = addictions
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_onboarding"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Forge My Oath", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ReforgeBg.copy(alpha = 0.95f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(color = ReforgeLime, modifier = Modifier.size(64.dp))
                Text(
                    text = "Forging Your 7-Day Reset Blueprint...",
                    color = ReforgeTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "The Personal Transformation Engine is compiling your biometric metrics, habit recovery paths, and cosmic alignments into your highly optimized local OS database.",
                    color = ReforgeTextMuted,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

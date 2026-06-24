package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ReforgeViewModel
import com.example.ui.theme.*
import com.example.data.Workout
import com.example.data.Meal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProtocolScreen(
    viewModel: ReforgeViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedSection by rememberSaveable { mutableStateOf("schedule") }
    
    val profile by viewModel.userProfile.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var savedFilePath by remember { mutableStateOf("") }
    var showRecoveryGuidance by rememberSaveable { mutableStateOf(false) }

    val daysList = remember {
        val list = mutableListOf<Pair<String, String>>()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        for (i in 0 until 7) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, i)
            val date = sdf.format(cal.time)
            val dayName = if (i == 0) "Today" else if (i == 1) "Tomorrow" else dayFormat.format(cal.time)
            list.add(date to "Day ${i+1} ($dayName)")
        }
        list
    }
    var activeDayIndex by remember { mutableStateOf(0) }

    val selectedDate = daysList[activeDayIndex].first
    val selectedDateLabel = daysList[activeDayIndex].second
    val workoutsFlow = remember(selectedDate) { viewModel.getWorkoutsForDate(selectedDate) }
    val mealsFlow = remember(selectedDate) { viewModel.getMealsForDate(selectedDate) }

    val workouts by workoutsFlow.collectAsState(initial = emptyList())
    val meals by mealsFlow.collectAsState(initial = emptyList())

    val isGeneratingWorkout by viewModel.isGeneratingWorkout.collectAsState()
    val generatedWorkoutPlan by viewModel.generatedWorkoutPlan.collectAsState()
    val workoutGenerationError by viewModel.workoutGenerationError.collectAsState()
    val lastGeneratedExerciseIds by viewModel.lastGeneratedExerciseIds.collectAsState()
    val allExercisesList by viewModel.allExercises.collectAsState(initial = emptyList())
    
    // States for interactive checklist items to persist during session
    val scheduleItems = remember {
        mutableStateListOf(
            ProtocolTask("08:00 AM", "Wake Up & Flush", "Drink 500ml warm water with Jeera/Ajwain + 1/2 Lemon.", "08:00_am"),
            ProtocolTask("08:00 AM", "Morning Supplements", "Take NAC (600mg) + Lion's Mane. Splash ice cold face wash.", "supps_morning"),
            ProtocolTask("08:20 AM", "Physical Prep", "10 Wall Angels & 20 Band Pull-Aparts for perfect posture.", "08:20_am"),
            ProtocolTask("08:30 AM", "Weight Training", "Go to gym (45 mins). Push / Pull / Legs rotation.", "08:30_am"),
            ProtocolTask("09:15 AM", "Anabolic Shake", "1 Scoop MuscleBlaze Biozyme Whey Protein.", "09:15_am"),
            ProtocolTask("10:00 AM", "Egg Breakfast", "3 Whole Eggs + 2 Egg Whites + Black Coffee. B-Complex & Multivitamin.", "10:00_am"),
            ProtocolTask("11:30 AM", "High-Protein Lunch", "Dal + 150g Chicken/Soya + 1 Chapati + Salad. Fish Oil & Liv.52.", "11:30_am"),
            ProtocolTask("12:00 PM", "Posture Calibration", "Adjust shoulder blades back, ears stacked directly over shoulders.", "12:00_pm"),
            ProtocolTask("03:00 PM", "Mid-Day Pivot", "Drink Buttermilk (Chaas) / Green Tea + 5 mins Box Breathing.", "03:00_pm"),
            ProtocolTask("06:00 PM", "Work Transition", "Eat 20g Roasted Chana/Peanuts + 5 mins Walk or stairs climbing.", "06:00_pm"),
            ProtocolTask("08:00 PM", "Carb-Cut Dinner", "Dal + 2 Boiled Egg Whites + Salad (Zero chapatis or heavy carbs).", "08:00_pm"),
            ProtocolTask("11:30 PM", "Sleep Induction Prep", "Warm milk with turmeric, black pepper, stevia + scalp massage.", "11:30_pm"),
            ProtocolTask("11:30 PM", "Bedtime Stack", "Take ZMA (Zinc, Magnesium, B6) + Ashwagandha capsule.", "supps_night")
        )
    }

    val supplementStack = remember {
        mutableStateListOf(
            SupplementItem("NAC (600mg)", "08:00 AM", "Liver detox & severe lung/nicotine craving block", "NAC"),
            SupplementItem("Lion's Mane", "08:00 AM", "Nerve Growth Factor (NGF) & brain recovery", "Lion"),
            SupplementItem("Neurobion Forte", "10:00 AM", "Therapeutic B-Complex for nervous system repair", "Neuro"),
            SupplementItem("Multivitamin", "10:00 AM", "Micronutrient buffer for muscle & endocrine health", "Multi"),
            SupplementItem("Fish Oil (1000mg)", "11:30 AM", "Omega-3 brain cell structural support", "Fish"),
            SupplementItem("Liv.52", "11:30 AM", "Herbal liver cellular regeneration support", "Liv"),
            SupplementItem("ZMA (Magnesium/Zinc)", "11:30 PM", "Induces deep REM recovery & sleep state regulation", "ZMA"),
            SupplementItem("Ashwagandha", "11:30 PM", "Cortisol crash prevention & baseline calm", "Ashwa")
        )
    }

    // PDF Export simulation states
    var isExportingPdf by remember { mutableStateOf(false) }
    var pdfProgress by remember { mutableStateOf(0f) }
    var exportCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(isExportingPdf) {
        if (isExportingPdf) {
            pdfProgress = 0f
            while (pdfProgress < 0.7f) {
                delay(150)
                pdfProgress += 0.15f
            }
            try {
                val file = com.example.util.PdfGenerator.generateProtocolPdf(context, profile ?: com.example.data.UserProfile())
                savedFilePath = file.name
            } catch (e: Exception) {
                e.printStackTrace()
                savedFilePath = "Error compiling PDF: ${e.localizedMessage}"
            }
            pdfProgress = 1.0f
            delay(200)
            isExportingPdf = false
            exportCompleted = true
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
        // Page Title Header
        item {
            Column {
                Text(
                    text = "Reforge Blueprint",
                    color = ReforgeTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Transformation Protocol (35M RESET)",
                    color = ReforgeLime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Recovery guidance is calm by default; details expand on request.
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = ReforgeSurfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRecoveryGuidance = !showRecoveryGuidance },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(ReforgeLimeMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = ReforgeLime,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recovery Guidance",
                                color = ReforgeTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You're in Week 2 of recovery.",
                                color = ReforgeTextMuted,
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = if (showRecoveryGuidance) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showRecoveryGuidance) "Collapse recovery guidance" else "Expand recovery guidance",
                            tint = ReforgeTextMuted
                        )
                    }

                    if (showRecoveryGuidance) {
                        val addictionsList = profile?.addictions?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                        val dynamicWarningText = if (addictionsList.isNotEmpty()) {
                            "${profile?.name?.ifBlank { "User" } ?: "User"}, you are reducing ${addictionsList.joinToString(" and ")}. Keep the next steps steady, simple, and supported."
                        } else {
                            "${profile?.name?.ifBlank { "User" } ?: "User"}, you are in an active recovery reset. Keep the next steps steady, simple, and supported."
                        }
                    Text(
                        text = dynamicWarningText,
                        color = ReforgeTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ReforgeSurfaceVariant.copy(alpha = 0.7f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "When to get urgent help",
                            color = ReforgeTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Seek medical help immediately for severe tremors, hallucinations, confusion, high fever, or seizures.",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Text(
                        text = "Week 2 can still feel uneven. Prioritize sleep, hydration, meals, light movement, and support from someone you trust.",
                        color = ReforgeTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    } else {
                        Text(
                            text = "Expand",
                            color = ReforgeLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showRecoveryGuidance = true }
                        )
                    }
                }
            }
        }

        // Section Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = when (selectedSection) {
                    "schedule" -> 0
                    "meals" -> 1
                    "workout" -> 2
                    "pdf" -> 3
                    else -> 0
                },
                containerColor = Color.Transparent,
                contentColor = ReforgeLime,
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                TabItem(
                    title = "📋 Everyday Schedule",
                    selected = selectedSection == "schedule",
                    onClick = { selectedSection = "schedule" }
                )
                TabItem(
                    title = "🥦 Raw Meals Plan",
                    selected = selectedSection == "meals",
                    onClick = { selectedSection = "meals" }
                )
                TabItem(
                    title = "💪 Custom Workouts",
                    selected = selectedSection == "workout",
                    onClick = { selectedSection = "workout" }
                )
                TabItem(
                    title = "📄 PDF Manual",
                    selected = selectedSection == "pdf",
                    onClick = { selectedSection = "pdf" }
                )
            }
        }

        // Content Renderer based on selectedSection
        when (selectedSection) {
            "schedule" -> {
                item {
                    Text(
                        text = "MON-FRI SCHEDULE CHECKLIST",
                        color = ReforgeTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }
                
                items(scheduleItems) { task ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.completed) ReforgeSurfaceVariant.copy(alpha = 0.4f) else ReforgeSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (task.completed) ReforgeLime.copy(alpha = 0.15f) else Color(0xFF79747E).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                val idx = scheduleItems.indexOf(task)
                                if (idx != -1) {
                                    scheduleItems[idx] = task.copy(completed = !task.completed)
                                    if (!task.completed) {
                                        viewModel.awardXp(15, "Completed ${task.title}")
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (task.completed) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = task.time,
                                    color = if (task.completed) ReforgeLime else ReforgeTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = if (task.completed) ReforgeTextMuted else ReforgeTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = task.desc,
                                    color = ReforgeTextMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            Icon(
                                imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Complete",
                                tint = if (task.completed) ReforgeLime else ReforgeTextMuted,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }

            "meals" -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "7-DAY DYNAMIC MEALS PLAN",
                            color = ReforgeLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                        Text(
                            text = "Select any day below to see the exact raw weight food quantities generated for your transformation.",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                item {
                    var isConfiguratorExpanded by remember { mutableStateOf(false) }

                    // Local Inputs
                    val weightVal = remember { mutableStateOf(78.4f) }
                    val heightVal = remember { mutableStateOf(178f) }
                    val ageVal = remember { mutableStateOf(35) }
                    val sexVal = remember { mutableStateOf("Male") }
                    val activityLevelVal = remember { mutableStateOf("Moderate") }
                    val proteinCoeffVal = remember { mutableStateOf(2.0f) }
                    val fatCoeffVal = remember { mutableStateOf(0.9f) }

                    LaunchedEffect(profile) {
                        profile?.let {
                            weightVal.value = it.weight
                            heightVal.value = it.height
                            ageVal.value = it.age
                            activityLevelVal.value = it.activityLevel
                        }
                    }

                    val actMultiplier = when (activityLevelVal.value) {
                        "Sedentary" -> 1.2f
                        "Moderate" -> 1.375f
                        "Active" -> 1.55f
                        "Elite" -> 1.725f
                        else -> 1.375f
                    }

                    val bmrVal = if (sexVal.value == "Male") {
                        (10f * weightVal.value) + (6.25f * heightVal.value) - (5f * ageVal.value) + 5f
                    } else {
                        (10f * weightVal.value) + (6.25f * heightVal.value) - (5f * ageVal.value) - 161f
                    }

                    val tdeeVal = bmrVal * actMultiplier
                    val protGVal = weightVal.value * proteinCoeffVal.value
                    val fatGVal = weightVal.value * fatCoeffVal.value
                    val remainingCals = (tdeeVal - (protGVal * 4f) - (fatGVal * 9f)).coerceAtLeast(0f)
                    val carbGVal = remainingCals / 4f

                    val foodOpts = remember {
                        listOf(
                            "Whey Protein", "Egg Whites", "Whole Eggs", "Chicken Breast",
                            "Paneer", "Soya Chunks", "Oats", "Basmati Rice",
                            "Dal", "Bananas", "Fish Fillet", "Mixed Salad", "Roasted Chana"
                        )
                    }
                    val selFoods = remember {
                        mutableStateListOf("Whey Protein", "Egg Whites", "Whole Eggs", "Chicken Breast", "Oats", "Basmati Rice", "Dal", "Mixed Salad")
                    }

                    val isGenerating by viewModel.isGeneratingNutrition.collectAsState()
                    val explanationText by viewModel.nutritionExplanation.collectAsState()
                    val nutritionErrorText by viewModel.nutritionError.collectAsState()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isConfiguratorExpanded) ReforgeLime.copy(alpha = 0.3f) else Color(0xFF79747E).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header row with toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isConfiguratorExpanded = !isConfiguratorExpanded }
                                    .testTag("toggle_nutrition_configurator"),
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
                                            .background(ReforgeLime.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = ReforgeLime,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "AI NUTRITION ENGINE",
                                            color = ReforgeTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mifflin-St Jeor locally calculated",
                                            color = ReforgeTextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (isConfiguratorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isConfiguratorExpanded) "Collapse" else "Expand",
                                    tint = ReforgeLime
                                )
                            }

                            if (isConfiguratorExpanded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = ReforgeSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))

                                // Biometrics Inputs Title
                                Text(
                                    text = "1. BIOMETRIC PARAMETERS",
                                    color = ReforgeLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Weight, Height inputs row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Weight
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Weight (kg)", color = ReforgeTextMuted, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = weightVal.value.toString(),
                                            onValueChange = {
                                                weightVal.value = it.toFloatOrNull() ?: weightVal.value
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = ReforgeTextPrimary,
                                                unfocusedTextColor = ReforgeTextPrimary,
                                                focusedBorderColor = ReforgeLime,
                                                unfocusedBorderColor = ReforgeSurfaceVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("weight_input_field"),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )
                                    }

                                    // Height
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Height (cm)", color = ReforgeTextMuted, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = heightVal.value.toString(),
                                            onValueChange = {
                                                heightVal.value = it.toFloatOrNull() ?: heightVal.value
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = ReforgeTextPrimary,
                                                unfocusedTextColor = ReforgeTextPrimary,
                                                focusedBorderColor = ReforgeLime,
                                                unfocusedBorderColor = ReforgeSurfaceVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("height_input_field"),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Age and Sex row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Age
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Age (years)", color = ReforgeTextMuted, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = ageVal.value.toString(),
                                            onValueChange = {
                                                ageVal.value = it.toIntOrNull() ?: ageVal.value
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = ReforgeTextPrimary,
                                                unfocusedTextColor = ReforgeTextPrimary,
                                                focusedBorderColor = ReforgeLime,
                                                unfocusedBorderColor = ReforgeSurfaceVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("age_input_field"),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )
                                    }

                                    // Biological Sex
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Biological Sex", color = ReforgeTextMuted, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(ReforgeSurfaceVariant)
                                                .padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf("Male", "Female").forEach { sex ->
                                                val active = sexVal.value == sex
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (active) ReforgeLime else Color.Transparent)
                                                        .clickable { sexVal.value = sex }
                                                        .testTag("sex_toggle_$sex"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = sex,
                                                        color = if (active) Color.Black else ReforgeTextPrimary,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Activity level selector
                                Column {
                                    Text("Activity Level", color = ReforgeTextMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ReforgeSurfaceVariant)
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("Sedentary", "Moderate", "Active", "Elite").forEach { level ->
                                            val active = activityLevelVal.value == level
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (active) ReforgeLime else Color.Transparent)
                                                    .clickable { activityLevelVal.value = level }
                                                    .testTag("activity_level_$level"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = level,
                                                    color = if (active) Color.Black else ReforgeTextPrimary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = ReforgeSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))

                                // Coefficients Sliders Title
                                Text(
                                    text = "2. MACRO COEFFICIENTS",
                                    color = ReforgeLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Protein slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Protein Ratio Coefficient", color = ReforgeTextPrimary, fontSize = 12.sp)
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.1f", proteinCoeffVal.value)} g/kg",
                                            color = ReforgeLime,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Slider(
                                        value = proteinCoeffVal.value,
                                        onValueChange = { proteinCoeffVal.value = it },
                                        valueRange = 1.6f..2.2f,
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = ReforgeLime,
                                            thumbColor = ReforgeLime,
                                            inactiveTrackColor = ReforgeSurfaceVariant
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Fat slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Fat Ratio Coefficient", color = ReforgeTextPrimary, fontSize = 12.sp)
                                        Text(
                                            text = "${String.format(java.util.Locale.US, "%.1f", fatCoeffVal.value)} g/kg",
                                            color = ReforgeLime,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Slider(
                                        value = fatCoeffVal.value,
                                        onValueChange = { fatCoeffVal.value = it },
                                        valueRange = 0.8f..1.0f,
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = ReforgeLime,
                                            thumbColor = ReforgeLime,
                                            inactiveTrackColor = ReforgeSurfaceVariant
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Local Calculations Display
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151515)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, ReforgeSurfaceVariant, RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Assessment, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "LOCAL METABOLIC ENGINE CALCULATIONS",
                                                color = ReforgeLime,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // BMR calculation details
                                            val sexTerm = if (sexVal.value == "Male") "+ 5" else "- 161"
                                            Text(
                                                text = "BMR formula (Mifflin-St Jeor):",
                                                color = ReforgeTextMuted,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "BMR = (10 * ${weightVal.value}) + (6.25 * ${heightVal.value}) - (5 * ${ageVal.value}) $sexTerm",
                                                color = ReforgeTextPrimary,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Computed Base BMR:", color = ReforgeTextMuted, fontSize = 12.sp)
                                                Text("${bmrVal.toInt()} kcal/day", color = ReforgeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Divider(color = ReforgeSurfaceVariant)

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // TDEE calculation details
                                            Text(
                                                text = "TDEE formula (Activity multiplier):",
                                                color = ReforgeTextMuted,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = "TDEE = BMR * $actMultiplier",
                                                color = ReforgeTextPrimary,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Total Daily Energy Expenditure:", color = ReforgeTextMuted, fontSize = 12.sp)
                                                Text("${tdeeVal.toInt()} kcal/day", color = ReforgeLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Divider(color = ReforgeSurfaceVariant)

                                        // Macromolecules Breakdown
                                        Text("Target Daily Macronutrients:", color = ReforgeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Calories column
                                            MacroBreakdownColumn(
                                                modifier = Modifier.weight(1f),
                                                label = "Calories",
                                                gVal = "",
                                                calVal = "${tdeeVal.toInt()} kcal",
                                                percent = "100%"
                                            )

                                            // Protein column
                                            MacroBreakdownColumn(
                                                modifier = Modifier.weight(1f),
                                                label = "Protein",
                                                gVal = "${protGVal.toInt()}g",
                                                calVal = "${(protGVal * 4).toInt()} kcal",
                                                percent = "${((protGVal * 4 / tdeeVal) * 100).toInt()}%"
                                            )

                                            // Carbs column
                                            MacroBreakdownColumn(
                                                modifier = Modifier.weight(1f),
                                                label = "Carbs",
                                                gVal = "${carbGVal.toInt()}g",
                                                calVal = "${(carbGVal * 4).toInt()} kcal",
                                                percent = "${((carbGVal * 4 / tdeeVal) * 100).toInt()}%"
                                            )

                                            // Fat column
                                            MacroBreakdownColumn(
                                                modifier = Modifier.weight(1f),
                                                label = "Fat",
                                                gVal = "${fatGVal.toInt()}g",
                                                calVal = "${(fatGVal * 9).toInt()} kcal",
                                                percent = "${((fatGVal * 9 / tdeeVal) * 100).toInt()}%"
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = ReforgeSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))

                                // Food Selection Checklist Title
                                Text(
                                    text = "3. AVAILABLE FOODS LIST",
                                    color = ReforgeLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gemini will construct your custom meals choosing strictly from these selected items.",
                                    color = ReforgeTextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Food option chips flow
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    foodOpts.forEach { food ->
                                        val selected = selFoods.contains(food)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) ReforgeLimeMuted else ReforgeSurfaceVariant)
                                                .border(1.dp, if (selected) ReforgeLime else Color.Transparent, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    if (selected) {
                                                        selFoods.remove(food)
                                                    } else {
                                                        selFoods.add(food)
                                                    }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                .testTag("food_chip_$food"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = food,
                                                    color = if (selected) ReforgeLime else ReforgeTextPrimary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (selected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Gemini call results
                                if (nutritionErrorText != null) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = ColorLung.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, ColorLung, RoundedCornerShape(8.dp))
                                    ) {
                                        Text(
                                            text = nutritionErrorText ?: "",
                                            color = ColorLung,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                if (explanationText.isNotEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = ReforgeLimeMuted.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, ReforgeLime.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "GEMINI STRUCTURAL EXPLANATION:",
                                                    color = ReforgeLime,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = explanationText,
                                                color = ReforgeTextPrimary,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Call to action Button
                                Button(
                                    onClick = {
                                        val foodListString = selFoods.joinToString(", ")
                                        viewModel.generateAndSaveNutritionMeals(
                                            date = selectedDate,
                                            calories = tdeeVal,
                                            protein = protGVal,
                                            carbs = carbGVal,
                                            fat = fatGVal,
                                            foodList = foodListString,
                                            goals = profile?.goals ?: "Quit addictions, build muscle",
                                            activity = activityLevelVal.value
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                                    modifier = Modifier.fillMaxWidth().testTag("forge_meals_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isGenerating && selFoods.isNotEmpty()
                                ) {
                                    if (isGenerating) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Forging exact amino-acid profiles...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("Forge Custom Meal Plan (+150 XP)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    // Day Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        daysList.forEachIndexed { index, pair ->
                            val selected = activeDayIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) ReforgeLime else ReforgeSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) ReforgeLime else Color(0xFF79747E).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { activeDayIndex = index }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = pair.second,
                                    color = if (selected) Color.Black else ReforgeTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (meals.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = ReforgeTextMuted, modifier = Modifier.size(32.dp))
                                Text(
                                    text = "No custom meals found in local OS database.",
                                    color = ReforgeTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "When you complete onboarding, our Personal Transformation Engine automatically compiles and records your exact custom meal schedules.",
                                    color = ReforgeTextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(meals) { meal ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (meal.isEaten) ReforgeSurfaceVariant.copy(alpha = 0.4f) else ReforgeSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (meal.isEaten) ReforgeLime.copy(alpha = 0.15f) else Color(0xFF79747E).copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.toggleMeal(meal) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (meal.isEaten) ReforgeLimeMuted else ReforgeSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = "Meal icon",
                                        tint = if (meal.isEaten) ReforgeLime else ReforgeTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = meal.name,
                                            color = if (meal.isEaten) ReforgeTextMuted else ReforgeTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (meal.isEaten) TextDecoration.LineThrough else null
                                        )
                                        if (meal.protein > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ReforgeLimeMuted)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${meal.protein.toInt()}g P",
                                                    color = ReforgeLime,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (meal.carbs > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ReforgeSurfaceVariant)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${meal.carbs.toInt()}g C",
                                                    color = ReforgeTextPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (meal.fat > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ReforgeSurfaceVariant)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${meal.fat.toInt()}g F",
                                                    color = ReforgeTextMuted,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        if (meal.calories > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ReforgeSurfaceVariant)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${meal.calories.toInt()} kcal",
                                                    color = ReforgeLime,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = meal.description,
                                        color = ReforgeTextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp
                                    )
                                }

                                Icon(
                                    imageVector = if (meal.isEaten) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Mark eaten",
                                    tint = if (meal.isEaten) ReforgeLime else ReforgeTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            "workout" -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "7-DAY DYNAMIC WORKOUT SPLIT",
                            color = ReforgeLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.0.sp
                        )
                        Text(
                            text = "Select any day below to see the custom physical training routine generated for you.",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                item {
                    // Day Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        daysList.forEachIndexed { index, pair ->
                            val selected = activeDayIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) ReforgeLime else ReforgeSurface)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) ReforgeLime else Color(0xFF79747E).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { activeDayIndex = index }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = pair.second,
                                    color = if (selected) Color.Black else ReforgeTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(
                                width = 1.dp,
                                color = ReforgeLime.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = ReforgeLime,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "AI WORKOUT FORGE",
                                    color = ReforgeLime,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = "Construct a custom posture-focused routine strictly using the local physical exercise database. Gemini only assembles plans and NEVER invents random movements.",
                                color = ReforgeTextMuted,
                                fontSize = 11.sp
                            )
                            
                            // Specs / Requirements Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val chips = listOf("⏱️ 45 Min", "🌱 Beginner", "🧘 Posture", "💪 Muscle Gain")
                                chips.forEach { chipText ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ReforgeSurfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = chipText, color = ReforgeTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            if (isGeneratingWorkout) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CircularProgressIndicator(color = ReforgeLime, modifier = Modifier.size(24.dp))
                                        Text(text = "Assembling plan from database...", color = ReforgeTextMuted, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.generateWorkoutPlan(selectedDate)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("forge_posture_workout_button")
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Forge Posture Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            workoutGenerationError?.let { err ->
                                Text(text = err, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            generatedWorkoutPlan?.let { explanation ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ReforgeSurfaceVariant)
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "🎯 ASSEMBLY LOGS",
                                        color = ReforgeLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = explanation,
                                        color = ReforgeTextPrimary,
                                        fontSize = 11.sp
                                    )
                                    if (lastGeneratedExerciseIds.isNotEmpty()) {
                                        Text(
                                            text = "Selected IDs: " + lastGeneratedExerciseIds.joinToString(", "),
                                            color = ReforgeLime,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (workouts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = ReforgeTextMuted, modifier = Modifier.size(32.dp))
                                Text(
                                    text = "No custom exercises found in local OS database.",
                                    color = ReforgeTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "When you complete onboarding, our Personal Transformation Engine automatically compiles and records your custom daily workouts.",
                                    color = ReforgeTextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    item {
                        val workoutType = workouts.firstOrNull()?.name ?: "CUSTOM SPLIT"
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ReforgeLimeMuted),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, ReforgeLime.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = ReforgeLime, modifier = Modifier.size(24.dp))
                                Column {
                                    Text(
                                        text = workoutType,
                                        color = ReforgeTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Targeted movement window designed to stabilize raw neurotransmitters.",
                                        color = ReforgeTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    items(workouts) { workout ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (workout.isCompleted) ReforgeSurfaceVariant.copy(alpha = 0.4f) else ReforgeSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (workout.isCompleted) ReforgeLime.copy(alpha = 0.15f) else Color(0xFF79747E).copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.toggleWorkout(workout) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (workout.isCompleted) ReforgeLimeMuted else ReforgeSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = "Workout icon",
                                        tint = if (workout.isCompleted) ReforgeLime else ReforgeTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = workout.exerciseName,
                                        color = if (workout.isCompleted) ReforgeTextMuted else ReforgeTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = if (workout.isCompleted) TextDecoration.LineThrough else null
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${workout.sets} Sets x ${workout.reps} Reps" + if (workout.weight > 0) " @ ${workout.weight} kg" else "",
                                        color = ReforgeTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Icon(
                                    imageVector = if (workout.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle Complete",
                                    tint = if (workout.isCompleted) ReforgeLime else ReforgeTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📖 EXERCISE DATABASE (${allExercisesList.size} ITEMS)",
                            color = ReforgeLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Below are the verified exercises stored in your local operating system database. Gemini must strictly pair from these exercises to compile a routine.",
                            color = ReforgeTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                items(allExercisesList) { exercise ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF79747E).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = exercise.name,
                                        color = ReforgeTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ReforgeLimeMuted)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = exercise.difficulty,
                                            color = ReforgeLime,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Target: ${exercise.body_part} | Equipment: ${exercise.equipment}",
                                    color = ReforgeTextMuted,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "ID: ${exercise.exercise_id}",
                                    color = ReforgeLime,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            val intentContext = androidx.compose.ui.platform.LocalContext.current
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(exercise.video_url))
                                        intentContext.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Handled
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Watch video tutorial",
                                    tint = ReforgeLime,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            "pdf" -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ReforgeSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color(0xFF79747E).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF document",
                                tint = ColorLung,
                                modifier = Modifier.size(54.dp)
                            )

                            Text(
                                text = "Transformation_Protocol.pdf",
                                color = ReforgeTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Generate and export a beautifully formatted, print-ready, professional PDF containing your complete 35M RESET Master Schedule, Supplement Stack, and PPL routines.",
                                color = ReforgeTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            if (isExportingPdf) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { pdfProgress },
                                        color = ReforgeLime,
                                        trackColor = ReforgeSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                    )
                                    Text(
                                        text = "Baking PDF document elements... ${(pdfProgress * 100).toInt()}%",
                                        color = ReforgeLime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { isExportingPdf = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ReforgeLime, contentColor = Color.White),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Download")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Simulate PDF Compilation", fontWeight = FontWeight.Bold)
                                }
                            }

                            // Success Dialog
                            if (exportCompleted) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ReforgeLimeMuted),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, ReforgeLime, RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = ReforgeLime,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "PDF Generated Successfully!",
                                                color = ReforgeTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Saved file as '$savedFilePath' in app directories. High-fidelity cosmic alignment blueprint complete.",
                                                color = ReforgeTextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                        IconButton(onClick = { exportCompleted = false }) {
                                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = ReforgeTextPrimary, modifier = Modifier.size(16.dp))
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
}

@Composable
fun TabItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) ReforgeLimeMuted else ReforgeSurface)
            .border(
                width = 1.dp,
                color = if (selected) ReforgeLime else Color(0xFF79747E).copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) ReforgeLime else ReforgeTextMuted,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun WorkoutDayItem(
    title: String,
    routines: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = ReforgeTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        routines.forEach { r ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ReforgeLime)
                )
                Text(
                    text = r,
                    color = ReforgeTextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class ProtocolTask(
    val time: String,
    val title: String,
    val desc: String,
    val tag: String,
    val completed: Boolean = false
)

data class SupplementItem(
    val name: String,
    val time: String,
    val purpose: String,
    val tag: String,
    val taken: Boolean = false
)

@Composable
fun MacroBreakdownColumn(
    modifier: Modifier = Modifier,
    label: String,
    gVal: String,
    calVal: String,
    percent: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ReforgeSurfaceVariant)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, color = ReforgeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        if (gVal.isNotEmpty()) {
            Text(text = gVal, color = ReforgeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = calVal, color = ReforgeTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text(text = percent, color = ReforgeTextMuted, fontSize = 10.sp)
    }
}

package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.UserProfile
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private val primaryColor = Color.parseColor("#1B5E20")    // Forest Green
    private val secondaryColor = Color.parseColor("#2E7D32")  // Medium Green
    private val accentColor = Color.parseColor("#00E676")     // Bright Neon Green
    private val textColor = Color.parseColor("#212121")       // Dark Charcoal
    private val textMutedColor = Color.parseColor("#666666")  // Slate Gray
    private val errorColor = Color.parseColor("#D32F2F")      // Medical Coral Red
    private val warningBgColor = Color.parseColor("#FFEBEE")  // Light red container

    fun generateProtocolPdf(context: Context, profile: UserProfile): File {
        val pdfDocument = PdfDocument()

        // Page 1: Header, Medical Alert, Cosmic Horoscope, Supplements
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = pdfDocument.startPage(pageInfo1)
        drawPage1(page1.canvas, profile)
        pdfDocument.finishPage(page1)

        // Page 2: Days 1 to 4 Full Day Schedule with exact raw weights
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = pdfDocument.startPage(pageInfo2)
        drawPage2(page2.canvas, profile)
        pdfDocument.finishPage(page2)

        // Page 3: Days 5 to 7 Full Day Schedule + Workout Details & Urge Protocol
        val pageInfo3 = PdfDocument.PageInfo.Builder(595, 842, 3).create()
        val page3 = pdfDocument.startPage(pageInfo3)
        drawPage3(page3.canvas, profile)
        pdfDocument.finishPage(page3)

        val file = File(context.getExternalFilesDir(null), "Reforge_35M_Transformation_Protocol.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    private fun drawPage1(canvas: Canvas, profile: UserProfile) {
        val paint = Paint()
        var y = 30f

        // 1. Dark Top Banner
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Header Text
        drawText(canvas, "REFORGE OPERATING MANUAL", 30f, 20f, 535, 18f, true, Color.WHITE, Layout.Alignment.ALIGN_CENTER)
        drawText(canvas, "35M RESET TOTAL TRANSFORMATION PROTOCOL • ACTIVE BLUEPRINT", 30f, 48f, 535, 10f, true, Color.parseColor("#A5D6A7"), Layout.Alignment.ALIGN_CENTER)

        y = 110f

        // 2. Patient Profile Box (Left side) & Cosmic Horoscope Details (Right side)
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(30f, y, 275f, y + 120f, 10f, 10f, paint) // Left Profile Box
        canvas.drawRoundRect(295f, y, 565f, y + 120f, 10f, 10f, paint) // Right Horoscope Box

        // Draw Left Profile Text
        var py = y + 10f
        drawText(canvas, "USER IDENTITY METRICS", 40f, py, 215, 10f, true, primaryColor)
        py += 15f
        drawText(canvas, "• Name: ${profile.name}", 40f, py, 215, 9f, false, textColor)
        py += 13f
        drawText(canvas, "• Age: ${profile.age} years", 40f, py, 215, 9f, false, textColor)
        py += 13f
        drawText(canvas, "• Weight: ${profile.weight} kg", 40f, py, 215, 9f, false, textColor)
        py += 13f
        drawText(canvas, "• Height: ${profile.height} cm", 40f, py, 215, 9f, false, textColor)
        py += 13f
        drawText(canvas, "• Core Reboot: ${profile.addictions}", 40f, py, 215, 9f, true, errorColor)

        // Draw Right Horoscope Cosmic Alignment
        var hy = y + 10f
        drawText(canvas, "COSMIC HOROSCOPE & INSIGHTS", 305f, hy, 250, 10f, true, secondaryColor)
        hy += 15f
        drawText(canvas, "• DOB: ${profile.dob}  • Time: ${profile.birthTime}", 305f, hy, 250, 9f, false, textColor)
        hy += 13f
        drawText(canvas, "• Birth Place: ${profile.birthPlace}", 305f, hy, 250, 9f, false, textColor)
        hy += 13f
        val insights = "Pisces/Virgo axis aligned with Saturn in 6th house. Highly optimized planetary windows support severe physical purging of toxic habits (Nicotine & Alcohol). Focus is raw discipline over volatile motivation."
        drawText(canvas, insights, 305f, hy, 250, 8f, false, textMutedColor)

        y += 140f

        // 3. Medical Emergency Alert (High Impact Warn Block)
        paint.color = warningBgColor
        canvas.drawRoundRect(30f, y, 565f, y + 115f, 10f, 10f, paint)
        paint.color = errorColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(30f, y, 565f, y + 115f, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        var wy = y + 10f
        drawText(canvas, "🚨 CRITICAL MEDICAL SAFETY WARNING", 40f, wy, 505, 11f, true, errorColor)
        wy += 16f
        val warningBody = "Vikas, you are quitting alcohol and smoking cold turkey after 10 years of chronic use. This creates severe physical & neurochemical adjustments. Monitor your system hourly.\n\n" +
                "EMERGENCY RED FLAGS: If you experience severe hand tremors, visual/auditory hallucinations, high fever, or seizures, go to the Emergency Room (ER) immediately. These indicate Delirium Tremens, a high-severity emergency."
        drawText(canvas, warningBody, 40f, wy, 505, 8.5f, false, textColor)

        y += 135f

        // 4. Targeted Supplement Stack Table
        drawText(canvas, "I. TARGETED RE-NEURALIZATION SUPPLEMENT STACK", 30f, y, 535, 12f, true, primaryColor)
        y += 18f

        // Table headers
        paint.color = Color.parseColor("#E8F5E9")
        canvas.drawRect(30f, y, 565f, y + 20f, paint)
        drawText(canvas, "Supplement", 35f, y + 4f, 140, 9f, true, primaryColor)
        drawText(canvas, "Dosage & Timing", 185f, y + 4f, 140, 9f, true, primaryColor)
        drawText(canvas, "Neuro-Nutritional Biological Purpose", 335f, y + 4f, 220, 9f, true, primaryColor)
        y += 20f

        val supplements = listOf(
            Triple("NAC (N-Acetyl Cysteine)", "600mg @ 08:00 AM (Empty)", "Protects lung cells; blocks severe smoking/nicotine cravings."),
            Triple("Lion's Mane Mushroom", "1 Capsule @ 08:00 AM", "Triggers NGF to repair nerve pathways and brain cognitive centers."),
            Triple("Neurobion Forte (Vit B)", "1 Tab @ 10:00 AM (Breakfast)", "Therapeutic high-dose B1, B6, B12 to rebuild myelin sheaths."),
            Triple("Multivitamin Supplement", "1 Tab @ 10:00 AM (Breakfast)", "Essential trace minerals to buffer heavy weight training."),
            Triple("Fish Oil (1000mg)", "1 Cap @ 11:30 AM (Lunch)", "Omega-3 EPA/DHA to combat chronic systematic cellular inflammation."),
            Triple("Liv.52 tablet", "1 Tab @ 11:30 AM (Lunch)", "Accelerates recovery from chronic hepatic-alcohol overload."),
            Triple("ZMA (Mag/Zinc/B6)", "1 Cap @ 11:30 PM (Bedtime)", "Forces sleep state restoration and neural muscular recovery."),
            Triple("Ashwagandha Capsule", "1 Cap @ 11:30 PM (Bedtime)", "Down-regulates high stress levels; balances sleep neurotransmitters.")
        )

        supplements.forEachIndexed { idx, item ->
            paint.color = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#F9F9F9")
            canvas.drawRect(30f, y, 565f, y + 22f, paint)
            drawText(canvas, item.first, 35f, y + 5f, 140, 8.5f, true, textColor)
            drawText(canvas, item.second, 185f, y + 5f, 140, 8.5f, false, textColor)
            drawText(canvas, item.third, 335f, y + 5f, 220, 8f, false, textMutedColor)
            y += 22f
        }

        y += 15f
        // Watermark / Footer
        drawText(canvas, "CONFIDENTIAL PROTOCOL • GENERATED FOR SYSTEM REFORGE • PAGE 1 OF 3", 30f, 810f, 535, 8f, false, textMutedColor, Layout.Alignment.ALIGN_CENTER)
    }

    private fun drawPage2(canvas: Canvas, profile: UserProfile) {
        val paint = Paint()
        var y = 30f

        // Header Title
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 50f, paint)
        drawText(canvas, "35M RESET: 7-DAY TRANSFORMATION PROGRAM (DAYS 1 - 4)", 30f, 18f, 535, 14f, true, Color.WHITE, Layout.Alignment.ALIGN_CENTER)

        y = 70f
        drawText(canvas, "II. INDIVIDUAL DAILY TIMELINES & MEAL QUANTITIES", 30f, y, 535, 12f, true, primaryColor)
        y += 18f

        // Days list
        val days1to4 = listOf(
            Triple(
                "DAY 1: PUSH DAY (Neuromuscular Activation)",
                "08:00 AM FLUSH: 500ml warm water + 5g raw Jeera/Ajwain + 1/2 Lemon.\n" +
                        "10:00 AM BREAKFAST: 3 Whole Eggs (150g) + 2 Whites scrambled with 5ml olive oil + Black Coffee. (Supps: Neurobion, Multi).\n" +
                        "11:30 AM LUNCH: 120g raw Dal (boiled) + 30g raw Basmati Rice + 150g raw Soya chunks + Green Salad. (Supps: Fish Oil, Liv.52).\n" +
                        "03:00 PM MID-DAY PIVOT: 250ml fresh Buttermilk (Chaas) with black salt + 5 mins Box Breathing.\n" +
                        "06:00 PM URGE DEFENSE: 30g raw unpeeled Peanuts (roasted, strictly unsalted) OR 1 medium Banana (100g).\n" +
                        "08:00 PM DINNER: 120g raw Dal (cooked thin, zero chapati) + 2 Boiled egg whites + Cucumber Salad.\n" +
                        "11:30 PM SLEEP STACK: 200ml warm turmeric milk + stevia. ZMA + Ashwagandha.",
                "WORKOUT (08:30 AM): PUSH splits - Flat Dumbbell Press (3 Sets x 10 rep), Overhead Press (3x8), Incline Flyes (3x12), Tricep pushdowns (3x15). Keep intense under 45 mins!"
            ),
            Triple(
                "DAY 2: PULL DAY (Posterior Chain Load & Detox)",
                "08:00 AM FLUSH: 500ml warm water + 5g raw Jeera/Ajwain + 1/2 Lemon.\n" +
                        "10:00 AM BREAKFAST: 3 Whole Eggs + 2 Whites scrambled + Black Coffee. (Supps: B-Complex, Multi).\n" +
                        "11:30 AM LUNCH: 120g raw Dal + 30g raw Basmati Rice + 150g grilled Chicken breast + cucumber. (Supps: Fish Oil, Liv.52).\n" +
                        "03:00 PM PIVOT: 250ml Chaas + 5 mins Deep Diaphragmatic Breathing.\n" +
                        "06:00 PM COGNITIVE SHIELD: 30g Roasted Unsalted Chana (gram) with 1 glass green tea.\n" +
                        "08:00 PM DINNER: 120g raw Dal soup + 150g paneer chunks grilled without oil + Tomato salad.\n" +
                        "11:30 PM SLEEP: 200ml Turmeric milk. Take ZMA + Ashwagandha.",
                "WORKOUT (08:30 AM): PULL splits - Barbell Deadlifts (3 Sets x 5 reps), Weighted Pullups (3x8), Seated Cable Rows (3x10), Hammer curls (3x12). Focus on back posture."
            ),
            Triple(
                "DAY 3: LEGS DAY (Endocrine & Dopamine Baseline Anchor)",
                "08:00 AM FLUSH: 500ml warm water + 5g raw Jeera/Ajwain + 1/2 Lemon.\n" +
                        "10:00 AM BREAKFAST: Oats porridge (40g raw oats) in water with 1 scoop Biozyme Protein + Black Coffee. (Supps: B-Complex).\n" +
                        "11:30 AM LUNCH: 120g raw Dal + 30g raw Rice + 150g raw Soya chunks + Green salad. (Supps: Fish Oil, Liv.52).\n" +
                        "03:00 PM PIVOT: 250ml Chaas + 5 mins Box Breathing.\n" +
                        "06:00 PM URGE DEFENSE: 30g roasted peanuts (strictly unsalted) + 1 glass green tea.\n" +
                        "08:00 PM DINNER: 120g raw Dal cooked thick + 2 boiled egg whites + Cucumber. (Strictly no chapati).\n" +
                        "11:30 PM SLEEP: 200ml Turmeric milk + 5 mins scalp massage. ZMA + Ashwagandha.",
                "WORKOUT (08:30 AM): LEGS splits - Barbell Back Squats (3 Sets x 8 reps), Romanian Deadlifts (3x10), Walking Lunges (3x12 steps/leg), Leg Raises (4xMax). Compound moves fire up growth hormone!"
            ),
            Triple(
                "DAY 4: ACTIVE RECOVERY & POSTURAL RESET",
                "08:00 AM FLUSH: 500ml warm water with Lemon. (Supps: NAC, Lion's Mane).\n" +
                        "10:00 AM BREAKFAST: 3 boiled Whole Eggs + salad + green tea. (Supps: B-Complex).\n" +
                        "11:30 AM LUNCH: 150g Paneer bhurji cooked with 5ml olive oil + 1 cup fresh low-fat Curd (150g). (Supps: Fish Oil).\n" +
                        "03:00 PM PIVOT: 250ml Chaas with raw mint leaves + 5 mins breathing.\n" +
                        "06:00 PM COGNITIVE SHIELD: 1 medium Banana (100g) + 20g raw almonds.\n" +
                        "08:00 PM DINNER: Mixed vegetable soup + 2 boiled egg whites + salad. (Strictly 0 carbs).\n" +
                        "11:30 PM SLEEP: 200ml Turmeric milk. Take ZMA + Ashwagandha.",
                "WORKOUT (08:30 AM): No weights today. Walk 45 mins briskly outdoors. Perform 20 Wall Angels, 30 Band Pull-aparts, and 3 sets of 1-min Planks. Opens up tight pathways!"
            )
        )

        days1to4.forEach { item ->
            paint.color = Color.parseColor("#FAFAFA")
            canvas.drawRoundRect(30f, y, 565f, y + 160f, 8f, 8f, paint)
            paint.color = primaryColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawRoundRect(30f, y, 565f, y + 160f, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            drawText(canvas, item.first, 40f, y + 6f, 510, 10f, true, primaryColor)
            
            // Draw Routine schedule
            drawText(canvas, item.second, 40f, y + 22f, 510, 7.5f, false, textColor)
            
            // Draw Workout schedule
            paint.color = Color.parseColor("#E8F5E9")
            canvas.drawRoundRect(40f, y + 115f, 555f, y + 152f, 4f, 4f, paint)
            drawText(canvas, item.third, 45f, y + 118f, 500, 7.5f, false, Color.parseColor("#2E7D32"))

            y += 175f
        }

        drawText(canvas, "CONFIDENTIAL PROTOCOL • GENERATED FOR SYSTEM REFORGE • PAGE 2 OF 3", 30f, 810f, 535, 8f, false, textMutedColor, Layout.Alignment.ALIGN_CENTER)
    }

    private fun drawPage3(canvas: Canvas, profile: UserProfile) {
        val paint = Paint()
        var y = 30f

        // Header Title
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 50f, paint)
        drawText(canvas, "35M RESET: 7-DAY TRANSFORMATION PROGRAM (DAYS 5 - 7)", 30f, 18f, 535, 14f, true, Color.WHITE, Layout.Alignment.ALIGN_CENTER)

        y = 70f
        drawText(canvas, "II. DAILY TIMELINES (CONTINUED - DAYS 5 TO 7)", 30f, y, 535, 12f, true, primaryColor)
        y += 18f

        val days5to7 = listOf(
            Triple(
                "DAY 5: PUSH DAY (Strength Compound Focus)",
                "08:00 AM FLUSH: 500ml warm water with Jeera/Ajwain.\n" +
                        "10:00 AM BREAKFAST: 3 Whole Eggs + 2 Whites scrambled + black coffee.\n" +
                        "11:30 AM LUNCH: 120g raw Dal + 30g raw Rice + 150g raw soya chunks + green salad.\n" +
                        "03:00 PM PIVOT: 250ml Chaas + 5 mins box breathing.\n" +
                        "06:00 PM URGE DEFENSE: 30g roasted peanuts (strictly unsalted) OR 1 medium Banana.\n" +
                        "08:00 PM DINNER: 120g raw Dal soup + 2 boiled egg whites + Cucumber.\n" +
                        "11:30 PM SLEEP: 200ml Turmeric milk. Take ZMA + Ashwagandha.",
                "WORKOUT (08:30 AM): Flat Bench Press (3x5 heavy), Barbell Military Press (3x6), Dumbbell Incline Press (3x10), Tricep skullcrushers (3x12). Power day."
            ),
            Triple(
                "DAY 6: PULL DAY (Hypertrophy / Nerve Repair Day)",
                "08:00 AM FLUSH: 500ml warm water with Jeera/Ajwain.\n" +
                        "10:00 AM BREAKFAST: 3 Whole Eggs + 2 Whites + Coffee.\n" +
                        "11:30 AM LUNCH: 120g raw Dal + 30g raw Basmati Rice + 150g grilled chicken breast + salad.\n" +
                        "03:00 PM PIVOT: 250ml Chaas + 5 mins box breathing.\n" +
                        "06:00 PM URGE DEFENSE: 30g roasted unsalted Chana + 1 glass green tea.\n" +
                        "08:00 PM DINNER: 120g raw Dal cooked thin + 150g grilled paneer chunks.\n" +
                        "11:30 PM SLEEP: 200ml Turmeric milk. Take ZMA + Ashwagandha.",
                "WORKOUT (08:30 AM): Pullups (3xMax), Barbell rows (3x10), Facepulls (3x15), Incline DB curls (3x12). High volume day."
            ),
            Triple(
                "DAY 7: LEGS & CARDIO CONDITIONING",
                "08:00 AM FLUSH: 500ml warm water with Lemon.\n" +
                        "10:00 AM BREAKFAST: oats porridge + 1 scoop Biozyme Protein + Coffee.\n" +
                        "11:30 AM LUNCH: 150g Paneer bhurji cooked with 5ml olive oil + 1 cup fresh low-fat Curd (150g).\n" +
                        "03:00 PM PIVOT: 250ml Chaas + 5 mins breathing.\n" +
                        "06:00 PM URGE DEFENSE: 1 medium Banana + 20g almonds.\n" +
                        "08:00 PM DINNER: 120g raw Dal soup + 2 boiled egg whites + salad.\n" +
                        "11:30 PM SLEEP: Turmeric milk. Take ZMA + Ashwagandha.",
                "WORKOUT (08:30 AM): Leg Press (3x12), Leg Extensions (3x15), Calf Raises (3x20), hanging leg raises (3xMax). Walk 15 mins briskly on treadmill post workout."
            )
        )

        days5to7.forEach { item ->
            paint.color = Color.parseColor("#FAFAFA")
            canvas.drawRoundRect(30f, y, 565f, y + 145f, 8f, 8f, paint)
            paint.color = primaryColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawRoundRect(30f, y, 565f, y + 145f, 8f, 8f, paint)
            paint.style = Paint.Style.FILL

            drawText(canvas, item.first, 40f, y + 5f, 510, 10f, true, primaryColor)
            drawText(canvas, item.second, 40f, y + 20f, 510, 7.5f, false, textColor)
            
            paint.color = Color.parseColor("#E8F5E9")
            canvas.drawRoundRect(40f, y + 105f, 555f, y + 138f, 4f, 4f, paint)
            drawText(canvas, item.third, 45f, y + 108f, 500, 7.5f, false, Color.parseColor("#2E7D32"))

            y += 157f
        }

        y += 5f

        // 3. Evening Urge Strategy & Mindset Blueprint Box
        paint.color = Color.parseColor("#FFF8E1") // Light amber container
        canvas.drawRoundRect(30f, y, 565f, y + 110f, 10f, 10f, paint)
        paint.color = Color.parseColor("#FFB300") // Amber border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(30f, y, 565f, y + 110f, 10f, 10f, paint)
        paint.style = Paint.Style.FILL

        var cy = y + 10f
        drawText(canvas, "🧠 BEHAVIORAL SCIENCE: HABIT MASTERY & EVENING URGE DEFENSE", 40f, cy, 505, 11f, true, Color.parseColor("#E65100"))
        cy += 16f
        val urgeAdvice = "1. THE EVENING CRITICAL POINT (06:00 PM - 07:30 PM): Chronic neuro-dopamine habits crave triggers (isolation, fatigue, stress). Pre-empt this by eating exactly 30g raw weight roasted peanuts or a medium banana. This triggers an insulin spike that drives calming tryptophan into the brain.\n" +
                "2. COMPASS OF THE MIND: Whenever an urge hits, execute the 5-Minute Box Breathing Protocol. Diaphragmatic breathing floods the vagus nerve with parasympathetic signals, applying a biological emergency brake to the fight-or-flight panic cycle.\n" +
                "3. HARD LOCAL ACCOUNTABILITY: Always log your daily cravings and sleep in your mobile database. Review the monthly metrics to identify cosmic and physical trends."
        drawText(canvas, urgeAdvice, 40f, cy, 505, 8f, false, textColor)

        drawText(canvas, "CONFIDENTIAL PROTOCOL • GENERATED FOR SYSTEM REFORGE • PAGE 3 OF 3", 30f, 810f, 535, 8f, false, textMutedColor, Layout.Alignment.ALIGN_CENTER)
    }

    private fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        textSize: Float = 10f,
        isBold: Boolean = false,
        color: Int = Color.BLACK,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
    ): Float {
        val textPaint = TextPaint().apply {
            this.color = color
            this.textSize = textSize
            this.isFakeBoldText = isBold
            this.isAntiAlias = true
        }
        canvas.save()
        canvas.translate(x, y)
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.15f)
            .build()
        staticLayout.draw(canvas)
        canvas.restore()
        return staticLayout.height.toFloat()
    }
}

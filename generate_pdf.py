#!/usr/bin/env python3
import sys

try:
    from reportlab.lib.pagesizes import letter
    from reportlab.lib import colors
    from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether
    from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
    from reportlab.lib.units import inch
except ImportError:
    print("=" * 60)
    print("Error: ReportLab library is required to generate the PDF.")
    print("Please install it on your system by running:")
    print("    pip install reportlab")
    print("=" * 60)
    sys.exit(1)

def build_pdf(filename="Transformation_Protocol.pdf"):
    # Create the document template with 0.5-inch margins for dense professional layout
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        rightMargin=36,
        leftMargin=36,
        topMargin=36,
        bottomMargin=36
    )
    
    styles = getSampleStyleSheet()
    
    # Custom high-fidelity color scheme (Deep Charcoal, Slate, Clean Lime/Green, Intense Coral)
    primary_color = colors.HexColor("#121212")    # Charcoal
    surface_color = colors.HexColor("#1E1E1E")    # Slate
    accent_color = colors.HexColor("#00E676")     # Growth indicators
    warning_color = colors.HexColor("#FF3D00")    # Intense Coral
    text_muted = colors.HexColor("#9E9E9E")       # Muted gray
    
    # Custom Paragraph Styles
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=24,
        leading=28,
        textColor=accent_color,
        spaceAfter=4
    )
    
    subtitle_style = ParagraphStyle(
        'DocSub',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=16,
        textColor=colors.white,
        spaceAfter=15,
        textTransform='uppercase'
    )
    
    section_heading = ParagraphStyle(
        'SecHead',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=14,
        leading=18,
        textColor=accent_color,
        spaceBefore=12,
        spaceAfter=8,
        keepWithNext=True
    )
    
    body_style = ParagraphStyle(
        'DocBody',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10,
        leading=14,
        textColor=colors.HexColor("#E0E0E0")
    )
    
    warning_body_style = ParagraphStyle(
        'WarnBody',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=14,
        textColor=colors.white
    )
    
    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        leading=12,
        textColor=colors.white
    )
    
    table_cell_bold_style = ParagraphStyle(
        'TableCellBold',
        parent=table_cell_style,
        fontName='Helvetica-Bold',
        textColor=accent_color
    )

    story = []

    # --- HEADER BLOCK (CHARCOAL CANVAS BANNER) ---
    banner_data = [
        [Paragraph("REFORGE SYSTEM OPERATING SHEET", title_style)],
        [Paragraph("TOTAL TRANSFORMATION PROTOCOL (35M RESET) • VIKAS ACTIVE BLUEPRINT", subtitle_style)]
    ]
    banner_table = Table(banner_data, colWidths=[540])
    banner_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), primary_color),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 10),
        ('TOPPADDING', (0, 0), (-1, -1), 15),
        ('LEFTPADDING', (0, 0), (-1, -1), 15),
        ('RIGHTPADDING', (0, 0), (-1, -1), 15),
    ]))
    story.append(banner_table)
    story.append(Spacer(1, 12))

    # --- MEDICAL WARNING SECTION (RED ACCENT CARD) ---
    warning_content = (
        "<b>🚨 MEDICAL WARNING (READ FIRST):</b> You are quitting both alcohol and smoking cold turkey after 10 years of heavy use. "
        "This is an extreme neurological and cardiovascular shift. Monitor your physical state continuously.<br/><br/>"
        "<b>RED FLAG EMERGENCY SYMPTOMS:</b> If you experience severe uncontrollable hand tremors, visual or auditory hallucinations, "
        "extreme mental confusion, high fever, or seizures in the first 72 hours, <b>go to the Emergency Room (ER) immediately</b>. "
        "These are indicators of <i>Delirium Tremens</i>, which is a life-threatening emergency.<br/><br/>"
        "<b>Expected Baseline:</b> For the first 7-14 days, you will feel extreme insomnia, high irritability, and sudden cravings. "
        "This is normal as your brain's dopamine receptors normalize. Keep your focus. Do not negotiate."
    )
    
    warning_table = Table([[Paragraph(warning_content, warning_body_style)]], colWidths=[540])
    warning_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), colors.HexColor("#2C1A1A")),
        ('BOX', (0, 0), (-1, -1), 1.5, warning_color),
        ('TOPPADDING', (0, 0), (-1, -1), 12),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
        ('LEFTPADDING', (0, 0), (-1, -1), 12),
        ('RIGHTPADDING', (0, 0), (-1, -1), 12),
    ]))
    story.append(warning_table)
    story.append(Spacer(1, 15))

    # --- SECTION 1: THE DAILY MASTER SCHEDULE ---
    story.append(Paragraph("I. THE DAILY MASTER SCHEDULE (MON-FRI)", section_heading))
    
    schedule_headers = [
        Paragraph("<b>Time</b>", table_cell_bold_style),
        Paragraph("<b>Activity</b>", table_cell_bold_style),
        Paragraph("<b>Protocol & Execution Details</b>", table_cell_bold_style)
    ]
    
    schedule_data = [
        schedule_headers,
        [
            Paragraph("08:00 AM", table_cell_bold_style),
            Paragraph("Wake Up & Flush", table_cell_style),
            Paragraph("Drink 500ml warm water with Jeera/Ajwain + 1/2 Lemon. Take NAC (600mg) + Lion's Mane. Splash ice cold face wash to activate mammalian dive reflex.", table_cell_style)
        ],
        [
            Paragraph("08:20 AM", table_cell_bold_style),
            Paragraph("Physical Prep", table_cell_style),
            Paragraph("Perform 10 Wall Angels & 20 Band Pull-Aparts to correct posture and expand tight airways.", table_cell_style)
        ],
        [
            Paragraph("08:30 AM", table_cell_bold_style),
            Paragraph("Gym Session", table_cell_style),
            Paragraph("45 Mins Weight Training. Push / Pull / Legs rotation. Heavy compound loading anchors baseline dopamine.", table_cell_style)
        ],
        [
            Paragraph("09:15 AM", table_cell_bold_style),
            Paragraph("Anabolic Fuel", table_cell_style),
            Paragraph("1 Scoop MuscleBlaze Biozyme Whey Protein in water.", table_cell_style)
        ],
        [
            Paragraph("10:00 AM", table_cell_bold_style),
            Paragraph("Breakfast", table_cell_style),
            Paragraph("3 Whole Eggs + 2 Whites (scrambled/boiled) + Black Coffee. Take Neurobion Forte + 1 Multivitamin.", table_cell_style)
        ],
        [
            Paragraph("11:30 AM", table_cell_bold_style),
            Paragraph("Lunch", table_cell_style),
            Paragraph("Dal + 150g Chicken/Soya + 1 Chapati + Salad. Take Fish Oil (1000mg) + Liv.52 tablet.", table_cell_style)
        ],
        [
            Paragraph("12:00 PM", table_cell_bold_style),
            Paragraph("Posture Check", table_cell_style),
            Paragraph("Calibrate shoulder blades back, pull head back to stack ears directly over shoulders.", table_cell_style)
        ],
        [
            Paragraph("03:00 PM", table_cell_bold_style),
            Paragraph("Mid-Day Pivot", table_cell_style),
            Paragraph("Drink fresh Buttermilk (Chaas) or Green Tea. Perform 5 minutes of Box Breathing (Inhale 4s, Hold 4s, Exhale 4s, Hold 4s) to apply parasympathetic vagal brake.", table_cell_style)
        ],
        [
            Paragraph("06:00 PM", table_cell_bold_style),
            Paragraph("Transition", table_cell_style),
            Paragraph("Eat 20g Roasted Chana or Peanuts. Do 5 mins Walk or climb stairs to release work stress.", table_cell_style)
        ],
        [
            Paragraph("08:00 PM", table_cell_bold_style),
            Paragraph("Dinner", table_cell_style),
            Paragraph("1 Bowl Dal + 2 Boiled Egg Whites + Green Salad. Strictly zero chapatis or heavy carbs at night.", table_cell_style)
        ],
        [
            Paragraph("11:30 PM", table_cell_bold_style),
            Paragraph("Sleep Prep & Stack", table_cell_style),
            Paragraph("Drink warm milk with turmeric, black pepper, and stevia. Spend 5 mins on scalp massage. Take ZMA + Ashwagandha capsule for cortisol reduction.", table_cell_style)
        ]
    ]
    
    schedule_table = Table(schedule_data, colWidths=[70, 100, 370])
    schedule_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), surface_color),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#424242")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor("#1E1E1E"), colors.HexColor("#252525")]),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(schedule_table)
    story.append(Spacer(1, 15))

    # --- SECTION 2: THE SUPPLEMENT BLUEPRINT ---
    story.append(KeepTogether([
        Paragraph("II. TARGETED TRANSFORMATION SUPPLEMENTS", section_heading),
        Paragraph("Use consistently to protect neuro-receptors and ease the double withdrawal transition.", body_style),
        Spacer(1, 8)
    ]))
    
    supp_headers = [
        Paragraph("<b>Supplement</b>", table_cell_bold_style),
        Paragraph("<b>Dosage & Timing</b>", table_cell_bold_style),
        Paragraph("<b>Biological / Neurochemical Purpose</b>", table_cell_bold_style)
    ]
    
    supp_data = [
        supp_headers,
        [
            Paragraph("NAC (N-Acetyl Cysteine)", table_cell_bold_style),
            Paragraph("600mg @ 08:00 AM (Empty Stomach)", table_cell_style),
            Paragraph("Boosts glutathione, protects liver cells, blocks severe smoking/nicotine lung cravings.", table_cell_style)
        ],
        [
            Paragraph("Lion's Mane", table_cell_bold_style),
            Paragraph("1 Capsule @ 08:00 AM", table_cell_style),
            Paragraph("Stimulates Nerve Growth Factor (NGF) for cognitive repair and brain remodeling.", table_cell_style)
        ],
        [
            Paragraph("Neurobion Forte", table_cell_bold_style),
            Paragraph("1 Tablet @ 10:00 AM (With Breakfast)", table_cell_style),
            Paragraph("High-dose therapeutic B-Complex (B1, B6, B12) for nerve cell sheath repair and baseline mood.", table_cell_style)
        ],
        [
            Paragraph("Multivitamin", table_cell_bold_style),
            Paragraph("1 Tablet @ 10:00 AM (With Breakfast)", table_cell_style),
            Paragraph("Provides micronutrient buffers to support heavy gym training, immunity, and endocrine system.", table_cell_style)
        ],
        [
            Paragraph("Fish Oil (1000mg)", table_cell_bold_style),
            Paragraph("1 Capsule @ 11:30 AM (With Lunch)", table_cell_style),
            Paragraph("Concentrated Omega-3 fatty acids for anti-inflammatory support and brain cellular structure.", table_cell_style)
        ],
        [
            Paragraph("Liv.52", table_cell_bold_style),
            Paragraph("1 Tablet @ 11:30 AM (With Lunch)", table_cell_style),
            Paragraph("Herbal liver protector to accelerate recovery from chronic alcohol processing damage.", table_cell_style)
        ],
        [
            Paragraph("ZMA (Magnesium/Zinc/B6)", table_cell_bold_style),
            Paragraph("1 Capsule @ 11:30 PM (Bedtime)", table_cell_style),
            Paragraph("Induces deep, restful REM sleep cycles. Essential for muscle repair and calming high brain excitation.", table_cell_style)
        ],
        [
            Paragraph("Ashwagandha", table_cell_bold_style),
            Paragraph("1 Capsule @ 11:30 PM (Bedtime)", table_cell_style),
            Paragraph("Blocks excessive evening cortisol production, lowering bedtime anxiety and stabilizing deep sleep.", table_cell_style)
        ]
    ]
    
    supp_table = Table(supp_data, colWidths=[130, 140, 270])
    supp_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), surface_color),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#424242")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor("#1E1E1E"), colors.HexColor("#252525")]),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
        ('TOPPADDING', (0, 0), (-1, -1), 5),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 5),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(supp_table)
    story.append(Spacer(1, 15))

    # --- SECTION 3: THE ANABOLIC PPL WORKOUT split ---
    story.append(KeepTogether([
        Paragraph("III. ANABOLIC RESISTANCE PROGRAM (PUSH/PULL/LEGS)", section_heading),
        Paragraph("Keep intense training under 45 minutes to prevent heavy catabolic cortisol surges. Progressively loadcompound exercises.", body_style),
        Spacer(1, 8)
    ]))
    
    workout_headers = [
        Paragraph("<b>Training Day</b>", table_cell_bold_style),
        Paragraph("<b>Exercises & Target Splits</b>", table_cell_bold_style),
        Paragraph("<b>Postural & Dietary Anchors</b>", table_cell_bold_style)
    ]
    
    workout_data = [
        workout_headers,
        [
            Paragraph("<b>DAY 1: PUSH</b><br/>Chest, Shoulders, Triceps", table_cell_style),
            Paragraph("• Flat Dumbbell Press (3x8-10)<br/>• Overhead Barbell Press (3x8)<br/>• Incline Flyes (3x12)<br/>• Overhead Tricep Rope extensions (3x12-15)", table_cell_style),
            Paragraph("<b>Postural Calibration (08:20 AM):</b><br/>Perform 10 Wall Angels and 20 Band Pull-Aparts before leaving the house.<br/><br/><b>Protein Focus:</b><br/>Target 120g - 150g protein daily.<br/>MuscleBlaze Biozyme Whey at 09:15 AM (post-workout). Breakfast eggs at 10:00 AM.", table_cell_style)
        ],
        [
            Paragraph("<b>DAY 2: PULL</b><br/>Back, Biceps, Rear Delts", table_cell_style),
            Paragraph("• Barbell Deadlifts (3x5)<br/>• Weighted Pullups or Lat Pulls (3x8)<br/>• Seated Cable Rows (3x10)<br/>• Dumbbell Hammer Curls (3x12)", table_cell_style),
            Paragraph("<b>Sub-Dinner Rule:</b><br/>Strictly no carbs (chapatis, rice, sugar) at dinner (08:00 PM). Eat 1 bowl dal, salad, and 2 boiled egg whites to encourage fat burn and metabolic flexibility.", table_cell_style)
        ],
        [
            Paragraph("<b>DAY 3: LEGS</b><br/>Quads, Hamstrings, Core", table_cell_style),
            Paragraph("• Barbell Back Squats (3x6-8)<br/>• Romanian Deadlifts (3x10)<br/>• Walking Weighted Lunges (3x12 steps/leg)<br/>• Hanging Leg Raises (4xMax)", table_cell_style),
            Paragraph("<b>Vagal Activation (03:00 PM):</b><br/>Box breathing on leg day reverses nervous stress instantly. Lowers baseline flight-or-fight response.", table_cell_style)
        ]
    ]
    
    workout_table = Table(workout_data, colWidths=[120, 210, 210])
    workout_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), surface_color),
        ('GRID', (0, 0), (-1, -1), 0.5, colors.HexColor("#424242")),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.HexColor("#1E1E1E"), colors.HexColor("#252525")]),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
        ('TOPPADDING', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
        ('LEFTPADDING', (0, 0), (-1, -1), 8),
        ('RIGHTPADDING', (0, 0), (-1, -1), 8),
    ]))
    story.append(workout_table)
    
    # Build document
    doc.build(story)
    print(f"Success! Generated '{filename}' successfully.")

if __name__ == "__main__":
    build_pdf()

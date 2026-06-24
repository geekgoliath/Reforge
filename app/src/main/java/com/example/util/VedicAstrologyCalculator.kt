package com.example.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

object VedicAstrologyCalculator {

    // Nakshatras and their ruling planets (Vimshottari Dasha order)
    private val NAKSHATRAS = listOf(
        "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", 
        "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", 
        "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", 
        "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", 
        "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    )

    private val DASHA_PLANETS = listOf(
        "Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"
    )

    private val DASHA_YEARS = mapOf(
        "Ketu" to 7, "Venus" to 20, "Sun" to 6, "Moon" to 10, "Mars" to 7, 
        "Rahu" to 18, "Jupiter" to 16, "Saturn" to 19, "Mercury" to 17
    )

    private val ZODIAC_SIGNS = listOf(
        "Aries (Mesha)", "Taurus (Vrishabha)", "Gemini (Mithuna)", "Cancer (Karka)", 
        "Leo (Simha)", "Virgo (Kanya)", "Libra (Tula)", "Scorpio (Vrischika)", 
        "Sagittarius (Dhanu)", "Capricorn (Makara)", "Aquarius (Kumbha)", "Pisces (Meena)"
    )

    data class VedicHoroscope(
        val dob: String,
        val birthTime: String,
        val birthPlace: String,
        val lagna: String,
        val lagnaDegree: Float,
        val moonSign: String,
        val moonDegree: Float,
        val nakshatra: String,
        val nakshatraLord: String,
        val currentMahadasha: String,
        val currentAntardasha: String,
        val dashaTimeRemaining: String,
        val planetaryPositions: Map<String, String>
    )

    fun calculate(dob: String, birthTime: String, birthPlace: String): VedicHoroscope {
        // Fallback or default values
        val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        try {
            calendar.time = dateParser.parse(dob) ?: Date()
        } catch (e: Exception) {
            calendar.set(1991, Calendar.MARCH, 15)
        }

        val timeParts = birthTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 12
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val decimalHour = hour + (minute / 60.0)

        // 1. Julian Date Calculation
        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 12 else month
        val a = y / 100
        val b = a / 4
        val c = 2 - a + b
        val e = (365.25 * (y + 4716)).toInt()
        val f = (30.6001 * (m + 1)).toInt()
        val jd = c + day + e + f - 1524.5 + (decimalHour / 24.0)

        val t = (jd - 2451545.0) / 36525.0 // Julian centuries since J2000.0

        // 2. Lahiri Ayanamsa (sidereal correction)
        val ayanamsa = 22.44 + (1.396 * (year - 1900) / 100.0)

        // 3. Sun sidereal position (Jean Meeus simplified Keplerian)
        val sunMeanLong = (280.466 + 36000.77 * t) % 360.0
        val sunMeanAnomaly = (357.529 + 35999.05 * t) % 360.0
        val sunTrueLong = (sunMeanLong + 1.915 * sin(Math.toRadians(sunMeanAnomaly)) + 0.02 * sin(Math.toRadians(2 * sunMeanAnomaly))) % 360.0
        val sunSiderealLong = (sunTrueLong - ayanamsa + 360.0) % 360.0

        // 4. Moon sidereal position (with main perturbations)
        val moonMeanLong = (218.316 + 481267.881 * t) % 360.0
        val moonMeanAnomaly = (134.963 + 477198.867 * t) % 360.0
        val moonElongation = (297.85 + 445267.11 * t) % 360.0
        val moonTrueLong = (moonMeanLong + 6.289 * sin(Math.toRadians(moonMeanAnomaly)) + 1.274 * sin(Math.toRadians(2 * moonElongation - moonMeanAnomaly)) + 0.658 * sin(Math.toRadians(2 * moonElongation)) + 0.214 * sin(Math.toRadians(2 * moonMeanAnomaly))) % 360.0
        val moonSiderealLong = (moonTrueLong - ayanamsa + 360.0) % 360.0

        // 5. Lagna (Ascendant) based on Sunrise Alignment
        // In Vedic, Lagna is the sign rising in the east. It starts at Sun's sidereal position at sunrise (approx 6 AM) and rotates 360 deg in 24 hrs.
        val hoursSinceSunrise = (decimalHour - 6.0 + 24.0) % 24.0
        val lagnaLong = (sunSiderealLong + (hoursSinceSunrise * 15.0)) % 360.0

        // 6. Moon Sign and Nakshatra
        val moonSignIndex = (moonSiderealLong / 30.0).toInt() % 12
        val moonSign = ZODIAC_SIGNS[moonSignIndex]
        val moonDegree = (moonSiderealLong % 30.0).toFloat()

        val lagnaSignIndex = (lagnaLong / 30.0).toInt() % 12
        val lagna = ZODIAC_SIGNS[lagnaSignIndex]
        val lagnaDegree = (lagnaLong % 30.0).toFloat()

        val nakshatraIndex = (moonSiderealLong / (360.0 / 27.0)).toInt() % 27
        val nakshatra = NAKSHATRAS[nakshatraIndex]
        
        // Vimshottari lord of the nakshatra
        val nakshatraLord = DASHA_PLANETS[nakshatraIndex % 9]

        // 7. Vimshottari Dasha Calculation
        // Calculate remaining years of birth dasha
        val nakshatraSpan = 360.0 / 27.0 // 13.333 degrees
        val traversedInNakshatra = moonSiderealLong % nakshatraSpan
        val fractionRemaining = 1.0 - (traversedInNakshatra / nakshatraSpan)
        val dashaTotalYears = DASHA_YEARS[nakshatraLord] ?: 10
        val initialDashaRemaining = fractionRemaining * dashaTotalYears

        // Project Dasha to the present (June 2026)
        val currentYear = 2026
        val currentMonth = 6
        val currentDay = 23
        
        val ageInYears = (currentYear - year) + (currentMonth - month) / 12.0 + (currentDay - day) / 365.25
        
        var cumulativeYears = initialDashaRemaining
        var currentDashaIndex = DASHA_PLANETS.indexOf(nakshatraLord)
        var mahadashaLord = nakshatraLord

        // If age exceeds the first dasha remaining years, cycle through next dashas
        if (ageInYears > initialDashaRemaining) {
            var tempAge = ageInYears - initialDashaRemaining
            while (true) {
                currentDashaIndex = (currentDashaIndex + 1) % 9
                val nextLord = DASHA_PLANETS[currentDashaIndex]
                val nextLordYears = DASHA_YEARS[nextLord] ?: 10
                if (tempAge < nextLordYears) {
                    mahadashaLord = nextLord
                    cumulativeYears = nextLordYears - tempAge
                    break
                }
                tempAge -= nextLordYears
            }
        } else {
            cumulativeYears = initialDashaRemaining - ageInYears
        }

        // Calculate Antardasha (Bhukti)
        // Sub-periods are proportionally divided based on Vimshottari ratios
        val currentMahadashaTotal = DASHA_YEARS[mahadashaLord] ?: 10
        val startingSubIndex = DASHA_PLANETS.indexOf(mahadashaLord)
        var elapsedInDasha = currentMahadashaTotal - cumulativeYears
        var antardashaLord = mahadashaLord
        var subElapsed = 0.0

        for (i in 0 until 9) {
            val subLord = DASHA_PLANETS[(startingSubIndex + i) % 9]
            val subLordYears = DASHA_YEARS[subLord] ?: 10
            val subPeriodDuration = (currentMahadashaTotal * subLordYears) / 120.0
            if (elapsedInDasha < subPeriodDuration) {
                antardashaLord = subLord
                subElapsed = subPeriodDuration - elapsedInDasha
                break
            }
            elapsedInDasha -= subPeriodDuration
        }

        val remainingDashaMonths = (cumulativeYears * 12.0).toInt()
        val dashaRemainingStr = "${remainingDashaMonths / 12} Years, ${remainingDashaMonths % 12} Months"

        // 8. Other Planetary Longitudes (Approximate deterministic sidereal placements for Vedic accuracy)
        val planetMeanLongs = mapOf(
            "Sun" to sunSiderealLong,
            "Moon" to moonSiderealLong,
            "Mars" to ((355.43 + 191.4 * t - ayanamsa + 360.0) % 360.0),
            "Mercury" to ((102.27 + 1494.72 * t - ayanamsa + 360.0) % 360.0),
            "Jupiter" to ((34.35 + 30.34 * t - ayanamsa + 360.0) % 360.0),
            "Venus" to ((272.3 + 585.17 * t - ayanamsa + 360.0) % 360.0),
            "Saturn" to ((50.07 + 12.22 * t - ayanamsa + 360.0) % 360.0),
            "Rahu" to ((125.04 - 1.934 * t - ayanamsa + 360.0) % 360.0),
            "Ketu" to ((125.04 - 1.934 * t - ayanamsa + 180.0 - ayanamsa + 360.0) % 360.0)
        )

        val planetaryPositions = planetMeanLongs.mapValues { (_, longitude) ->
            val signIdx = (longitude / 30.0).toInt() % 12
            val deg = (longitude % 30.0).toInt()
            "${ZODIAC_SIGNS[signIdx]} at ${deg}°"
        }

        return VedicHoroscope(
            dob = dob,
            birthTime = birthTime,
            birthPlace = birthPlace,
            lagna = lagna,
            lagnaDegree = lagnaDegree,
            moonSign = moonSign,
            moonDegree = moonDegree,
            nakshatra = nakshatra,
            nakshatraLord = nakshatraLord,
            currentMahadasha = mahadashaLord,
            currentAntardasha = antardashaLord,
            dashaTimeRemaining = dashaRemainingStr,
            planetaryPositions = planetaryPositions
        )
    }

    data class EnergyForecast(
        val morning: String, // "High", "Medium", "Low"
        val afternoon: String,
        val night: String
    )

    data class TransitAnalysis(
        val date: String,
        val transitMoonSign: String,
        val transitMoonSignIndex: Int,
        val houseFromNatalMoon: Int,
        val isAshtamaChandra: Boolean,
        val riskWindow: String,
        val reason: String,
        val suggestedFocus: String,
        val avoidList: List<String>,
        val recommendedList: List<String>,
        val energyForecast: EnergyForecast,
        val relapseMultiplier: Float
    )

    fun calculateTransitMoonSignIndex(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val decimalHour = hour + (minute / 60.0)

        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 12 else month
        val a = y / 100
        val b = a / 4
        val c = 2 - a + b
        val e = (365.25 * (y + 4716)).toInt()
        val f = (30.6001 * (m + 1)).toInt()
        val jd = c + day + e + f - 1524.5 + (decimalHour / 24.0)

        val t = (jd - 2451545.0) / 36525.0
        val ayanamsa = 22.44 + (1.396 * (year - 1900) / 100.0)

        val moonMeanLong = (218.316 + 481267.881 * t) % 360.0
        val moonMeanAnomaly = (134.963 + 477198.867 * t) % 360.0
        val moonElongation = (297.85 + 445267.11 * t) % 360.0
        val moonTrueLong = (moonMeanLong + 6.289 * sin(Math.toRadians(moonMeanAnomaly)) + 1.274 * sin(Math.toRadians(2 * moonElongation - moonMeanAnomaly)) + 0.658 * sin(Math.toRadians(2 * moonElongation)) + 0.214 * sin(Math.toRadians(2 * moonMeanAnomaly))) % 360.0
        val moonSiderealLong = (moonTrueLong - ayanamsa + 360.0) % 360.0
        return ((moonSiderealLong / 30.0).toInt() % 12 + 12) % 12
    }

    fun calculateTransit(dob: String, birthTime: String, birthPlace: String, transitDate: Date): TransitAnalysis {
        val natalHoroscope = calculate(dob, birthTime, birthPlace)
        val natalMoonSignIndex = ZODIAC_SIGNS.indexOf(natalHoroscope.moonSign)
        val transitMoonSignIndex = calculateTransitMoonSignIndex(transitDate)
        
        val houseFromNatalMoon = (transitMoonSignIndex - natalMoonSignIndex + 12) % 12 + 1

        val isAshtamaChandra = (houseFromNatalMoon == 8)
        val isArdhaAshtama = (houseFromNatalMoon == 4)
        val isDwadasa = (houseFromNatalMoon == 12)

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(transitDate)

        // Setup values for different houses
        val riskWindow: String
        val reason: String
        val suggestedFocus: String
        val avoids: List<String>
        val recommended: List<String>
        val energy: EnergyForecast
        val multiplier: Float

        when (houseFromNatalMoon) {
            8 -> {
                riskWindow = "19:00 - 21:00"
                reason = "Moon transiting 8th house from natal Moon (Ashtama Chandra). High emotional volatility and relapse vulnerability."
                suggestedFocus = "Avoid isolation. Schedule workout before 6 PM."
                avoids = listOf("Alcohol", "Impulsive spending", "Arguments")
                recommended = listOf("Gym", "Walking", "Journaling")
                energy = EnergyForecast("High", "Medium", "Low")
                multiplier = 1.5f
            }
            12 -> {
                riskWindow = "21:00 - 23:00"
                reason = "Moon transiting 12th house from natal Moon (Dwadasa Chandra). Mind seeking escape, distraction risk."
                suggestedFocus = "Leave screens away. Focus on relaxation, read a physical book."
                avoids = listOf("Late night screens", "Impulsive decisions", "Sugary snacks")
                recommended = listOf("Meditation", "Reading", "Warm bath")
                energy = EnergyForecast("Medium", "Medium", "Low")
                multiplier = 1.3f
            }
            4 -> {
                riskWindow = "14:00 - 16:00"
                reason = "Moon transiting 4th house from natal Moon (Ardha Ashtama Chandra). Mid-day emotional unrest."
                suggestedFocus = "Take a 15-minute nature walk. Stay hydrated."
                avoids = listOf("Caffeine abuse", "Isolation", "Arguments")
                recommended = listOf("Short walks", "Deep breathing", "Green tea")
                energy = EnergyForecast("Medium", "Low", "Medium")
                multiplier = 1.2f
            }
            else -> {
                riskWindow = "18:00 - 20:00"
                reason = "Moon transiting ${houseFromNatalMoon}th house from natal Moon. Energetic support is stable."
                suggestedFocus = "Channel focus into physical alignment and core goals."
                avoids = listOf("Complacency", "Procrastination", "Junk food")
                recommended = listOf("High protein meals", "Strength workout", "Deep sleep focus")
                energy = EnergyForecast("High", "High", "Medium")
                multiplier = 1.0f
            }
        }

        return TransitAnalysis(
            date = dateStr,
            transitMoonSign = ZODIAC_SIGNS[transitMoonSignIndex],
            transitMoonSignIndex = transitMoonSignIndex,
            houseFromNatalMoon = houseFromNatalMoon,
            isAshtamaChandra = isAshtamaChandra,
            riskWindow = riskWindow,
            reason = reason,
            suggestedFocus = suggestedFocus,
            avoidList = avoids,
            recommendedList = recommended,
            energyForecast = energy,
            relapseMultiplier = multiplier
        )
    }
}

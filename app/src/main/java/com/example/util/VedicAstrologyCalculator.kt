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
}

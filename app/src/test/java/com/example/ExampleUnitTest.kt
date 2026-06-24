package com.example

import org.junit.Assert.*
import org.junit.Test
import okhttp3.OkHttpClient
import okhttp3.Request

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun list_gemini_models() {
    val apiKey = com.example.BuildConfig.GEMINI_API_KEY
    println("DEBUG: GEMINI_API_KEY length is ${apiKey.length}")
    if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
      println("DEBUG: Using mock/placeholder API key")
      return
    }
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
        .build()
    
    try {
        client.newCall(request).execute().use { response ->
            println("DEBUG: Response Code = ${response.code}")
            println("DEBUG: Response Body = ${response.body?.string()}")
        }
    } catch (e: Exception) {
        println("DEBUG: Exception during call: ${e.message}")
        e.printStackTrace()
    }
  }
}


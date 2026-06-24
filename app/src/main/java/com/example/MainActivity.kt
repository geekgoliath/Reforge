package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.ReforgeApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Request notification runtime permission on Android 13+
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
    }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        ReforgeApp()
      }
    }
  }
}

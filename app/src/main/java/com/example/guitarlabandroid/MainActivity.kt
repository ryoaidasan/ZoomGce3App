package com.example.guitarlabandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    private lateinit var zoomManager: ZoomMidiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zoomManager = ZoomMidiManager(this)

        setContent {
            MaterialTheme {
                GuitarLabScreen(zoomManager = zoomManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        zoomManager.close()
    }
}

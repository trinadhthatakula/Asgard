package com.valhalla.asgard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.valhalla.asgard.demo.GalleryApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryApp(initialEntry = if (savedInstanceState == null) intent.getStringExtra("entry") else null)
        }
    }
}

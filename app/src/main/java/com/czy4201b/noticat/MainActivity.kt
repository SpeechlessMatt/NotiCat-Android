package com.czy4201b.noticat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.czy4201b.noticat.core.theme.NotiCatTheme
import com.czy4201b.noticat.navigation.NotiCatApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiCatTheme {
                NotiCatApp()
            }
        }
    }
}
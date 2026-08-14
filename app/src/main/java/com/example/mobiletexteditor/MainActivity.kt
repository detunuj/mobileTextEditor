package com.example.mobiletexteditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mobiletexteditor.editor.ui.MainEditorScreen
import com.example.mobiletexteditor.ui.theme.MobileTextEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileTextEditorTheme {
                MainEditorScreen()
            }
        }
    }
}
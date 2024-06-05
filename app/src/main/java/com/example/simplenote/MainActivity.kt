package com.example.simplenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.simplenote.ui.LoginScreen
import com.example.simplenote.ui.theme.SimpleNoteTheme
import com.example.simplenote.ui.PreviewEditorScreen
import com.example.simplenote.ui.MainScreen
import com.example.simplenote.ui.navigation.NoteNavHost
import com.example.simplenote.ui.PreviewEditorScreen
import com.example.simplenote.ui.theme.SimpleNoteTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SimpleNoteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    LoginScreen()
                    NoteNavHost()

//                    PreviewEditorScreen()
                }
            }
        }
    }

}



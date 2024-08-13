package com.example.yestion_ms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.yestion_ms.ui.theme.Yestion_msTheme
import com.google.firebase.Firebase
import com.google.firebase.database.database

class MainActivity : ComponentActivity() {
    private val viewModel: ContentViewModel by viewModels()
    val fireRealTimeDatabase =
        Firebase.database("https://orcaprj-3518d-default-rtdb.asia-southeast1.firebasedatabase.app/")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Yestion_msTheme {
                var refreshTrigger by remember{ mutableStateOf(false) }
                WorkSpaceScreen(viewModel)
            }
        }
    }
}
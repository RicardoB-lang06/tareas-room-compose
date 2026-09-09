package com.example.tareasroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tareasroom.ui.TaskListScreen
import com.example.tareasroom.ui.TaskViewModel
import com.example.tareasroom.ui.theme.TaskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskTheme {
                val model: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
                TaskListScreen(model)
            }
        }
    }
}

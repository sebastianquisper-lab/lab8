package com.example.lab8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import kotlinx.coroutines.launch
import com.example.lab8.ui.theme.Lab8Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crear la base de datos Room
        val db = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "task_db"
        ).build()

        val dao = db.taskDao()

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return TaskViewModel(dao) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        // ✅ Crear el ViewModel usando la factory
        val viewModel: TaskViewModel by viewModels { factory }

        // ✅ Pasar el ViewModel al contenido Compose
        setContent {
            Lab8Theme {
                TaskScreen(viewModel)
            }
        }
    }
}
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var newTaskDescription by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Tareas 📝",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Campo de texto para nueva tarea o edición
        OutlinedTextField(
            value = newTaskDescription,
            onValueChange = { newTaskDescription = it },
            label = { Text(if (isEditing) "Editar tarea" else "Nueva tarea") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (newTaskDescription.isNotEmpty()) {
                    if (isEditing && taskBeingEdited != null) {
                        viewModel.updateTask(taskBeingEdited!!, newTaskDescription)
                        isEditing = false
                        taskBeingEdited = null
                    } else {
                        viewModel.addTask(newTaskDescription)
                    }
                    newTaskDescription = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(if (isEditing) "Guardar cambios" else "Agregar tarea")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de tareas
        tasks.forEach { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = if (task.isCompleted) "Completada ✅" else "Pendiente ⏳",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row {
                        // Botón para editar
                        IconButton(onClick = {
                            newTaskDescription = task.description
                            isEditing = true
                            taskBeingEdited = task
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }

                        // Botón para eliminar
                        IconButton(onClick = {
                            viewModel.deleteTask(task)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar"
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para eliminar todas las tareas
        Button(
            onClick = { viewModel.deleteAllTasks() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Eliminar todas las tareas 🗑️")
        }
    }
}
package com.anana.flashcardapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.anana.flashcardapp.ui.theme.FlashcardAppTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember

// Main activity for the Flashcard App using Jetpack Compose
class MainActivity : ComponentActivity() {
    // Initializes the activity and sets up the Compose UI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the custom theme and set up the main surface
            FlashcardAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FlashcardScreen(this)
                }
            }
        }
    }

    // Composable function for the main flashcard screen UI
    @Composable
    fun FlashcardScreen(context: Context) {
        // Initialize state for flashcards, current index, question/answer display, and dialog visibility
        var flashcards by remember { mutableStateOf(loadFlashcards(context)) }
        var currentIndex by remember { mutableStateOf(0) }
        var showingQuestion by remember { mutableStateOf(true) }
        var showAddDialog by remember { mutableStateOf(false) }

        // Ensure at least one default flashcard exists
        if (flashcards.isEmpty()) {
            flashcards = mutableListOf(Flashcard("What is 2+2?", "4"))
            saveFlashcards(context, flashcards)
        }

        // Main UI layout with centered content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display current flashcard question or answer
            Text(
                text = if (flashcards.isNotEmpty()) {
                    if (showingQuestion) flashcards[currentIndex].question else flashcards[currentIndex].answer
                } else "No flashcards available",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0E0E0))
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to toggle between question and answer
            Button(onClick = { showingQuestion = !showingQuestion }) {
                Text("Flip Card")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row for Previous and Next navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Navigate to previous flashcard
                Button(
                    onClick = {
                        if (currentIndex > 0) {
                            currentIndex--
                            showingQuestion = true
                        }
                    },
                    enabled = currentIndex > 0
                ) {
                    Text("Previous")
                }
                // Navigate to next flashcard
                Button(
                    onClick = {
                        if (currentIndex < flashcards.size - 1) {
                            currentIndex++
                            showingQuestion = true
                        }
                    },
                    enabled = currentIndex < flashcards.size - 1
                ) {
                    Text("Next")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button to open dialog for adding a new flashcard
            Button(onClick = { showAddDialog = true }) {
                Text("Add New Card")
            }

            // Dialog for adding a new flashcard
            if (showAddDialog) {
                var question by remember { mutableStateOf("") }
                var answer by remember { mutableStateOf("") }
                Dialog(onDismissRequest = { showAddDialog = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Add New Flashcard", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            // Input field for question
                            OutlinedTextField(
                                value = question,
                                onValueChange = { question = it },
                                label = { Text("Question") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Input field for answer
                            OutlinedTextField(
                                value = answer,
                                onValueChange = { answer = it },
                                label = { Text("Answer") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // Buttons for canceling or adding the flashcard
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showAddDialog = false }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    // Add new flashcard if inputs are valid
                                    if (question.isNotBlank() && answer.isNotBlank()) {
                                        flashcards = flashcards.toMutableList().apply {
                                            add(Flashcard(question, answer))
                                        }
                                        saveFlashcards(context, flashcards)
                                        currentIndex = flashcards.size - 1
                                        showingQuestion = true
                                        showAddDialog = false
                                        question = ""
                                        answer = ""
                                    }
                                }) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Saves the list of flashcards to SharedPreferences using Gson
    private fun saveFlashcards(context: Context, flashcards: List<Flashcard>) {
        // Initialize SharedPreferences and Gson
        val sharedPreferences = context.getSharedPreferences("FlashcardApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        // Convert flashcards to JSON and save
        val json = gson.toJson(flashcards)
        editor.putString("flashcards", json)
        editor.apply()
    }

    // Loads the list of flashcards from SharedPreferences using Gson
    private fun loadFlashcards(context: Context): MutableList<Flashcard> {
        // Initialize SharedPreferences and Gson
        val sharedPreferences = context.getSharedPreferences("FlashcardApp", Context.MODE_PRIVATE)
        val gson = Gson()
        // Retrieve and parse JSON data
        val json = sharedPreferences.getString("flashcards", null)
        val type = object : TypeToken<MutableList<Flashcard>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
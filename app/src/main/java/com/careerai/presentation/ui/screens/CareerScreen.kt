package com.careerai.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.careerai.domain.model.GoalCategory
import com.careerai.domain.model.GoalType
import com.careerai.domain.model.Priority
import com.careerai.presentation.viewmodel.CareerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreen(
    viewModel: CareerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<String?>(null) }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, description, category, priority, type ->
                viewModel.createGoal(title, description, category, priority, type)
                showAddGoalDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    goalToDelete?.let { goalId ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGoal(goalId)
                        goalToDelete = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Career Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showAddGoalDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }
        }

        item {
            // Stats from ViewModel
            val stats = uiState.goalStatistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Active", "${stats?.inProgressGoals ?: uiState.goals.size}", Modifier.weight(1f))
                StatCard("Completed", "${stats?.completedGoals ?: 0}", Modifier.weight(1f))
                StatCard("Total", "${stats?.totalGoals ?: uiState.goals.size}", Modifier.weight(1f))
            }
        }

        item {
            Text("Current Goals", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        if (uiState.goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No goals yet",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { showAddGoalDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Add Your First Goal")
                            }
                        }
                    }
                }
            }
        } else {
            items(uiState.goals, key = { it.id }) { goal ->
                GoalCard(
                    title = goal.title,
                    category = goal.category.name,
                    progress = goal.progress,
                    onDelete = { goalToDelete = goal.id },
                    onProgressUpdate = { newProgress ->
                        viewModel.updateGoalProgress(goal.id, newProgress)
                    }
                )
            }
        }

        item {
            Text("Skill Development", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.skills.isEmpty()) {
                    item { SkillChip("Add skills in Settings") }
                } else {
                    items(uiState.skills) { skill ->
                        SkillChip(skill.name)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Job Applications", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    JobApplicationItem("Senior Android Developer", "TechCorp", "Interview Scheduled")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    JobApplicationItem("Mobile App Lead", "StartupXYZ", "Application Sent")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    JobApplicationItem("Full Stack Developer", "BigTech Inc", "Pending Response")
                }
            }
        }

        // Error display
        uiState.error?.let { error ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
                LaunchedEffect(error) { viewModel.clearError() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, GoalCategory, Priority, GoalType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GoalCategory.CAREER) }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedType by remember { mutableStateOf(GoalType.SHORT_TERM) }
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        GoalCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = { selectedCategory = cat; expandedCategory = false }
                            )
                        }
                    }
                }
                // Priority dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = it }
                ) {
                    OutlinedTextField(
                        value = selectedPriority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        Priority.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = { selectedPriority = p; expandedPriority = false }
                            )
                        }
                    }
                }
                // Type selection
                Text("Type:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalType.values().take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name.replace("_", " "), fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description.ifBlank { null }, selectedCategory, selectedPriority, selectedType)
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("Add Goal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GoalCard(
    title: String,
    category: String,
    progress: Int,
    onDelete: () -> Unit,
    onProgressUpdate: (Int) -> Unit
) {
    var showSlider by remember { mutableStateOf(false) }
    var sliderValue by remember(progress) { mutableStateOf(progress.toFloat()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${progress}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showSlider = !showSlider }) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = "Update progress",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            if (showSlider) {
                Spacer(Modifier.height(8.dp))
                Text("Update Progress: ${sliderValue.toInt()}%", fontSize = 12.sp)
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..100f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { showSlider = false }) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onProgressUpdate(sliderValue.toInt())
                        showSlider = false
                    }) { Text("Save") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillChip(skill: String) {
    FilterChip(
        onClick = {},
        label = { Text(skill) },
        selected = false,
        leadingIcon = {
            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    )
}

@Composable
fun JobApplicationItem(position: String, company: String, status: String) {
    Column {
        Text(position, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(company, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(status, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
    }
}

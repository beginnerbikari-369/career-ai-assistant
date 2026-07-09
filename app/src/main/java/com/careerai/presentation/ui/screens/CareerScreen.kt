package com.careerai.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerScreen(
    viewModel: com.careerai.presentation.viewmodel.CareerViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Career Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { /* TODO: Add new goal */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }
        }

        item {
            // Career Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Active Goals", "8", Modifier.weight(1f))
                StatCard("Completed", "24", Modifier.weight(1f))
                StatCard("In Progress", "12", Modifier.weight(1f))
            }
        }

        item {
            // Goals Section
            Text(
                text = "Current Goals",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            val goals = listOf(
                Goal("Learn React Native", "Mobile Development", 75),
                Goal("Complete AWS Certification", "Cloud Computing", 45),
                Goal("Improve Public Speaking", "Soft Skills", 30),
                Goal("Network with 5 professionals", "Networking", 60)
            )
            
            goals.forEach { goal ->
                GoalCard(goal = goal)
            }
        }

        item {
            // Skills Development
            Text(
                text = "Skill Development",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listOf("Kotlin", "Cloud Architecture", "Leadership", "Data Analysis")) { skill ->
                    SkillChip(skill)
                }
            }
        }

        item {
            // Job Applications
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Work,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Job Applications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    JobApplicationItem("Senior Android Developer", "TechCorp", "Interview Scheduled")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    JobApplicationItem("Mobile App Lead", "StartupXYZ", "Application Sent")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    JobApplicationItem("Full Stack Developer", "BigTech Inc", "Pending Response")
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GoalCard(goal: Goal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = goal.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${goal.progress}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = goal.progress / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillChip(skill: String) {
    FilterChip(
        onClick = { /* TODO: Navigate to skill details */ },
        label = { Text(skill) },
        selected = false,
        leadingIcon = {
            Icon(
                Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

@Composable
fun JobApplicationItem(position: String, company: String, status: String) {
    Column {
        Text(
            text = position,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = company,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = status,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

data class Goal(
    val title: String,
    val category: String,
    val progress: Int
)
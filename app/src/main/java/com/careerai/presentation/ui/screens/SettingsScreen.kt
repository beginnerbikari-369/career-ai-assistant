package com.careerai.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your personal information"
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Privacy & Security",
                        subtitle = "Manage your privacy settings"
                    )
                }
            }
        }

        item {
            // AI & Chat Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "AI Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.Psychology,
                        title = "AI Model Settings",
                        subtitle = "Configure AI behavior and responses"
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Chat,
                        title = "Chat History",
                        subtitle = "Manage conversation data"
                    )
                    
                    var voiceEnabled by remember { mutableStateOf(true) }
                    SettingsToggleItem(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Voice Input",
                        subtitle = "Enable voice-to-text functionality",
                        checked = voiceEnabled,
                        onCheckedChange = { voiceEnabled = it }
                    )
                }
            }
        }

        item {
            // Notifications
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var calendarNotifications by remember { mutableStateOf(true) }
                    SettingsToggleItem(
                        icon = Icons.Default.EventNote,
                        title = "Calendar Reminders",
                        subtitle = "Get notified about upcoming events",
                        checked = calendarNotifications,
                        onCheckedChange = { calendarNotifications = it }
                    )
                    
                    var habitReminders by remember { mutableStateOf(true) }
                    SettingsToggleItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Habit Reminders",
                        subtitle = "Daily prompts for your habits",
                        checked = habitReminders,
                        onCheckedChange = { habitReminders = it }
                    )
                    
                    var aiSuggestions by remember { mutableStateOf(true) }
                    SettingsToggleItem(
                        icon = Icons.Default.Lightbulb,
                        title = "AI Suggestions",
                        subtitle = "Receive intelligent recommendations",
                        checked = aiSuggestions,
                        onCheckedChange = { aiSuggestions = it }
                    )
                }
            }
        }

        item {
            // Data & Sync
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Data & Sync",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        icon = Icons.Default.CloudSync,
                        title = "Cloud Sync",
                        subtitle = "Sync data across devices"
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.GetApp,
                        title = "Export Data",
                        subtitle = "Download your data as backup"
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.CalendarMonth,
                        title = "Calendar Integration",
                        subtitle = "Connect with Google Calendar"
                    )
                }
            }
        }

        item {
            // App Settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "App Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var darkMode by remember { mutableStateOf(false) }
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "English (US)"
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "Version 1.0.0"
                    )
                }
            }
        }

        item {
            // Sign Out
            OutlinedButton(
                onClick = { /* TODO: Handle sign out */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { /* TODO: Handle click */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
package com.careerai.presentation.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.careerai.domain.model.ConversationContext
import com.careerai.presentation.viewmodel.ChatViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // ── TTS state ──────────────────────────────────────────────
    var ttsEnabled by remember { mutableStateOf(true) }
    var isSpeaking by remember { mutableStateOf(false) }
    val tts = remember {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale.getDefault()
                instance?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { isSpeaking = true }
                    override fun onDone(utteranceId: String?) { isSpeaking = false }
                    override fun onError(utteranceId: String?) { isSpeaking = false }
                })
            }
        }
        instance
    }
    DisposableEffect(Unit) { onDispose { tts?.stop(); tts?.shutdown() } }

    // speak last AI message whenever it changes and TTS is on
    val lastAiMessage = uiState.messages.lastOrNull { !it.isFromUser }
    LaunchedEffect(lastAiMessage?.id) {
        if (ttsEnabled && lastAiMessage != null) {
            tts?.speak(lastAiMessage.content, TextToSpeech.QUEUE_FLUSH, null, "ai_msg")
        }
    }

    // ── Voice input state ───────────────────────────────────────
    var isListening by remember { mutableStateOf(false) }
    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                viewModel.updateCurrentMessage(spoken)
            }
        }
    }

    fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your message…")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        isListening = true
        speechLauncher.launch(intent)
    }

    // ── Auto-create conversation ────────────────────────────────
    LaunchedEffect(Unit) {
        if (uiState.selectedConversationId == null && uiState.conversations.isNotEmpty()) {
            viewModel.selectConversation(uiState.conversations.first().id)
        } else if (uiState.conversations.isEmpty()) {
            viewModel.createNewConversation(ConversationContext.GENERAL)
        }
    }

    // ── Auto-scroll to bottom on new message ───────────────────
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(0)
    }

    // ── Mic pulse animation ────────────────────────────────────
    val micScale by rememberInfiniteTransition(label = "mic").animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "scale"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Chat Assistant", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {
                ttsEnabled = !ttsEnabled
                if (!ttsEnabled) tts?.stop()
            }) {
                Icon(
                    imageVector = if (ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (ttsEnabled) "Mute voice" else "Unmute voice",
                    tint = if (ttsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Error banner ────────────────────────────────────────
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp
                )
            }
        }

        // ── Context chips ───────────────────────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(ConversationContext.values()) { ctx ->
                FilterChip(
                    onClick = {
                        viewModel.switchContext(ctx)
                        viewModel.createNewConversation(ctx)
                    },
                    label = { Text(ctx.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    selected = ctx == uiState.selectedContext
                )
            }
        }

        // ── Messages list ───────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            if (uiState.isLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TypingIndicator()
                    }
                }
            }

            items(uiState.messages.reversed(), key = { it.id }) { message ->
                MessageBubble(
                    message = ChatUiMessage(
                        sender = if (message.isFromUser) "You" else "AI",
                        content = message.content,
                        isFromAI = !message.isFromUser
                    ),
                    onSpeakClick = { content ->
                        if (ttsEnabled) tts?.speak(content, TextToSpeech.QUEUE_FLUSH, null, "manual")
                    }
                )
            }
        }

        // ── Input bar ───────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic button
                IconButton(
                    onClick = {
                        if (!micPermission.status.isGranted) {
                            micPermission.launchPermissionRequest()
                        } else {
                            launchSpeechRecognizer()
                        }
                    },
                    modifier = if (isListening) Modifier.scale(micScale) else Modifier
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = if (isListening) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.primary
                    )
                }

                // Text field
                OutlinedTextField(
                    value = uiState.currentMessage,
                    onValueChange = { viewModel.updateCurrentMessage(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type or speak…") },
                    shape = RoundedCornerShape(20.dp),
                    enabled = !uiState.isLoading,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )

                // Send button
                IconButton(
                    onClick = {
                        if (uiState.currentMessage.isNotBlank() && !uiState.isLoading) {
                            viewModel.sendMessage(uiState.currentMessage)
                        }
                    },
                    enabled = uiState.currentMessage.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (uiState.currentMessage.isNotBlank() && !uiState.isLoading)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatUiMessage, onSpeakClick: (String) -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromAI) Arrangement.Start else Arrangement.End
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromAI)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isFromAI) 4.dp else 16.dp,
                bottomEnd = if (message.isFromAI) 16.dp else 4.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.sender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (message.isFromAI) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onPrimary
                    )
                    if (message.isFromAI) {
                        IconButton(
                            onClick = { onSpeakClick(message.content) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Speak",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (message.isFromAI) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val dot1 by transition.animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(400, delayMillis = 0), RepeatMode.Reverse), "d1"
    )
    val dot2 by transition.animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(400, delayMillis = 150), RepeatMode.Reverse), "d2"
    )
    val dot3 by transition.animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(400, delayMillis = 300), RepeatMode.Reverse), "d3"
    )

    Card(
        modifier = Modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(dot1, dot2, dot3).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

data class ChatUiMessage(
    val sender: String,
    val content: String,
    val isFromAI: Boolean
)

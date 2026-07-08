package com.careerai.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

// Date and Time Extensions
fun Long.toDateString(pattern: String = Constants.DATE_FORMAT_DISPLAY): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.toTimeString(): String {
    val formatter = SimpleDateFormat(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault())
    return formatter.format(Date(this))
}

fun Long.isToday(): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = this@isToday }
    
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

fun Long.isThisWeek(): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = this@isThisWeek }
    
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
           today.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR)
}

// String Extensions
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
}

// Context Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showLongToast(message: String) {
    showToast(message, Toast.LENGTH_LONG)
}

// Composable Extensions
@Composable
fun showToast(message: String) {
    val context = LocalContext.current
    context.showToast(message)
}

// Progress Extensions
fun Int.toProgressFloat(): Float = (this.coerceIn(0, 100) / 100f)

fun Float.toProgressPercent(): Int = (this.coerceIn(0f, 1f) * 100).toInt()

// Collection Extensions
fun <T> List<T>.safe(index: Int): T? = if (index in indices) this[index] else null

// Result Extensions
inline fun <T> Result<T>.onSuccessOrFailure(
    onSuccess: (T) -> Unit,
    onFailure: (Throwable) -> Unit
) {
    if (isSuccess) {
        onSuccess(getOrThrow())
    } else {
        onFailure(exceptionOrNull() ?: Exception("Unknown error"))
    }
}

// Time formatting for habits and goals
fun Long.formatDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

// Habit streak formatting
fun Int.formatStreak(): String = when {
    this >= 365 -> "${this / 365}y ${(this % 365) / 30}m"
    this >= 30 -> "${this / 30}m ${this % 30}d"
    this >= 7 -> "${this / 7}w ${this % 7}d"
    else -> "${this}d"
}

// Goal progress formatting
fun Int.formatProgress(): String = "${this.coerceIn(0, 100)}%"

// Safe navigation for nullable collections
fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun <K, V> Map<K, V>?.orEmpty(): Map<K, V> = this ?: emptyMap()

// Validation extensions
fun String.isValidGoalTitle(): Boolean = this.isNotBlank() && this.length <= Constants.MAX_GOAL_TITLE_LENGTH

fun String.isValidHabitName(): Boolean = this.isNotBlank() && this.length <= Constants.MAX_HABIT_NAME_LENGTH

fun String.isValidMessage(): Boolean = this.isNotBlank() && this.length <= Constants.MAX_MESSAGE_LENGTH
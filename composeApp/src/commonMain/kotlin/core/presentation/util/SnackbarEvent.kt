package core.presentation.util

import androidx.compose.material3.SnackbarDuration

data class SnackbarEvent(
    val message: String,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

data class SnackbarAction(
    val name: String,
    val action: suspend () -> Unit
)
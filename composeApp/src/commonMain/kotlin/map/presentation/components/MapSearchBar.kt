package map.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import map.presentation.MapEvent

@Composable
fun MapSearchBar(
    onEvent: (MapEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = { onEvent(MapEvent.NavigateUp) },
            modifier = Modifier
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back button",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colors.onPrimary
            )
        }
    }
}
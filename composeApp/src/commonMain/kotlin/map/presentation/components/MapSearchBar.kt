package map.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.presentation.ui.theme.sunnyOnPrimary
import core.presentation.ui.theme.sunnyPrimary
import map.presentation.MapEvent

@Composable
fun MapSearchBar(
    modifier: Modifier = Modifier,
    onEvent: (MapEvent) -> Unit,
) {
    Row(modifier = modifier) {
        IconButton(
            modifier = Modifier
                .background(
                    color = sunnyPrimary,
                    shape = CircleShape
                )
                .size(48.dp),
            onClick = { onEvent(MapEvent.NavigateUp) },
            content = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back button",
                    tint = sunnyOnPrimary
                )
            },
        )
    }
}
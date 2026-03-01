package home.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import weather.domain.model.Neighbor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborWeatherSlider(
    neighborWeight: Pair<Neighbor, Float>,
    modifier: Modifier = Modifier,
    onSlide: (Pair<Neighbor, Double>) -> Unit = {},
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = neighborWeight.first.toTextString(),
            color = colorScheme.onPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.35f)
        )
        Slider(
            value = neighborWeight.second,
            onValueChange = { newValue ->
                onSlide(neighborWeight.first to newValue.toDouble())
            },
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.primary,
                activeTrackColor = colorScheme.onPrimary,
                inactiveTrackColor = colorScheme.secondary,
            ),
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.onPrimary,
                        activeTrackColor = colorScheme.onPrimary,
                        inactiveTrackColor = colorScheme.secondary,
                    ),
                    thumbSize = DpSize(4.dp, 20.dp),
                )
            },
            steps = 0,
            valueRange = 0f..1f,
            modifier = Modifier.weight(0.65f)
        )
    }
}
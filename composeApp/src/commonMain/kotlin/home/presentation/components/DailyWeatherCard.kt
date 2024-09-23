package home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import weather.domain.model.DailyWeather

@Composable
fun DailyWeatherCard(
    dailyWeathers: List<DailyWeather>,
    modifier: Modifier = Modifier,
    baseGradientColor: Color = MaterialTheme.colors.secondary,
    tint: Color = MaterialTheme.colors.onSecondary,
) {
    val updatedDailyWeathers by rememberUpdatedState(dailyWeathers)
    Column(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        baseGradientColor.copy(alpha = 0.3f),
                        baseGradientColor.copy(alpha = 0.7f),
                    ),
                    start = Offset(Float.POSITIVE_INFINITY, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY),
                ),
                shape = RoundedCornerShape(25.dp)
            )
            .padding(top = 15.dp, bottom = 20.dp, start = 15.dp, end = 15.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        updatedDailyWeathers.forEach {
            key(it.time.toString()) {
                DailyWeatherItem(
                    dailyWeather = it,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    tint = tint
                )
            }
        }

    }
}
package home.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.presentation.util.getScreenWidth
import dev.jordond.compass.Place
import map.domain.util.toPlaceIdentifier
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import org.jetbrains.compose.resources.painterResource
import weather.domain.model.Neighbor
import weather.domain.model.Weather

@Composable
fun HomeNavigationDrawer(
    items: List<Pair<Place, Weather>>,
    neighborWeights: Map<Neighbor, Double>,
    selectedItem: Pair<Place, Weather>?,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    topMaxWidth: Dp = 360.dp,
    bottomMaxWidth: Dp = 500.dp,
    onItemClick: (Pair<Place, Weather>) -> Unit = {},
    onSlideNeighborWeight: (Pair<Neighbor, Double>) -> Unit = {},
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val density = LocalDensity.current
        val screenWidth = getScreenWidth()
        var contentSizePx by remember { mutableStateOf(IntSize.Zero) }
        val topMaxWidthPx =  remember(topMaxWidth) { with(density) { topMaxWidth.toPx() } }
        val bottomMaxWidthPx = remember(bottomMaxWidth) { with(density) { bottomMaxWidth.toPx() } }
        val buttonSizePx = remember { with(density) { 48.dp.toPx() } }
        val buttonPaddingSizePx = remember { with(density) { 8.dp.toPx() } }
        val calculatedDrawerWidthPx by remember {
            derivedStateOf {
                val availableContentWidth = contentSizePx.width.toFloat()
                val contentWidth = minOf(bottomMaxWidthPx, availableContentWidth) - buttonSizePx - buttonPaddingSizePx * 2

                val exceedMaxWidth = contentSizePx.width > (bottomMaxWidthPx + buttonSizePx + buttonPaddingSizePx)
                if (exceedMaxWidth) {
                    contentWidth + buttonSizePx
                } else {
                    val topWidthAdjustment = ((contentSizePx.width - topMaxWidthPx).takeIf { it >= 0 } ?: 0f) / 2f
                    contentWidth - topWidthAdjustment
                }
            }
        }
        val calculatedContentWidth = remember(calculatedDrawerWidthPx) {
            with(density) { calculatedDrawerWidthPx.toDp() }
        }
        // Fix drawer automatically opening bug when initial composition is triggered
        LaunchedEffect(drawerState.targetValue, selectedItem) {
            if (selectedItem == null) {
                drawerState.snapTo(DrawerValue.Closed)
            }
        }

        val scrollState = rememberScrollState()

        DismissibleNavigationDrawer(
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DismissibleDrawerSheet(
                        modifier = Modifier
                            .graphicsLayer {
                                val offset = -0.5f * (screenWidth.toPx() - (minOf(bottomMaxWidthPx, contentSizePx.width.toFloat()) + 2 * buttonPaddingSizePx))
                                val exceedMaxWidth = contentSizePx.width > (bottomMaxWidthPx + buttonSizePx + buttonPaddingSizePx)
                                translationX = if (exceedMaxWidth) offset else 0f
                            }
                            .requiredWidth(calculatedContentWidth)
                            .fillMaxHeight(),
                        drawerContainerColor = Color.Transparent,
                        drawerContentColor = Color.Transparent,
                    ) {
                        val isOpening by remember { derivedStateOf {
                            (drawerState.currentOffset > -calculatedDrawerWidthPx * 0.9)
                                    || (drawerState.isClosed && drawerState.targetValue == DrawerValue.Open)
                                    || drawerState.isOpen
                        } }
                        AnimatedVisibility(
                            visible = isOpening,
                            modifier = Modifier,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
//                            // Fix drawer automatically opening bug when initial composition is triggered
//                            LaunchedEffect(Unit) {
//                                if (drawerState.currentValue == DrawerValue.Open) {
//                                    drawerState.snapTo(DrawerValue.Closed)
//                                }
//                            }
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                                    .padding(end = 12.dp)
                                    .padding(vertical = 28.dp)
                            ) {
                                items.forEach { item ->
                                    key(item.first) {
                                        NavigationDrawerItem(
                                            label = {
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = "View on map",
                                                        tint = colorScheme.onPrimary,
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .padding(end = 6.dp)
                                                    )
                                                    Text(
                                                        text = item.first.toPlaceIdentifier(),
                                                        color = colorScheme.onPrimary,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        overflow = TextOverflow.Ellipsis,
                                                        maxLines = 1,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    WeatherIcon(
                                                        weatherType = item.second.current.weatherType,
                                                        modifier = Modifier.size(24.dp),
                                                        colorFilter = ColorFilter.tint(colorScheme.onPrimary)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${item.second.current.temperature}",
                                                        color = colorScheme.onPrimary,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium,
                                                    )
                                                    Spacer(modifier = Modifier.width(0.5.dp))
                                                    Image(
                                                        painter = painterResource(Res.drawable.icon_temperature_unit_sign),
                                                        contentDescription = "Temperature unit sign",
                                                        modifier = Modifier
                                                            .align(Alignment.Top)
                                                            .padding(top = 6.dp)
                                                            .size(5.dp),
                                                        alignment = Alignment.TopCenter,
                                                        colorFilter = ColorFilter.tint(colorScheme.onPrimary)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = "Go to map",
                                                        tint = colorScheme.onPrimary.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            },
                                            selected = selectedItem?.first?.toPlaceIdentifier() == item.first.toPlaceIdentifier(),
                                            onClick = { onItemClick(item) },
                                            modifier = Modifier.align(Alignment.Start),
                                            colors = NavigationDrawerItemDefaults.colors(
                                                selectedContainerColor = colorScheme.primary,
                                                selectedTextColor = colorScheme.onPrimary,
                                                selectedIconColor = colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .background(
                                            color = colorScheme.primary,
                                            shape = RoundedCornerShape(25.dp)
                                        )
                                ) {
                                    neighborWeights.forEach { weight ->
                                        key(weight.key) {
                                            NavigationDrawerItem(
                                                label = {
                                                    NeighborWeatherSlider(
                                                        neighborWeight = weight.key to weight.value.toFloat(),
                                                        onSlide = { onSlideNeighborWeight(it) },
                                                        colorScheme = colorScheme
                                                    )
                                                },
                                                selected = false,
                                                onClick = {},
                                                modifier = Modifier,
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Adjust Forecast Contribution",
                                        color = colorScheme.onPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .padding(bottom = 6.dp, end = 12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            modifier = modifier,
            drawerState = drawerState,
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(modifier = Modifier.onGloballyPositioned { contentSizePx = it.size }) {
                    content()
                }
            }
        }
    }
}
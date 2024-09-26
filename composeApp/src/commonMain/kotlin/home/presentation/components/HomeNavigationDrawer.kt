package home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jordond.compass.Place
import map.domain.util.toPlaceIdentifier
import neighborweather.composeapp.generated.resources.Res
import neighborweather.composeapp.generated.resources.icon_temperature_unit_sign
import org.jetbrains.compose.resources.painterResource
import weather.domain.model.Weather

@Composable
fun HomeNavigationDrawer(
    items: List<Pair<Place, Weather>>,
    selectedItem: Pair<Place, Weather>?,
    modifier: Modifier = Modifier,
    onItemClick: (Pair<Place, Weather>) -> Unit = {},
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        DismissibleNavigationDrawer(
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DismissibleDrawerSheet(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.6f)
                            .fillMaxHeight()
                            .padding(16.dp),
                        drawerShape = RoundedCornerShape(16.dp),
                        drawerContainerColor = Color.Transparent,
                        drawerContentColor = Color.Transparent
                    ) {
                        Column {
                            items.forEach { item ->
                                key(item.first) {
                                    NavigationDrawerItem(
                                        label = {
                                            Row(horizontalArrangement = Arrangement.Center) {
                                                Text(
                                                    text = item.first.toPlaceIdentifier(),
                                                    color = colorScheme.onPrimary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                                Text(
                                                    text = ": ${item.second.current.temperature}",
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
                                                        .padding(top = 4.dp)
                                                        .size(5.dp),
                                                    alignment = Alignment.TopCenter,
                                                    colorFilter = ColorFilter.tint(colorScheme.onPrimary)
                                                )
                                            }
                                        },
                                        onClick = { onItemClick(item) },
                                        selected = selectedItem?.first?.toPlaceIdentifier() == item.first.toPlaceIdentifier(),
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = colorScheme.primary,
                                            selectedTextColor = colorScheme.onPrimary,
                                            selectedIconColor = colorScheme.onPrimary
                                        )
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
                content()
            }
        }
    }
}
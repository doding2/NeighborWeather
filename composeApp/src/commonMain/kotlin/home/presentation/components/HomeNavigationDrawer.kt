//package home.presentation.components
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.asPaddingValues
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.offset
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.safeDrawing
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.windowInsetsPadding
//import androidx.compose.material.Colors
//import androidx.compose.material.DrawerValue
//import androidx.compose.material.ExperimentalMaterialApi
//import androidx.compose.material.MaterialTheme
//import androidx.compose.material.ModalDrawer
//import androidx.compose.material.Text
//import androidx.compose.material.rememberDrawerState
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Outline
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.Density
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.LayoutDirection
//import androidx.compose.ui.unit.dp
//import dev.jordond.compass.Place
//import weather.domain.model.Weather
//
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun HomeNavigationDrawer(
//    selectedItem: Pair<Place, Weather>? = null,
//    items: List<Pair<Place, Weather>> = emptyList(),
//    onItemClick: (Pair<Place, Weather>) -> Unit = {},
//    width: Dp,
//    height: Dp,
//    modifier: Modifier = Modifier,
//    colors: Colors = MaterialTheme.colors,
//    content: @Composable () -> Unit
//) {
//    val widthPx = with(LocalDensity.current) { width.toPx() }
//    val heightPx = with(LocalDensity.current) {
//        (WindowInsets.safeDrawing.asPaddingValues().run {
//            calculateTopPadding() + calculateBottomPadding()
//        } + height).toPx()
//    }
//    val drawerMinOffsetAbs = -with(LocalDensity.current) { (-360).dp.toPx() }.dp
//    val drawerState = rememberDrawerState(DrawerValue.Open)
//
//    ModalDrawer(
//        drawerContent = {
//            Column(
//                modifier = Modifier
//                    .width(width)
//                    .fillMaxHeight()
//                    .windowInsetsPadding(WindowInsets.safeDrawing)
//            ) {
//                Text("Drawer title", modifier = Modifier.padding(16.dp))
//            }
//        },
//        modifier = modifier,
//        drawerState = drawerState,
//        gesturesEnabled = true,
//        drawerShape = object : Shape {
//            override fun createOutline(
//                size: Size,
//                layoutDirection: LayoutDirection,
//                density: Density,
//            ): Outline {
//                return Outline.Rectangle(Rect(0f,0f, widthPx, heightPx))
//            }
//        },
//        drawerElevation = 0.dp,
//        drawerBackgroundColor = Color.Transparent,
//        scrimColor = Color.Transparent,
//    ) {
//        Box(
//            modifier = Modifier
//                .offset(x = (drawerState.offset.dp + drawerMinOffsetAbs) * (width / drawerMinOffsetAbs))
//        ) {
//            content()
//        }
//    }
//}
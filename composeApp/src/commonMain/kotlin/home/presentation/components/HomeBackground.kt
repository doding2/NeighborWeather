package home.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeBackground(
    backgroundImage: DrawableResource,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = backgroundImage,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(1000, delayMillis = 0))
                .togetherWith(fadeOut(animationSpec = tween(500)))
        }
    ) { background ->
        Image(
            painter = painterResource(background),
            contentDescription = "Home background image",
            alignment = Alignment.BottomCenter,
            contentScale = ContentScale.Crop
        )
    }
}
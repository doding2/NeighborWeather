package core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
fun <T> ObserveEffectsOnLifecycle(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEffect: (T) -> Unit
) {
    // flow가 compose state에 들어있는 것을 사용할 수도 있기 때문에
    // (recomposition으로 값이 변할 수도 있기 때문에) LaunchedEffect key로 넣음
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle, key1, key2, flow) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEffect)
            }
        }
    }
}
package home.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    fun onEvent(event: HomeEvent) {

    }
}
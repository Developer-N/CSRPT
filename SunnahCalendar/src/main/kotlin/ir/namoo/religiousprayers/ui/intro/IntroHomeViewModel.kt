package ir.namoo.religiousprayers.ui.intro

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class IntroHomeViewModel : ViewModel() {

    private val _selectedScreen = MutableStateFlow(IntroScreen.Welcome)
    val selectedScreen = _selectedScreen.asStateFlow()

    fun selectScreen(screen: IntroScreen) {
        _selectedScreen.value = screen
    }
}

package ir.namoo.religiousprayers.ui.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IntroHomeViewModel : ViewModel() {

    private val _selectedScreen = MutableStateFlow(IntroScreen.Welcome)
    val selectedScreen = _selectedScreen.asStateFlow()

    fun selectScreen(screen: IntroScreen) {
        viewModelScope.launch {
            _selectedScreen.value = screen
        }
    }
}

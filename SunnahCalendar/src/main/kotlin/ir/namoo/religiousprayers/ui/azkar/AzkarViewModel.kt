package ir.namoo.religiousprayers.ui.azkar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AzkarViewModel @Inject constructor(private val azkarDB: AzkarDB) : ViewModel() {

    private val _azkarTitles = MutableLiveData<List<AzkarTitles>>()
    val azkarTitles: MutableLiveData<List<AzkarTitles>> get() = _azkarTitles


    fun loadAzkarTitles() {
        viewModelScope.launch {
            _azkarTitles.value = azkarDB.azkarsDAO().getAllAzkarsTitle()
        }
    }

    fun updateAzkarTitle(title: AzkarTitles) {
        viewModelScope.launch {
            azkarDB.azkarsDAO().updateAzkarTitle(title)
        }
    }

}//end of class

package ir.namoo.quran

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.qari.QariEntity
import ir.namoo.quran.qari.QariRepository
import ir.namoo.quran.utils.DEFAULT_PAGE_TYPE
import ir.namoo.quran.utils.PREF_PAGE_TYPE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("SdCardPath")
class QuranActivityViewModel(
    private val qariRepository: QariRepository,
    private val prefs: SharedPreferences,
) : ViewModel() {
    private val _isDBExist = MutableStateFlow(false)
    val isDBExist = _isDBExist.asStateFlow()

    private val _qariList = MutableStateFlow(emptyList<QariEntity>())
    val qariList = _qariList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _pageType = MutableStateFlow(DEFAULT_PAGE_TYPE)
    val pageType = _pageType.asStateFlow()


    fun reload() {
        _pageType.value = prefs.getInt(PREF_PAGE_TYPE, DEFAULT_PAGE_TYPE)
    }

    @SuppressLint("SdCardPath")
    fun checkDBAndQari(packageName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _isDBExist.value = File("/data/data/${packageName}/databases/quran_v3.db").exists()
            _qariList.value = qariRepository.getLocalQariList()
            if (_qariList.value.isEmpty()) _qariList.value = qariRepository.getQariList()
            _isLoading.value = false
        }
    }
}

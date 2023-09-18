package ir.namoo.religiousprayers.ui.intro

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.utils.appPrefs
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.repository.PrayTimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IntroDownloadViewModel(
    private val prayTimeRepository: PrayTimeRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _addedCityList = MutableStateFlow(emptyList<CityModel>())
    val addedCityModel = query.combine(_addedCityList) { text, list ->
        when {
            text.isNotEmpty() -> list.filter { it.name.contains(text) }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _addedCityList.value)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _downloadingCityId = MutableStateFlow(-1)
    val downloadingCityID = _downloadingCityId.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            withContext(Dispatchers.IO) {
                prayTimeRepository.updateAndGetCityList()
                _addedCityList.value = prayTimeRepository.getAddedCity()
            }
            _isLoading.value = false
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            prayTimeRepository.updateAndGetCityList()
            _addedCityList.value = prayTimeRepository.getAddedCity()
            _isLoading.value = false
        }
    }

    fun updateQuery(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _query.value = query
            _isLoading.value = false
        }
    }

    fun downloadAndStart(context: Context, cityModel: CityModel, startMainActivity: () -> Unit) {
        viewModelScope.launch {
            _downloadingCityId.value = cityModel.id
            val res = prayTimeRepository.getTimesForCityAndSaveToLocalDB(cityModel)
            if (res) {
                context.appPrefs.edit {
                    putString(PREF_GEOCODED_CITYNAME, cityModel.name)
                    putString(PREF_LATITUDE, cityModel.latitude.toString())
                    putString(PREF_LONGITUDE, cityModel.longitude.toString())
                    putString(PREF_SELECTED_LOCATION, "")
                    putBoolean(PREF_FIRST_START, false)
                }
                startMainActivity()
            } else {
                //error
            }
        }
    }

}//end of class IntroDownloadViewModel

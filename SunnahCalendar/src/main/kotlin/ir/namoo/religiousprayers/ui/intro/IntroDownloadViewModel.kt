package ir.namoo.religiousprayers.ui.intro

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.utils.preferences
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IntroDownloadViewModel(
    private val prayTimeRepository: PrayTimeRepository
) : ViewModel() {
    private val _addedCityList = mutableStateListOf<CityModel>()
    val addedCityModel = _addedCityList

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _downloadingCityId = MutableStateFlow(-1)
    val downloadingCityID = _downloadingCityId.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            prayTimeRepository.getAddedCity().collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        _isLoading.value = false
                    }

                    DataState.Loading -> _isLoading.value = true
                    is DataState.Success -> {
                        _addedCityList.clear()
                        _addedCityList.addAll(state.data)
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun downloadAndStart(context: Context, cityModel: CityModel, startMainActivity: () -> Unit) {
        viewModelScope.launch {
            _downloadingCityId.value = cityModel.id
            prayTimeRepository.getTimesForCityAndSaveToLocalDB(cityModel.id)
                .collectLatest { state ->
                    when (state) {
                        is DataState.Error -> {
                            _downloadingCityId.value = -1
                            Log.e("IntroDownloadViewModel", "downloadAndStart: ${state.message}")
                        }

                        DataState.Loading -> {}
                        is DataState.Success -> {
                            if (state.data) {
                                context.preferences.edit {
                                    putString(PREF_GEOCODED_CITYNAME, cityModel.name)
                                    putString(PREF_LATITUDE, cityModel.latitude.toString())
                                    putString(PREF_LONGITUDE, cityModel.longitude.toString())
                                    putString(PREF_SELECTED_LOCATION, "")
                                    putBoolean(PREF_FIRST_START, false)
                                }
                                startMainActivity()
                            }
                        }
                    }
                }
        }
    }

}//end of class IntroDownloadViewModel

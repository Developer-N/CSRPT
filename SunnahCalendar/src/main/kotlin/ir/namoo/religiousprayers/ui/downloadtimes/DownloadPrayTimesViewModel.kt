package ir.namoo.religiousprayers.ui.downloadtimes

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
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.utils.preferences
import ir.namoo.commons.PREF_LAST_UPDATE_PRAY_TIMES_KEY
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDAO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadPrayTimesViewModel(
    private val prayTimesRepository: PrayTimeRepository,
    private val downloadedPrayTimesDAO: DownloadedPrayTimesDAO,
    private val locationsDB: LocationsDB
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSearchBoxIsOpen = MutableStateFlow(false)
    val isSearchBoxIsOpen = _isSearchBoxIsOpen.asStateFlow()

    private val _cityItemState = mutableStateListOf<CityItemState>()
    val cityIteState = _cityItemState

    fun loadData(context: Context) {
        viewModelScope.launch {
            val selectedCity = context.preferences.getString(PREF_GEOCODED_CITYNAME, "") ?: ""
            val downloadedIDs = downloadedPrayTimesDAO.getDownloadedCitiesID()
            prayTimesRepository.getAddedCity().collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        _isLoading.value = false
                    }

                    DataState.Loading -> {
                        _isLoading.value = true
                    }

                    is DataState.Success -> {
                        _cityItemState.clear()
                        _cityItemState.addAll(state.data.sortedBy { city -> city.name }.map {
                            CityItemState(
                                id = it.id,
                                name = it.name,
                                latitude = it.latitude,
                                longitude = it.longitude,
                                lastUpdate = it.lastUpdate,
                                isDownloading = false,
                                isDownloaded = downloadedIDs.contains(it.id),
                                isSelected = it.name == selectedCity
                            )
                        })
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun download(city: CityItemState, context: Context) {
        viewModelScope.launch {
            val index = cityIteState.indexOf(city)
            if (locationsDB.cityDAO().getAllCity().find { it.id == city.id } == null) {
                prayTimesRepository.getAndUpdateCities().collectLatest { state ->
                    when (state) {
                        is DataState.Error -> {
                            _cityItemState[index] =
                                _cityItemState[index].copy(isDownloading = false)
                        }

                        DataState.Loading -> {
                            _cityItemState[index] = _cityItemState[index].copy(isDownloading = true)
                        }

                        is DataState.Success -> {
                            getTimes(city, index, context)
                        }
                    }
                }
            } else {
                getTimes(city, index, context)
            }
        }
    }

    private suspend fun getTimes(city: CityItemState, index: Int, context: Context) {
        prayTimesRepository.getTimesForCityAndSaveToLocalDB(city.id)
            .collectLatest { pState ->
                when (pState) {
                    is DataState.Error -> {
                        Log.e(
                            "DownloadTimesViewModel",
                            "download: ${pState.message}"
                        )
                        _cityItemState[index] = _cityItemState[index].copy(
                            isDownloading = false,
                            isDownloaded = false,
                        )
                    }

                    DataState.Loading -> {
                        _cityItemState[index] = _cityItemState[index].copy(isDownloading = true)
                    }

                    is DataState.Success -> {
                        val oldSelected =
                            cityIteState.indexOfFirst { it.isSelected }
                        if (oldSelected != -1) {
                            _cityItemState[oldSelected] =
                                _cityItemState[oldSelected].copy(isSelected = false)
                        }
                        context.preferences.edit {
                            putString(PREF_GEOCODED_CITYNAME, city.name)
                            putString(PREF_LATITUDE, city.latitude.toString())
                            putString(PREF_LONGITUDE, city.longitude.toString())
                            putString(PREF_SELECTED_LOCATION, "")
                        }
                        context.appPrefsLite.edit {
                            putLong(PREF_LAST_UPDATE_PRAY_TIMES_KEY, Jdn.today().value)
                        }
                        _cityItemState[index] = _cityItemState[index].copy(
                            isDownloading = false,
                            isDownloaded = true,
                            isSelected = true
                        )
                    }
                }
            }
    }

    fun openSearch() {
        _isSearchBoxIsOpen.value = true
    }

    fun closeSearch() {
        _isSearchBoxIsOpen.value = false
    }
}//end of class

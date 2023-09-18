package ir.namoo.religiousprayers.ui.downloadtimes

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDAO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadPrayTimesViewModel constructor(
    private val prayTimesRepository: PrayTimeRepository,
    private val downloadedPrayTimesDAO: DownloadedPrayTimesDAO,
    private val locationsDB: LocationsDB
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _addedCities = MutableStateFlow(emptyList<CityModel>())
    val addedCities = query.combine(_addedCities) { text, list ->
        when {
            text.isEmpty() -> list
            else -> list.filter { it.name.contains(text) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _addedCities.value)

    private var downloadCitiesID by mutableStateOf(listOf<Int>())

    private val _cityItemState = MutableStateFlow(emptyList<CityItemState>())
    val cityIteState = _cityItemState.asStateFlow()

    private val _selectedCity = MutableStateFlow("")
    val selectedCity = _selectedCity.asStateFlow()

    fun loadData(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCity.value = context.appPrefs.getString(PREF_GEOCODED_CITYNAME, "") ?: ""
            _addedCities.value =
                prayTimesRepository.getAddedCity().sortedBy { city -> city.name }
            downloadCitiesID = downloadedPrayTimesDAO.getCities()
            val tmp = mutableListOf<CityItemState>().apply {
                for (c in _addedCities.value) add(CityItemState().apply {
                    if (downloadCitiesID.contains(c.id)) isDownloaded = true
                })
            }
            _cityItemState.value = tmp
            _isLoading.value = false
        }
    }

    fun download(city: CityModel, context: Context) {
        viewModelScope.launch {
            runCatching {
                if (locationsDB.cityDAO().getAllCity().find { it.id == city.id } == null) {
                    prayTimesRepository.updateAndGetCityList()
                }
                val index = addedCities.value.indexOf(city)
                cityIteState.value[index].isDownloading = true

                val isDownloaded = prayTimesRepository.getTimesForCityAndSaveToLocalDB(city)

                downloadCitiesID = downloadedPrayTimesDAO.getCities()
                context.appPrefs.edit {
                    putString(PREF_GEOCODED_CITYNAME, city.name)
                    putString(PREF_LATITUDE, city.latitude.toString())
                    putString(PREF_LONGITUDE, city.longitude.toString())
                    putString(PREF_SELECTED_LOCATION, "")
                }
                cityIteState.value[index].isDownloaded = isDownloaded
                cityIteState.value[index].isDownloading = false
                _selectedCity.value = city.name
            }.onFailure(logException)
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _query.value = query
            _isLoading.value = false
        }
    }
}//end of class

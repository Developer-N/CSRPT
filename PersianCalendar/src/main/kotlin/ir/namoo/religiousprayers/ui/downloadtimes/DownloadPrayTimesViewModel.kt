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
import ir.namoo.commons.model.PrayTimesModel
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.repository.asDataState
import ir.namoo.commons.utils.modelToDBTimes
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDAO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DownloadPrayTimesViewModel constructor(
    private val prayTimesRepository: PrayTimeRepository,
    private val downloadedPrayTimesDAO: DownloadedPrayTimesDAO
) : ViewModel() {

    var serverCitiesList by mutableStateOf((listOf<CityModel>()))
        private set
    var downloadCitiesID by mutableStateOf(listOf<Int>())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var citiesState by mutableStateOf(listOf<CityItemState>())
        private set
    var searchQuery by mutableStateOf("")
        private set
    private var getCitiesJob: Job? = null

    fun loadAddedCities() {
        runCatching {
            getCitiesJob?.cancel()
            getCitiesJob = viewModelScope.launch {
                prayTimesRepository.getAddedCities().collect {
                    when (it.asDataState()) {
                        is DataState.Error -> {
                            isLoading = false
                        }
                        DataState.Loading -> {
                            isLoading = true
                        }
                        is DataState.Success -> {
                            serverCitiesList =
                                (it.asDataState() as DataState.Success<List<CityModel>>).data
                            downloadCitiesID = downloadedPrayTimesDAO.getCities()
                            citiesState = mutableListOf<CityItemState>().apply {
                                for (c in serverCitiesList) add(CityItemState().apply {
                                    if (downloadCitiesID.contains(c.id)) isDownloaded = true
                                })
                            }
                            isLoading = false
                        }
                    }
                }
            }
        }.onFailure { logException }
    }

    fun selectCity(cityName: String?) {
        runCatching {
            citiesState.forEach {
                if (it.isSelected) it.isSelected = false
            }
            val index = serverCitiesList.indexOf(serverCitiesList.find { it.name == cityName })
            citiesState[index].isSelected = true
        }.onFailure(logException)
    }

    fun download(city: CityModel, context: Context) {
        viewModelScope.launch {
            runCatching {
                val index = serverCitiesList.indexOf(city)
                citiesState[index].isDownloading = true
                prayTimesRepository.getPrayTimeFor(city.id).collect {
                    when (it.asDataState()) {
                        is DataState.Error -> {
                            citiesState[index].isDownloading = false
                        }
                        DataState.Loading -> {
                            citiesState[index].isDownloading = true
                        }
                        is DataState.Success -> {
                            downloadedPrayTimesDAO.clearDownloadFor(city.id)
                            downloadedPrayTimesDAO.insertToDownload(
                                modelToDBTimes(
                                    (it.asDataState() as DataState.Success<List<PrayTimesModel>>).data
                                )
                            )
                            downloadCitiesID = downloadedPrayTimesDAO.getCities()
                            context.appPrefs.edit {
                                putString(PREF_GEOCODED_CITYNAME, city.name)
                                putString(PREF_LATITUDE, city.latitude.toString())
                                putString(PREF_LONGITUDE, city.longitude.toString())
                                putString(PREF_SELECTED_LOCATION, "")
                            }
                            citiesState[index].isDownloaded = true
                            citiesState[index].isDownloading = false
                            selectCity(city.name)
                        }
                    }
                }
            }.onFailure(logException)
        }
    }

    fun search(query: String) {
        searchQuery = query
    }
}//end of class

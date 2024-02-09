package ir.namoo.religiousprayers.ui.intro

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.friendlyName
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.locationtracker.LocationResult
import ir.namoo.commons.locationtracker.LocationTracker
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.ProvinceModel
import ir.namoo.commons.repository.PrayTimeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IntroCustomLocationViewModel(
    private val locationTracker: LocationTracker,
    private val prayTimeRepository: PrayTimeRepository
) : ViewModel() {

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _city = MutableStateFlow("")
    val city = _city.asStateFlow()

    private val _latitude = MutableStateFlow("")
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow("")
    val longitude = _longitude.asStateFlow()

    private val _provinceList = MutableStateFlow(emptyList<ProvinceModel>())
    val provinceList = _provinceList.asStateFlow()

    private val _selectedProvince = MutableStateFlow<ProvinceModel?>(null)
    val selectedProvince = _selectedProvince.asStateFlow()

    private val _cityList = MutableStateFlow(emptyList<CityModel>())
    val cityList = selectedProvince.combine(_cityList) { province, cities ->
        if (province == null) emptyList()
        else
            cities.filter { it.provinceId == province.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _cityList.value)

    private val _selectedCity = MutableStateFlow<CityModel?>(null)
    val selectedCity = _selectedCity.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var localProvinceList =
                    prayTimeRepository.getLocalProvinceList().sortedBy { it.name }
                var localCityList = prayTimeRepository.getLocalCityList().sortedBy { it.name }
                if (localCityList.isEmpty() || localProvinceList.isEmpty()) {
                    localCityList = prayTimeRepository.updateAndGetCityList().sortedBy { it.name }
                    localProvinceList =
                        prayTimeRepository.getLocalProvinceList().sortedBy { it.name }
                    _provinceList.value = localProvinceList
                    _cityList.value = localCityList
                    _selectedProvince.value = localProvinceList.first()
                    _selectedCity.value = localCityList.first()
                } else {
                    _provinceList.value = localProvinceList
                    _cityList.value = localCityList
                    _selectedProvince.value = localProvinceList.first()
                    _selectedCity.value =
                        localCityList.filter { it.provinceId == localProvinceList.first().id }
                            .minBy { it.name }
                }
            }
        }
    }

    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            locationTracker.getCurrentLocation().collect {
                when (it) {
                    is LocationResult.Error -> {
                        _message.value = it.message
                        _isLoading.value = false
                    }

                    LocationResult.Loading -> {
                        _message.value = ""
                        _isLoading.value = true
                    }

                    is LocationResult.Success -> {
                        _location.value = it.location
                        runCatching {
                            location.value?.let { l ->
                                _latitude.value = l.latitude.toString()
                                _longitude.value = l.longitude.toString()

                                val geocoder = Geocoder(context, language.value.asSystemLocale())

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) geocoder.getFromLocation(
                                    l.latitude,
                                    l.longitude,
                                    1
                                ) { address ->
                                    _city.value = address.first()?.friendlyName ?: ""
                                }
                                else {
                                    @Suppress("DEPRECATION")
                                    val address =
                                        geocoder.getFromLocation(l.latitude, l.longitude, 1)
                                    _city.value = address?.first()?.friendlyName ?: ""
                                }
                            }
                        }.onFailure(logException)
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun saveAndContinue(context: Context, startMainActivity: () -> Unit) {
        viewModelScope.launch {
            context.appPrefs.edit {
                putString(PREF_GEOCODED_CITYNAME, city.value)
                putString(PREF_LATITUDE, latitude.value)
                putString(PREF_LONGITUDE, longitude.value)
                putString(PREF_SELECTED_LOCATION, "")
                putBoolean(PREF_FIRST_START, false)
            }
            startMainActivity()
        }
    }

    fun updateCity(name: String) {
        viewModelScope.launch {
            _city.value = name
        }
    }

    private fun updateLatitude(latitude: String) {
        viewModelScope.launch {
            _latitude.value = latitude
        }
    }

    private fun updateLongitude(longitude: String) {
        viewModelScope.launch {
            _longitude.value = longitude
        }
    }

    fun updateSelectedProvince(provinceModel: ProvinceModel?) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedProvince.value = provinceModel
            provinceModel?.let { p ->
                _selectedCity.value = _cityList.value.filter { it.provinceId == p.id }
                    .minBy { it.name }
            }
            _isLoading.value = false
        }
    }

    fun updateSelectedCity(cityModel: CityModel?) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCity.value = cityModel
            cityModel?.let {
                updateLatitude(it.latitude.toString())
                updateLongitude(it.longitude.toString())
                updateCity(it.name)
            }
            _isLoading.value = false
        }
    }
}

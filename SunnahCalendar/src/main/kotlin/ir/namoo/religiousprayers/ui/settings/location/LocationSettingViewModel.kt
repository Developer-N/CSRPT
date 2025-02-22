package ir.namoo.religiousprayers.ui.settings.location

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
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.utils.friendlyName
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import ir.namoo.commons.locationtracker.LocationResult
import ir.namoo.commons.locationtracker.LocationTracker
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.model.ProvinceModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationSettingViewModel(
    private val locationRepository: LocationsDB,
    private val locationTracker: LocationTracker,
) : ViewModel() {

    private val _provinceList = MutableStateFlow(emptyList<ProvinceModel>())
    val provinceList = _provinceList.asStateFlow()

    private val _selectedProvince = MutableStateFlow<ProvinceModel?>(null)
    val selectedProvince = _selectedProvince.asStateFlow()

    private val _cityList = MutableStateFlow(emptyList<CityModel>())
    val cityList = selectedProvince.combine(_cityList) { province, cities ->
        if (province == null) emptyList()
        else cities.filter { it.provinceId == province.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _cityList.value)

    private val _selectedCity = MutableStateFlow<CityModel?>(null)
    val selectedCity = _selectedCity.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _cityName = MutableStateFlow("")
    val cityName = _cityName.asStateFlow()

    private val _latitude = MutableStateFlow("")
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow("")
    val longitude = _longitude.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    fun loadData(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _cityName.value = context.preferences.getString(PREF_GEOCODED_CITYNAME, "") ?: ""
            _latitude.value = context.preferences.getString(PREF_LATITUDE, "0.0") ?: "0.0"
            _longitude.value = context.preferences.getString(PREF_LONGITUDE, "0.0") ?: "0.0"
            val allCities = locationRepository.cityDAO().getAllCity()
            _provinceList.value = locationRepository.provinceDAO().getAllProvinces()
            _selectedProvince.value =
                provinceList.value.find { p -> allCities.find { c -> c.name == cityName.value }?.provinceId == p.id }
            _cityList.value = locationRepository.cityDAO().getAllCity()
            _selectedCity.value = cityList.value.find { it.name == cityName.value }

            _isLoading.value = false
        }
    }

    fun updateCityInfo(cityName: String, latitude: String, longitude: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _cityName.value = cityName
            _latitude.value = latitude
            _longitude.value = longitude
            _isLoading.value = false
        }
    }

    fun saveCityInfo(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            context.preferences.edit {
                putString(PREF_GEOCODED_CITYNAME, cityName.value)
                putString(PREF_LATITUDE, latitude.value)
                putString(PREF_LONGITUDE, longitude.value)
            }
            _isLoading.value = false
        }
    }

    fun updateSelectedProvince(province: ProvinceModel?) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedProvince.value = province
            province?.let { p ->
                updateSelectedCity(_cityList.value.filter { it.provinceId == p.id }
                    .minBy { it.name })
            }
            _isLoading.value = false
        }
    }

    fun updateSelectedCity(city: CityModel?) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCity.value = city
            city?.let {
                updateCityInfo(it.name, it.latitude.toString(), it.longitude.toString())
            }
            _isLoading.value = false
        }
    }

    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            locationTracker.getCurrentLocation().collect {
                when (it) {
                    is LocationResult.Error -> {
                        _isLoading.value = false
                    }

                    LocationResult.Loading -> {
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
                                    l.latitude, l.longitude, 1
                                ) { address ->
                                    _cityName.value = address.first()?.friendlyName ?: ""
                                }
                                else {
                                    @Suppress("DEPRECATION") val address =
                                        geocoder.getFromLocation(l.latitude, l.longitude, 1)
                                    _cityName.value = address?.first()?.friendlyName ?: ""
                                }
                            }
                        }.onFailure(logException)
                        _isLoading.value = false
                    }
                }
            }
        }
    }

}//end of class LocationSettingViewModel

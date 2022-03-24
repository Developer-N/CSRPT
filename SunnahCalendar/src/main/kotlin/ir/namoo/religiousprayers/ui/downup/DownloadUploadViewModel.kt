package ir.namoo.religiousprayers.ui.downup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.PrayTimesModel
import ir.namoo.commons.service.PrayTimesService
import ir.namoo.commons.utils.modelToDBTimes
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDAO
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDAO
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadUploadViewModel @Inject constructor(
    private val prayTimesService: PrayTimesService,
    private val downloadedPrayTimesDAO: DownloadedPrayTimesDAO,
    private val prayTimesDAO: PrayTimesDAO
) : ViewModel() {

    private val _addedCities = MutableLiveData<List<CityModel>>()
    val addedCities: LiveData<List<CityModel>> get() = _addedCities

    private val _downloadedCities = MutableLiveData<List<Int>>()
    val downloaded: LiveData<List<Int>> get() = _downloadedCities

    val allEdited = MutableLiveData<List<EditedPrayTimesEntity>>()


    fun loadAddedCities() {
        viewModelScope.launch {
            _downloadedCities.value = downloadedPrayTimesDAO.getCities()
            _addedCities.value = prayTimesService.getAddedCities()
            allEdited.value = prayTimesDAO.getAllEdited()
        }
    }

    fun saveToDatabase(prayTimesModel: List<PrayTimesModel>) {
        viewModelScope.launch {
            downloadedPrayTimesDAO.clearDownloadFor(prayTimesModel.first().cityID)
            downloadedPrayTimesDAO.insertToDownload(modelToDBTimes(prayTimesModel))
            _downloadedCities.value = downloadedPrayTimesDAO.getCities()
        }
    }

}//end of class

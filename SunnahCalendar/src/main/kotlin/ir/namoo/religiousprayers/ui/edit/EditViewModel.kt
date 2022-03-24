package ir.namoo.religiousprayers.ui.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDB
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(private val prayTimesDB: PrayTimesDB) : ViewModel() {

    private val _timeList = MutableLiveData<MutableList<EditedPrayTimesEntity>>()
    val timeList: MutableLiveData<MutableList<EditedPrayTimesEntity>> get() = _timeList

    init {
        viewModelScope.launch {
            _timeList.value = prayTimesDB.prayTimes().getAllEdited()
        }
    }

    val dayNum: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = 1
        }
    }
}

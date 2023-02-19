package ir.namoo.religiousprayers.ui.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDB
import kotlinx.coroutines.launch

class EditViewModel constructor(private val prayTimesDB: PrayTimesDB) : ViewModel() {

    private val _timeList = MutableLiveData<MutableList<EditedPrayTimesEntity>>()
    val timeList: MutableLiveData<MutableList<EditedPrayTimesEntity>> get() = _timeList

    init {
        viewModelScope.launch {
            _timeList.value = prayTimesDB.prayTimes().getAllEdited()
            prayTimesDB.close()
        }
    }

    val dayNum: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = 1
        }
    }
}

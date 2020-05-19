package ir.namoo.religiousprayers.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ir.namoo.religiousprayers.praytimes.EditedPrayTimesEntity
import ir.namoo.religiousprayers.praytimes.PrayTimesDB

class EditViewModel(application: Application) : AndroidViewModel(application) {
    private val timeList: MutableLiveData<MutableList<EditedPrayTimesEntity>> by lazy {
        MutableLiveData<MutableList<EditedPrayTimesEntity>>().also {
            it.value =
                PrayTimesDB.getInstance(application.applicationContext).prayTimes().getAllEdited()
        }
    }

    fun getTimes(): MutableLiveData<MutableList<EditedPrayTimesEntity>> {
        return timeList
    }

    private val dayNum: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().also {
            it.value = 1
        }
    }

    fun getDay(): MutableLiveData<Int> {
        return dayNum
    }
}
package ir.namoo.religiousprayers.ui.settings.athan

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.settings.widgetnotification.WidgetConfigurationActivity
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.utils.update
import ir.namoo.commons.DEFAULT_JUMMA_SILENT_MINUTE
import ir.namoo.commons.PREF_JUMMA_SILENT
import ir.namoo.commons.PREF_JUMMA_SILENT_MINUTE
import ir.namoo.commons.PREF_SHOW_SYSTEM_RINGTONES
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getDefaultAlertUri
import ir.namoo.commons.utils.getDefaultAthanUri
import ir.namoo.commons.utils.getDefaultFajrAthanUri
import ir.namoo.commons.utils.getFileNameFromLink
import ir.namoo.religiousprayers.praytimeprovider.getAllAvailableAthans
import ir.namoo.religiousprayers.praytimeprovider.getAthanUriFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AthanSettingsViewModel(
    private val athanSettingsDB: AthanSettingsDB, private val athanDB: AthanDB
) : ViewModel() {
    private val mediaPlayer = MediaPlayer()
    private var athanId: Int = 0
    private lateinit var athanSettings: AthanSetting
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _athanState = MutableStateFlow(false)
    val athanState = _athanState.asStateFlow()

    private val _playType = MutableStateFlow(0)
    val playType = _playType.asStateFlow()

    private val _playDoa = MutableStateFlow(false)
    val playDoa = _playDoa.asStateFlow()

    private val _enableBefore = MutableStateFlow(false)
    val enableBefore = _enableBefore.asStateFlow()

    private val _beforeMinute = MutableStateFlow(10)
    val beforeMinute = _beforeMinute.asStateFlow()

    private val _enableAfter = MutableStateFlow(false)
    val enableAfter = _enableAfter.asStateFlow()

    private val _afterMinute = MutableStateFlow(10)
    val afterMinute = _afterMinute.asStateFlow()

    private val _enableSilent = MutableStateFlow(false)
    val enableSilent = _enableSilent.asStateFlow()

    private val _silentMinute = MutableStateFlow(10)
    val silentMinute = _silentMinute.asStateFlow()

    private val _enableJummaSilent = MutableStateFlow(false)
    val enableJummaSilent = _enableJummaSilent.asStateFlow()

    private val _silentJummaMinute = MutableStateFlow(40)
    val silentJummaMinute = _silentJummaMinute.asStateFlow()

    private val _ascendingVolume = MutableStateFlow(false)
    val ascendingVolume = _ascendingVolume.asStateFlow()

    private val _volume = MutableStateFlow(0)
    val volume = _volume.asStateFlow()

    private val _systemRingtoneNames = MutableStateFlow(listOf<String>())
    private val systemRingtoneNames = _systemRingtoneNames.asStateFlow()

    private val _athanNames = MutableStateFlow(listOf<String>())
    val athanNames = _athanNames.asStateFlow()

    private val _selectedAthan = MutableStateFlow("")
    val selectedAthan = _selectedAthan.asStateFlow()

    private val _systemRingtoneUris = MutableStateFlow(listOf<Uri>())
    private val systemRingtoneUris = _systemRingtoneUris.asStateFlow()

    private val _selectedAlarm = MutableStateFlow("")
    val selectedAlarm = _selectedAlarm.asStateFlow()

    private val _alarmNames = MutableStateFlow(listOf<String>())
    val alarmNames = _alarmNames.asStateFlow()

    private val _isAthanPlaying = MutableStateFlow(false)
    val isAthanPlaying = _isAthanPlaying.asStateFlow()

    private val _isAlarmPlaying = MutableStateFlow(false)
    val isAlarmPlaying = _isAlarmPlaying.asStateFlow()

    private val _selectedBackground = MutableStateFlow<String?>(null)
    val selectedBackground = _selectedBackground.asStateFlow()

    fun loadData(context: Context, id: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            athanId = id
            athanSettings = athanSettingsDB.athanSettingsDAO().getSetting(id)
            _athanState.value = athanSettings.state
            _playType.value = athanSettings.playType
            _playDoa.value = athanSettings.playDoa
            _enableBefore.value = athanSettings.isBeforeEnabled
            _beforeMinute.value = athanSettings.beforeAlertMinute
            _enableAfter.value = athanSettings.isAfterEnabled
            _afterMinute.value = athanSettings.afterAlertMinute
            _enableSilent.value = athanSettings.isSilentEnabled
            _silentMinute.value = athanSettings.silentMinute
            _ascendingVolume.value = athanSettings.isAscending
            _volume.value = athanSettings.athanVolume
            _selectedBackground.value = athanSettings.backgroundUri

            _enableJummaSilent.value = context.preferences.getBoolean(PREF_JUMMA_SILENT, false)
            _silentJummaMinute.value =
                context.preferences.getInt(PREF_JUMMA_SILENT_MINUTE, DEFAULT_JUMMA_SILENT_MINUTE)

            val cursor = RingtoneManager(context).cursor
            val tmpNames = mutableListOf<String>()
            val tmpUris = mutableListOf<Uri>()
            while (cursor.moveToNext()) {
                tmpNames.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX))
                tmpUris.add(
                    "${cursor.getString(RingtoneManager.URI_COLUMN_INDEX)}/${
                        cursor.getString(
                            RingtoneManager.ID_COLUMN_INDEX
                        )
                    }".toUri()
                )
            }
            _systemRingtoneNames.value = tmpNames
            _systemRingtoneUris.value = tmpUris

            _selectedAthan.value =
                if (athanId == 1) context.getString(R.string.default_fajr_athan_name)
                else context.getString(R.string.default_athan_name)
            if (athanSettings.athanURI.isNotEmpty()) {
                _selectedAthan.value =
                    if (athanSettings.athanURI.startsWith("content")) systemRingtoneNames.value[systemRingtoneUris.value.indexOf(
                        athanSettings.athanURI.toUri()
                    )]
                    else when (athanSettings.athanURI) {
                        "" -> if (athanSettings.athanKey == "FAJR") context.getString(R.string.default_fajr_athan_name)
                        else context.getString(R.string.default_athan_name)

                        else -> {
                            val name = getFileNameFromLink(athanSettings.athanURI)
                            val all = athanDB.athanDAO().getAllAthans()
                            var n = ""
                            for (a in all) if (a.link.contains(name)) n = a.name
                            n
                        }
                    }
            }
            _athanNames.value =
                if (context.preferences.getBoolean(PREF_SHOW_SYSTEM_RINGTONES, false))
                    (getNames(if (athanId == 1) 1 else 0, context) + systemRingtoneNames.value)
                        .toList()
                else
                    getNames(if (athanId == 1) 1 else 0, context).toList()
            _selectedAlarm.value = context.getString(R.string.default_alert_before_name)
            if (athanSettings.alertURI.isNotEmpty()) {
                _selectedAlarm.value =
                    if (athanSettings.alertURI.startsWith("content")) systemRingtoneNames.value[systemRingtoneUris.value.indexOf(
                        athanSettings.alertURI.toUri()
                    )]
                    else when (athanSettings.alertURI) {
                        "" -> context.getString(R.string.default_alert_before_name)
                        else -> {
                            val name = getFileNameFromLink(athanSettings.alertURI)
                            val all = athanDB.athanDAO().getAllAthans()
                            var n = ""
                            for (a in all) if (a.link.contains(name)) n = a.name
                            n
                        }
                    }
            }
            _alarmNames.value =
                if (context.preferences.getBoolean(PREF_SHOW_SYSTEM_RINGTONES, false))
                    (getNames(2, context) + systemRingtoneNames.value).toList()
                else
                    getNames(2, context).toList()

            _isLoading.value = false
        }
    }

    fun updateAthanState(context: Context) {
        viewModelScope.launch {
            athanSettings.apply { state = !state }
            _athanState.value = athanSettings.state
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            update(context, true)
        }
    }

    fun updatePlayType(newPlayType: Int) {
        viewModelScope.launch {
            athanSettings.playType = newPlayType
            _playType.value = newPlayType
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
        }
    }

    fun updatePlayDoa() {
        viewModelScope.launch {
            athanSettings.apply { playDoa = !playDoa }
            _playDoa.value = athanSettings.playDoa
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
        }
    }

    fun updateBeforeState(context: Context) {
        viewModelScope.launch {
            athanSettings.apply { isBeforeEnabled = !isBeforeEnabled }
            _enableBefore.value = athanSettings.isBeforeEnabled
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            update(context, true)
        }
    }

    fun updateBeforeMinutes(context: Context, minutes: Int) {
        viewModelScope.launch {
            athanSettings.beforeAlertMinute = minutes
            _beforeMinute.value = minutes
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            update(context, true)
        }
    }

    fun updateAfterState(context: Context) {
        viewModelScope.launch {
            athanSettings.apply { isAfterEnabled = !isAfterEnabled }
            _enableAfter.value = athanSettings.isAfterEnabled
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            update(context, true)
        }
    }


    fun updateAfterMinutes(context: Context, minutes: Int) {
        viewModelScope.launch {
            athanSettings.afterAlertMinute = minutes
            _afterMinute.value = minutes
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            update(context, true)
        }
    }

    fun updateSilentState(context: Context) {
        viewModelScope.launch {
            athanSettings.apply { isSilentEnabled = !isSilentEnabled }
            _enableSilent.value = athanSettings.isSilentEnabled
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            update(context, true)
        }
    }

    fun updateSilentMinutes(context: Context, minutes: Int) {
        viewModelScope.launch {
            athanSettings.silentMinute = minutes
            _silentMinute.value = minutes
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            update(context, true)
        }
    }

    fun updateJummaSilentState(context: Context) {
        viewModelScope.launch {
            context.preferences.edit {
                putBoolean(PREF_JUMMA_SILENT, !enableJummaSilent.value)
            }
            _enableJummaSilent.value = !enableJummaSilent.value
            update(context, true)
        }
    }

    fun updateJummaSilentMinute(context: Context, minutes: Int) {
        viewModelScope.launch {
            context.preferences.edit {
                putInt(PREF_JUMMA_SILENT_MINUTE, minutes)
            }
            _silentJummaMinute.value = minutes
            update(context, true)
        }
    }

    fun updateAscendingState() {
        viewModelScope.launch {
            athanSettings.apply { isAscending = !isAscending }
            _ascendingVolume.value = athanSettings.isAscending
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
        }
    }

    fun updateVolume(volume: Int) {
        viewModelScope.launch {
            athanSettings.athanVolume = volume
            _volume.value = volume
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
        }
    }

    fun updateSelectedAthan(context: Context, selectedAthan: String) {
        viewModelScope.launch {
            val position = athanNames.value.indexOf(selectedAthan)
            if (position == 0) {
                athanSettings.athanURI = ""
                _selectedAthan.value =
                    if (athanId == 1) context.getString(R.string.default_fajr_athan_name)
                    else context.getString(R.string.default_athan_name)
            } else {
                val selectedName = athanNames.value[position]
                if (systemRingtoneNames.value.contains(selectedName)) {
                    val athanUri =
                        systemRingtoneUris.value[systemRingtoneNames.value.indexOf(selectedName)]
                    athanSettings.athanURI = athanUri.toString()
                    _selectedAthan.value = selectedName
                } else for (d in athanDB.athanDAO()
                    .getAllAthans()) if (selectedName == d.name) {
                    val athanUri = getAthanUriFor(context, getFileNameFromLink(d.link))
                    if (athanUri != null) {
                        athanSettings.athanURI = athanUri.toString()
                        _selectedAthan.value = selectedName
                    }
                }
            }
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
        }
    }

    fun updateSelectedAlarm(context: Context, selectedAlarm: String) {
        viewModelScope.launch {
            val position = alarmNames.value.indexOf(selectedAlarm)
            if (position == 0) {
                athanSettings.alertURI = ""
                _selectedAlarm.value = context.getString(R.string.default_alert_before_name)
            } else {
                val selectedName = alarmNames.value[position]
                if (systemRingtoneNames.value.contains(selectedName)) {
                    val alertUri =
                        systemRingtoneUris.value[systemRingtoneNames.value.indexOf(selectedName)]
                    athanSettings.alertURI = alertUri.toString()
                    _selectedAlarm.value = selectedName
                } else for (d in athanDB.athanDAO()
                    .getAllAthans()) if (selectedName == d.name) {
                    val alertUri = getAthanUriFor(context, getFileNameFromLink(d.link))
                    if (alertUri != null) {
                        athanSettings.alertURI = alertUri.toString()
                        _selectedAlarm.value = selectedName
                    }
                }
            }
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
        }
    }

    private suspend fun getNames(type: Int, context: Context): Array<String> {
        val athanNames = arrayListOf<String>()
        athanNames.add(
            when (type) {
                0 -> context.getString(R.string.default_athan_name)
                1 -> context.getString(R.string.default_fajr_athan_name)
                else -> context.getString(R.string.default_alert_before_name)
            }
        )

        val existsFiles = getAllAvailableAthans(context)
        val athansInDB = athanDB.athanDAO().getAllAthans()
        for (file in existsFiles) {
            val fileName = getFileNameFromLink(file.absolutePath)
            for (athan in athansInDB)
                if (athan.link.split("/").last() == fileName)
                    if (((type == 0 || type == 1) && athan.type == 1) || (type == 2 && athan.type == 2))
                        athanNames.add(athan.name)
        }

        val res = Array(athanNames.size, init = { i -> "$i" })
        athanNames.toArray(res)
        return res
    }

    fun playAthan(context: Context,activity: Activity?) {
        viewModelScope.launch {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
            if (isAthanPlaying.value) {
                _isAthanPlaying.value = false
            } else {
                _isAthanPlaying.value = true
                _isAlarmPlaying.value = false

                val uri = if (athanSettings.athanURI == "") if (athanSettings.athanKey == "FAJR")
                    getDefaultFajrAthanUri(context)
                else getDefaultAthanUri(context)
                else athanSettings.athanURI.toUri()
                mediaPlayer.apply {
                    runCatching {
                        reset()
                        setDataSource(context, uri)
                        setAudioAttributes(
                            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                        )
                        activity?.volumeControlStream = AudioManager.STREAM_ALARM
                        setOnCompletionListener { _isAthanPlaying.value = false }
                        prepare()
                        start()
                    }.onFailure(logException)
                }
            }
        }
    }

    fun playAlarm(context: Context,activity: Activity?) {
        viewModelScope.launch {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
            if (isAlarmPlaying.value) {
                _isAlarmPlaying.value = false
            } else {
                _isAlarmPlaying.value = true
                _isAthanPlaying.value = false

                val uri = if (athanSettings.alertURI == "")
                    getDefaultAlertUri(context)
                else athanSettings.alertURI.toUri()
                mediaPlayer.apply {
                    runCatching {
                        reset()
                        setDataSource(context, uri)
                        setAudioAttributes(
                            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                        )
                        activity?.volumeControlStream = AudioManager.STREAM_ALARM
                        setOnCompletionListener { _isAlarmPlaying.value = false }
                        prepare()
                        start()
                    }.onFailure(logException)
                }
            }
        }
    }

    fun updateSelectedBackground(newBackground: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            athanSettings.backgroundUri = newBackground
            _selectedBackground.value = newBackground
            athanSettingsDB.athanSettingsDAO().update(athanSettings)
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}//end of class AthanSettingsViewModel


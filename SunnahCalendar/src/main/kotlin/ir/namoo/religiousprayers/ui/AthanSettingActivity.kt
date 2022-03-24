package ir.namoo.religiousprayers.ui

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.NumberPicker
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAthanSettingBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.scheduleAlarms
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.commons.ATHAN_ID
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.utils.getDefaultAthanUri
import ir.namoo.commons.utils.getDefaultBeforeAlertUri
import ir.namoo.commons.utils.getDefaultFajrAthanUri
import ir.namoo.commons.utils.getFileNameFromLink
import ir.namoo.religiousprayers.praytimeprovider.getAllAvailableAthans
import ir.namoo.religiousprayers.praytimeprovider.getAthanUriFor
import ir.namoo.religiousprayers.ui.shared.ShapedAdapter
import javax.inject.Inject


@AndroidEntryPoint
class AthanSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAthanSettingBinding

    @Inject
    lateinit var athanSettingsDB: AthanSettingsDB

    @Inject
    lateinit var athanDB: AthanDB
    private lateinit var athanSetting: AthanSetting
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var audioManager: AudioManager? = null
    private var systemRingtoneNames = arrayListOf<String>()
    private var systemRingtoneUris = arrayListOf<Uri>()


    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resolveColor(android.R.attr.colorPrimaryDark)
        }

        val athanId = intent.extras?.getInt(ATHAN_ID) ?: return
        athanSetting = athanSettingsDB.athanSettingsDAO().getSetting(athanId)
        binding = ActivityAthanSettingBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        audioManager = getSystemService()
        audioManager?.getStreamVolume(AudioManager.STREAM_ALARM)

        val manager = RingtoneManager(this)
        val cursor = manager.cursor
        while (cursor.moveToNext()) {
            systemRingtoneNames.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX))
            systemRingtoneUris.add(
                "${cursor.getString(RingtoneManager.URI_COLUMN_INDEX)}/${
                    cursor.getString(
                        RingtoneManager.ID_COLUMN_INDEX
                    )
                }".toUri()
            )
        }

        initBinding()

        binding.btnAthanSettingClose.setOnClickListener { onBackPressed() }

    }//end of onCreate

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        scheduleAlarms(this)
    }

    private fun initBinding() {
        when (athanSetting.athanKey) {
            "FAJR" -> {
                binding.itemAthanSettingTitle.text = getString(R.string.fajr)
            }
            "SUNRISE" -> {
                binding.itemAthanSettingTitle.text = getString(R.string.sunrise)
                binding.switchDoa.visibility = View.GONE
                binding.beforeLayout.visibility = View.GONE
                binding.txtSelectAthanTitle.visibility = View.GONE
                binding.selectAthanLayout.visibility = View.GONE
                binding.btnAthanPlay.visibility = View.GONE
                binding.btnAthanRest.visibility = View.GONE
            }
            "DHUHR" -> {
                binding.itemAthanSettingTitle.text = getString(R.string.dhuhr)
            }
            "ASR" -> {
                binding.itemAthanSettingTitle.text = getString(R.string.asr)
            }
            "MAGHRIB" -> {
                binding.itemAthanSettingTitle.text = getString(R.string.maghrib)
            }
            "ISHA" -> {
                binding.itemAthanSettingTitle.text = getString(R.string.isha)
            }
        }

        //######################################## STATE
        binding.itemAthanSettingState.isChecked = athanSetting.state
        binding.itemAthanSettingState.setOnClickListener {
            athanSetting.state = binding.itemAthanSettingState.isChecked
            athanSettingsDB.athanSettingsDAO().update(athanSetting)
        }

        //######################################## AlertType
        binding.itemAthanSettingAlertType.check(
            when (athanSetting.playType) {
                0 -> R.id.item_athan_setting_fullscreen
                1 -> R.id.item_athan_setting_notification
                else -> R.id.item_athan_setting_just_notification
            }
        )
        binding.itemAthanSettingAlertType.setOnCheckedChangeListener { _, checkedId ->
            val type =
                when (checkedId) {
                    R.id.item_athan_setting_fullscreen -> 0
                    R.id.item_athan_setting_notification -> 1
                    else -> 2
                }
            athanSetting.playType = type
            athanSettingsDB.athanSettingsDAO().update(athanSetting)
        }

        //######################################## PlayDOA
        binding.switchDoa.isChecked = athanSetting.playDoa
        binding.switchDoa.setOnClickListener {
            athanSetting.playDoa = binding.switchDoa.isChecked
            athanSettingsDB.athanSettingsDAO().update(athanSetting)
        }

        //######################################## Before
        binding.switchAlarmBefore.isChecked = athanSetting.isBeforeEnabled
        binding.switchAlarmBefore.setOnClickListener {
            athanSetting.isBeforeEnabled = binding.switchAlarmBefore.isChecked
            athanSettingsDB.athanSettingsDAO().update(athanSetting)
        }
        //######################################## Before minute
        binding.numberPickerAlarmBefore.minValue = 5
        if (athanSetting.athanKey == "FAJR")
            binding.numberPickerAlarmBefore.maxValue = 90
        else
            binding.numberPickerAlarmBefore.maxValue = 60
        binding.numberPickerAlarmBefore.value =
            if (athanSetting.beforeAlertMinute >= binding.numberPickerAlarmBefore.minValue && athanSetting.beforeAlertMinute <= binding.numberPickerAlarmBefore.maxValue) athanSetting.beforeAlertMinute else 10
        binding.numberPickerAlarmBefore.setOnValueChangedListener { numberPicker: NumberPicker, _: Int, _: Int ->
            athanSetting.beforeAlertMinute = numberPicker.value
            athanSettingsDB.athanSettingsDAO().update(athanSetting)
        }
        //######################################## isAscending
        binding.switchAscendingAthanVolume.isChecked = athanSetting.isAscending
        binding.switchAscendingAthanVolume.setOnClickListener {
            it as SwitchMaterial
            athanSetting.isAscending = it.isChecked
            athanSettingsDB.athanSettingsDAO().update(athanSetting)
            binding.seekBarAthanVolume.isEnabled = !it.isChecked
        }
        //######################################## AthanVolume
        binding.seekBarAthanVolume.isEnabled =
            !binding.switchAscendingAthanVolume.isChecked
        binding.seekBarAthanVolume.apply {
            max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7
            progress = athanSetting.athanVolume
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    audioManager?.setStreamVolume(
                        AudioManager.STREAM_ALARM,
                        progress,
                        0
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    athanSetting.athanVolume = progress
                    athanSettingsDB.athanSettingsDAO().update(athanSetting)
                }
            })
        }
        //######################################## END AthanVolume
        //######################################## Select Athan
        var selectedAthan =
            if (athanSetting.athanKey == "FAJR")
                getString(R.string.default_fajr_athan_name)
            else
                getString(R.string.default_athan_name)
        if (athanSetting.athanURI.isNotEmpty()) {
            selectedAthan = if (athanSetting.athanURI.startsWith("content"))
                systemRingtoneNames[systemRingtoneUris.indexOf(athanSetting.athanURI.toUri())]
            else when (athanSetting.athanURI) {
                "" -> if (athanSetting.athanKey == "FAJR")
                    getString(R.string.default_fajr_athan_name)
                else
                    getString(R.string.default_athan_name)
                else -> {
                    val name = getFileNameFromLink(athanSetting.athanURI)
                    val all = athanDB.athanDAO().getAllAthans()
                    var n = ""
                    for (a in all)
                        if (a.link.contains(name))
                            n = a.name
                    n
                }
            }
        }
        val athanNames =
            getNames(if (athanSetting.athanKey == "FAJR") 1 else 0) + systemRingtoneNames
        binding.spinnerAthanName.adapter = ShapedAdapter(
            this, R.layout.select_dialog_item, R.id.text1, athanNames
        )
        binding.spinnerAthanName.setSelection(athanNames.indexOf(selectedAthan))
        binding.spinnerAthanName.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    athanSetting.athanURI = ""
                    binding.txtSelectedAthanName.text =
                        if (athanSetting.athanKey == "FAJR")
                            getString(R.string.default_fajr_athan_name)
                        else
                            getString(R.string.default_athan_name)
                } else {
                    val selectedName = athanNames[position]
                    if (systemRingtoneNames.contains(selectedName)) {
                        val athanUri =
                            systemRingtoneUris[systemRingtoneNames.indexOf(selectedName)]
                        athanSetting.athanURI = athanUri.toString()
                        binding.txtSelectedAthanName.text = selectedName
                    } else for (d in athanDB.athanDAO()
                        .getAllAthans())
                        if (selectedName == d.name) {
                            val athanUri =
                                getAthanUriFor(
                                    this@AthanSettingActivity,
                                    getFileNameFromLink(d.link)
                                )
                            if (athanUri != null) {
                                athanSetting.athanURI = athanUri.toString()
                                binding.txtSelectedAthanName.text = selectedName
                            }
                        }
                }
                athanSettingsDB.athanSettingsDAO().update(athanSetting)
            }
        }
        //############################################## Alert
        var selectedAlert = getString(R.string.default_alert_before_name)
        if (athanSetting.alertURI.isNotEmpty()) {
            selectedAlert = if (athanSetting.alertURI.startsWith("content"))
                systemRingtoneNames[systemRingtoneUris.indexOf(athanSetting.alertURI.toUri())]
            else when (athanSetting.alertURI) {
                "" -> resources.getString(R.string.default_alert_before_name)
                else -> {
                    val name = getFileNameFromLink(athanSetting.alertURI)
                    val all = athanDB.athanDAO().getAllAthans()
                    var n = ""
                    for (a in all)
                        if (a.link.contains(name))
                            n = a.name
                    n
                }
            }
        }
        val alarmNames = getNames(2) + systemRingtoneNames
        binding.spinnerAlarmName.adapter = ShapedAdapter(
            this, R.layout.select_dialog_item, R.id.text1, alarmNames
        )
        binding.spinnerAlarmName.setSelection(alarmNames.indexOf(selectedAlert))
        binding.spinnerAlarmName.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    athanSetting.alertURI = ""
                    binding.txtSelectedAlarmName.text =
                        resources.getString(R.string.default_alert_before_name)
                } else {
                    val selectedName = alarmNames[position]
                    if (systemRingtoneNames.contains(selectedName)) {
                        val alertUri =
                            systemRingtoneUris[systemRingtoneNames.indexOf(selectedName)]
                        athanSetting.alertURI = alertUri.toString()
                        binding.txtSelectedAlarmName.text = selectedName
                    } else for (d in athanDB.athanDAO().getAllAthans())
                        if (selectedName == d.name) {
                            val alertUri = getAthanUriFor(
                                this@AthanSettingActivity,
                                getFileNameFromLink(d.link)
                            )
                            if (alertUri != null) {
                                athanSetting.alertURI = alertUri.toString()
                                binding.txtSelectedAlarmName.text = selectedName
                            }
                        }
                }
                athanSettingsDB.athanSettingsDAO().update(athanSetting)
            }
        }

        //########################################### play buttons
        binding.btnAthanPlay.setOnClickListener {
            it as Chip
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            audioManager?.setStreamVolume(
                AudioManager.STREAM_ALARM,
                athanSetting.athanVolume,
                0
            )
            if (binding.btnAlertPlay.text == getString(R.string.stop))
                stopAlert()
            if (!mediaPlayer.isPlaying)
                playAthan(
                    if (athanSetting.athanURI == "")
                        if (athanSetting.athanKey == "FAJR")
                            getDefaultFajrAthanUri(this)
                        else
                            getDefaultAthanUri(this)
                    else
                        athanSetting.athanURI.toUri()
                )
            else
                stopAthan()
        }

        binding.btnAlertPlay.setOnClickListener {
            it as Chip
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            audioManager?.setStreamVolume(
                AudioManager.STREAM_ALARM,
                athanSetting.athanVolume,
                0
            )
            if (binding.btnAthanPlay.text == getString(R.string.stop))
                stopAthan()
            if (!mediaPlayer.isPlaying)
                playAlert(
                    if (athanSetting.alertURI == "")
                        getDefaultBeforeAlertUri(this)
                    else
                        athanSetting.alertURI.toUri()
                )
            else
                stopAlert()
        }

        //########################################### reset button

        binding.btnAthanRest.setOnClickListener {
            athanSetting.alertURI = ""
            athanSetting.athanURI = ""
            athanSetting.athanVolume = 1
            athanSetting.isAscending = false
            athanSetting.beforeAlertMinute = 10
            athanSetting.isBeforeEnabled = false
            athanSetting.playDoa = false
            athanSetting.playType = 0
            athanSetting.state = false
            athanSettingsDB.athanSettingsDAO().update(athanSetting)
        }
    }

    private fun playAthan(uri: Uri) {
        runCatching {
            binding.btnAthanPlay.setChipIconResource(R.drawable.ic_stop)
            binding.btnAthanPlay.text = resources.getString(R.string.stop)
            mediaPlayer.apply {
                runCatching {
                    reset()
                    setDataSource(this@AthanSettingActivity, uri)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        setAudioStreamType(AudioManager.STREAM_ALARM)
                    }
                    volumeControlStream = AudioManager.STREAM_ALARM
                    setOnCompletionListener { stopAthan() }
                    prepare()
                    start()
                }.onFailure(logException)
            }
        }.onFailure(logException)
    }//end of play

    private fun stopAthan() {
        runCatching {
            binding.btnAthanPlay.setChipIconResource(R.drawable.ic_play)
            binding.btnAthanPlay.text = resources.getString(R.string.play_athan)
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
        }.onFailure(logException)
    }//end of stop

    private fun playAlert(uri: Uri) {
        runCatching {
            binding.btnAlertPlay.setChipIconResource(R.drawable.ic_stop)
            binding.btnAlertPlay.text = resources.getString(R.string.stop)
            mediaPlayer.apply {
                runCatching {
                    reset()
                    setDataSource(this@AthanSettingActivity, uri)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        setAudioStreamType(AudioManager.STREAM_ALARM)
                    }
                    volumeControlStream = AudioManager.STREAM_ALARM
                    setOnCompletionListener { stopAlert() }
                    prepare()
                    start()
                }.onFailure(logException)
            }
        }.onFailure(logException)
    }//end of play

    private fun stopAlert() {
        runCatching {
            binding.btnAlertPlay.setChipIconResource(R.drawable.ic_play)
            binding.btnAlertPlay.text = resources.getString(R.string.play_alert)
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
        }.onFailure(logException)
    }//end of stop

    private fun getNames(type: Int): Array<String> {
        val athanNames = arrayListOf<String>()
        athanNames.add(
            when (type) {
                0 -> resources.getString(R.string.default_athan_name)
                1 -> resources.getString(R.string.default_fajr_athan_name)
                else -> resources.getString(R.string.default_alert_before_name)
            }
        )

        val existsFiles = getAllAvailableAthans(this)
        val inDB = athanDB.athanDAO().getAllAthans()
        for (f in existsFiles) {
            val fName = getFileNameFromLink(f.absolutePath)
            for (d in inDB)
                if ((d.link.contains(fName) || d.fileName.contains(fName)) &&
                    (d.type == type || (type == 1 && d.type == 0) || (type == 0 && d.type == 1))
                )
                    athanNames.add(d.name)
        }

        val res = Array(athanNames.size, init = { i -> "$i" })
        athanNames.toArray(res)
        return res
    }

}//end of class

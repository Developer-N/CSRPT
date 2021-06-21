package ir.namoo.religiousprayers.ui.preferences

import android.app.Dialog
import android.content.DialogInterface
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.NumberPicker
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.ItemAthanSettingBinding
import ir.namoo.religiousprayers.db.AthanDB
import ir.namoo.religiousprayers.db.AthanSetting
import ir.namoo.religiousprayers.db.AthanSettingsDAO
import ir.namoo.religiousprayers.db.AthanSettingsDB
import ir.namoo.religiousprayers.praytimes.getAllAvailableAthans
import ir.namoo.religiousprayers.praytimes.getAthanUriFor
import ir.namoo.religiousprayers.ui.edit.ShapedAdapter
import ir.namoo.religiousprayers.utils.*

class AthanSettingDialog(val athanSetting: AthanSetting) : AppCompatDialogFragment() {

    private lateinit var db: AthanSettingsDAO

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var audioManager: AudioManager? = null
    private lateinit var binding: ItemAthanSettingBinding
    private var systemRingtoneNames = arrayListOf<String>()
    private var systemRingtoneUris = arrayListOf<Uri>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = ItemAthanSettingBinding.inflate(requireActivity().layoutInflater)
        db = AthanSettingsDB.getInstance(requireContext().applicationContext).athanSettingsDAO()
        audioManager = requireContext().getSystemService()
        audioManager?.getStreamVolume(AudioManager.STREAM_ALARM)

        val manager = RingtoneManager(requireContext())
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

        val dialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            BottomSheetDialog(requireContext()).apply {
                setContentView(binding.root)
                create()
            }
        else AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            setCustomTitle(null)
        }.create()

        binding.btnAthanSettingClose.setOnClickListener { dialog.dismiss() }

        return dialog
    }//end of onCreateDialog

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        update(requireContext(), true)
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
                binding.volumeLayout.visibility = View.GONE
                binding.selectAthanLayout.visibility = View.GONE
                binding.playLayout.visibility = View.GONE
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
            db.update(athanSetting)
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
            db.update(athanSetting)
        }

        if (athanSetting.athanKey != "SUNRISE") {
            //######################################## PlayDOA
            binding.switchDoa.isChecked = athanSetting.playDoa
            binding.switchDoa.setOnClickListener {
                athanSetting.playDoa = binding.switchDoa.isChecked
                db.update(athanSetting)
            }

            //######################################## Before
            binding.switchAlarmBefore.isChecked = athanSetting.isBeforeEnabled
            binding.switchAlarmBefore.setOnClickListener {
                athanSetting.isBeforeEnabled = binding.switchAlarmBefore.isChecked
                db.update(athanSetting)
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
                db.update(athanSetting)
            }
            //######################################## isAscending
            binding.switchAscendingAthanVolume.isChecked = athanSetting.isAscending
            binding.switchAscendingAthanVolume.setOnClickListener {
                it as SwitchMaterial
                athanSetting.isAscending = it.isChecked
                db.update(athanSetting)
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
                        db.update(athanSetting)
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
                selectedAthan = when (athanSetting.athanURI) {
                    "" -> if (athanSetting.athanKey == "FAJR")
                        getString(R.string.default_fajr_athan_name)
                    else
                        getString(R.string.default_athan_name)
                    else -> {
                        val name = getFileNameFromLink(athanSetting.athanURI)
                        val all =
                            AthanDB.getInstance(requireContext()).athanDAO().getAllAthans()
                        var n = ""
                        for (a in all)
                            if (a.link.contains(name))
                                n = a.name
                        n
                    }
                }
            }
            val athanNames = getNames(if (athanSetting.athanKey == "FAJR") 1 else 0)
            binding.spinnerAthanName.adapter = ShapedAdapter(
                requireContext(),
                R.layout.select_dialog_item,
                athanNames
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
                        for (d in AthanDB.getInstance(requireContext()).athanDAO()
                            .getAllAthans())
                            if (selectedName == d.name) {
                                val athanUri =
                                    getAthanUriFor(
                                        requireContext(),
                                        getFileNameFromLink(d.link)
                                    )
                                if (athanUri != null) {
                                    athanSetting.athanURI = athanUri.toString()
                                    binding.txtSelectedAthanName.text = selectedName
                                }
                            }
                    }
                    db.update(athanSetting)
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
                        val all =
                            AthanDB.getInstance(requireContext()).athanDAO().getAllAthans()
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
                requireContext(),
                R.layout.select_dialog_item,
                alarmNames
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
                            val athanUri =
                                systemRingtoneUris[systemRingtoneNames.indexOf(selectedName)]
                            athanSetting.alertURI = athanUri.toString()
                            binding.txtSelectedAlarmName.text = selectedName

                        } else for (d in AthanDB.getInstance(requireContext()).athanDAO()
                            .getAllAthans())
                            if (selectedName == d.name) {
                                val athanUri = getAthanUriFor(
                                    requireContext(),
                                    getFileNameFromLink(d.link)
                                )
                                if (athanUri != null) {
                                    athanSetting.alertURI = athanUri.toString()
                                    binding.txtSelectedAlarmName.text = selectedName
                                }
                            }
                    }
                    Log.e(TAG, "onItemSelected uri: ${athanSetting.alertURI}")
                    db.update(athanSetting)
                }
            }

            //########################################### play buttons
            binding.btnAthanPlay.setOnClickListener {
                it as Chip
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
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
                                getDefaultFajrAthanUri(requireContext())
                            else
                                getDefaultAthanUri(requireContext())
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
                        requireContext(),
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
                            getDefaultBeforeAlertUri(requireContext())
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
                db.update(athanSetting)
            }

        }//end of if(!Sunrise)
    }

    private fun playAthan(uri: Uri) {
        runCatching {
            binding.btnAthanPlay.setChipIconResource(R.drawable.ic_stop)
            binding.btnAthanPlay.text = resources.getString(R.string.stop)
            mediaPlayer.apply {
                runCatching {
                    reset()
                    setDataSource(requireContext(), uri)
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
                    requireActivity().volumeControlStream = AudioManager.STREAM_ALARM
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
                    setDataSource(requireContext(), uri)
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
                    requireActivity().volumeControlStream = AudioManager.STREAM_ALARM
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
        val existsFiles = getAllAvailableAthans(requireContext())
        val db = AthanDB.getInstance(requireContext().applicationContext).athanDAO()
        val inDB = db.getAllAthans()
        for (f in existsFiles) {
            val fName = getFileNameFromLink(f.absolutePath)
            for (d in inDB)
                if (d.link.contains(fName) && d.type == type)
                    athanNames.add(d.name)
        }
        val res = Array(athanNames.size, init = { i -> "$i" })
        athanNames.toArray(res)
        return res
    }

}//end of class
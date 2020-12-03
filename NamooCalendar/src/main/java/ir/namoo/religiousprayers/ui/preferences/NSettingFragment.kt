package ir.namoo.religiousprayers.ui.preferences

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.FragmentNsettingBinding
import ir.namoo.religiousprayers.db.Athan
import ir.namoo.religiousprayers.db.AthanDB
import ir.namoo.religiousprayers.db.CityDB
import ir.namoo.religiousprayers.praytimes.getAllAvailableAthans
import ir.namoo.religiousprayers.praytimes.getAthanUriFor
import ir.namoo.religiousprayers.ui.edit.ShapedAdapter
import ir.namoo.religiousprayers.utils.*
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class NSettingFragment : Fragment() {
    private lateinit var binding: FragmentNsettingBinding
    private lateinit var lm: LocationManager
    private var lat = ""
    private var long = ""
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private val locationListener = LocationListener { showLocation(it) }

    private fun showLocation(it: Location) {
        lat = it.latitude.toString()
        long = it.longitude.toString()
        binding.txtLatitudeNs.text = formatNumber(lat)
        binding.txtLongitudeNs.text = formatNumber(long)
        binding.btnRenewLocation.isEnabled = true
        val gcd = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = gcd.getFromLocation(it.latitude, it.longitude, 1)
            if (addresses.isNotEmpty())
                binding.txtCityNs.setText(addresses[0].locality)
            else {
                binding.txtCityNs.setText("")
                binding.txtCityNs.requestFocus()
                animateVisibility(binding.btnSaveLocation, false)
                snackMessage(binding.txtCityNs, getString(R.string.intro1_msg))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            binding.txtCityNs.setText("")
            binding.txtCityNs.requestFocus()
            animateVisibility(binding.btnSaveLocation, false)
            snackMessage(binding.txtCityNs, getString(R.string.intro1_msg))
        }
        binding.txtCityNs.isEnabled = true
        binding.txtCityNs.addTextChangedListener { editable ->
            if (editable.toString().isNotEmpty()) {
                var allCities =
                    CityDB.getInstance(requireContext().applicationContext).cityDBDAO().getAllCity()
                if (allCities.isNullOrEmpty()) {
                    copyCityDB(requireContext().applicationContext)
                    allCities = CityDB.getInstance(requireContext().applicationContext).cityDBDAO()
                        .getAllCity()
                }
                val c = allCities.find { it.name == editable.toString() }
                if (c != null) {
                    binding.txtLatitudeNs.text = formatNumber(c.latitude.toString())
                    lat = c.latitude.toString()
                    binding.txtLongitudeNs.text = formatNumber(c.longitude.toString())
                    long = c.longitude.toString()
                }
            }
            if (editable.toString()
                    .isEmpty() && binding.btnSaveLocation.visibility == View.VISIBLE
            ) {
                animateVisibility(binding.btnSaveLocation, false)
                return@addTextChangedListener
            }
            if (binding.btnSaveLocation.visibility == View.GONE && editable.toString() != getString(
                    R.string.please_wait
                )
            )
                animateVisibility(binding.btnSaveLocation, true)

        }
        if (binding.btnSaveLocation.visibility == View.GONE)
            animateVisibility(binding.btnSaveLocation, true)
        lm.removeUpdates(locationListener)
        binding.btnSaveLocation.setOnClickListener {
            if (lat.isEmpty() || long.isEmpty() || lat == "0.0" || long == "0.0") {
                snackMessage(it, getString(R.string.location_error))
                return@setOnClickListener
            }
            requireContext().appPrefs.edit {
                putString(
                    PREF_GEOCODED_CITYNAME,
                    binding.txtCityNs.text.toString().trimEnd().trimStart()
                )
                putString(PREF_LATITUDE, lat)
                putString(PREF_LONGITUDE, long)
            }
            binding.txtCityNs.isEnabled = false
            animateVisibility(it, false)
            if (binding.btnCancelChangeLocation.visibility == View.VISIBLE)
                animateVisibility(binding.btnCancelChangeLocation, false)
            snackMessage(it, getString(R.string.location_changed))
        }
    }

    @SuppressLint("PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNsettingBinding.inflate(inflater)
        lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //start location block
        binding.txtCityNs.setText(requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, ""))
        binding.txtLongitudeNs.text =
            requireContext().appPrefs.getString(PREF_LONGITUDE, "0.0")?.let { formatNumber(it) }
        binding.txtLatitudeNs.text =
            requireContext().appPrefs.getString(PREF_LATITUDE, "0.0")?.let { formatNumber(it) }
        val names = mutableListOf<String>()
        var allCities =
            CityDB.getInstance(requireContext().applicationContext).cityDBDAO().getAllCity()
        if (allCities.isNullOrEmpty()) {
            copyCityDB(requireContext().applicationContext)
            allCities =
                CityDB.getInstance(requireContext().applicationContext).cityDBDAO().getAllCity()
        }
        for (c in allCities)
            names.add(c.name)
        binding.txtCityNs.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.suggestion,
                android.R.id.text1,
                names
            )
        )
        binding.txtCityNs.setOnKeyListener { v, keyCode, _ ->
            return@setOnKeyListener if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideKeyBoard(v)
                true
            } else false
        }
        binding.btnRenewLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askForPermission()
                return@setOnClickListener
            }
            if (binding.btnCancelChangeLocation.visibility == View.GONE)
                animateVisibility(binding.btnCancelChangeLocation, true)
            // request for new location
            var gpsEnabled = false
            var networkEnabled = false
            try {
                gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (ex: Exception) {
                Log.d(TAG, "checkLocationEnabled: $ex")
            }
            try {
                networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
                Log.d(TAG, "checkLocationEnabled: $ex")
            }
            if (!gpsEnabled && !networkEnabled) {
                val dialog =
                    AlertDialog.Builder(requireContext())
                dialog.setMessage(requireContext().resources.getString(R.string.gps_network_not_enabled))
                dialog.setPositiveButton(
                    requireContext().resources.getString(R.string.open_location_setting)
                ) { _: DialogInterface?, _: Int ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    requireContext().startActivity(myIntent)
                }
                dialog.setNegativeButton(
                    requireContext().getString(R.string.cancel)
                ) { paramDialogInterface: DialogInterface, _: Int -> paramDialogInterface.dismiss() }
                dialog.show()
            } else {
                binding.btnRenewLocation.isEnabled = false
                binding.txtLongitudeNs.text = "..."
                binding.txtLatitudeNs.text = "..."
                if (binding.btnSaveLocation.visibility == View.VISIBLE)
                    animateVisibility(binding.btnSaveLocation, false)
                binding.txtCityNs.setText(requireContext().resources.getString(R.string.please_wait))
                if (LocationManager.GPS_PROVIDER in lm.allProviders)
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                if (LocationManager.NETWORK_PROVIDER in lm.allProviders)
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0f,
                        locationListener
                    )
            }
        }
        binding.btnCancelChangeLocation.setOnClickListener {
            binding.txtLatitudeNs.text = requireContext().appPrefs.getString(PREF_LATITUDE, "0.0")
            binding.txtLongitudeNs.text = requireContext().appPrefs.getString(PREF_LONGITUDE, "0.0")
            binding.txtCityNs.setText(
                requireContext().appPrefs.getString(
                    PREF_GEOCODED_CITYNAME,
                    "-"
                )
            )
            binding.txtCityNs.clearFocus()
            if (it.visibility == View.VISIBLE)
                animateVisibility(it, false)
            if (binding.btnSaveLocation.visibility == View.VISIBLE)
                animateVisibility(binding.btnSaveLocation, false)
            binding.txtCityNs.isEnabled = false
            binding.btnRenewLocation.isEnabled = true
            lm.removeUpdates(locationListener)
        }
        //end location block

        initSwitches()

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ start init audioManager and mediaPlayer
        audioManager = requireContext().getSystemService()
        audioManager?.let { am ->
            am.setStreamVolume(
                AudioManager.STREAM_ALARM,
                requireContext().appPrefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)
                    .takeUnless { it == DEFAULT_ATHAN_VOLUME } ?: am.getStreamVolume(
                    AudioManager.STREAM_ALARM
                ),
                0
            )
        }
        mediaPlayer = MediaPlayer()
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ end init audioManager and mediaPlayer
        initAthans()
        initAlarmBeforeFajr()

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ feqh
        val feqhNames = resources.getStringArray(R.array.feqh)
        binding.spinnerAsrJuristics.adapter = ShapedAdapter(
            requireContext(),
            R.layout.select_dialog_item,
            feqhNames
        )
        binding.spinnerAsrJuristics.setSelection(
            if (requireContext().appPrefs.getString(
                    PREF_ASR_JURISTICS,
                    DEFAULT_ASR_JURISTICS
                ) == DEFAULT_ASR_JURISTICS
            ) 0 else 1
        )
        binding.spinnerAsrJuristics.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    asrMethod = when (position) {
                        0 -> {
                            requireContext().appPrefs.edit {
                                putString(PREF_ASR_JURISTICS, DEFAULT_ASR_JURISTICS)
                            }
                            CalculationMethod.AsrJuristics.Standard
                        }
                        else -> {
                            requireContext().appPrefs.edit {
                                putString(PREF_ASR_JURISTICS, "Hanafi")
                            }
                            CalculationMethod.AsrJuristics.Standard
                        }
                    }
                }
            }

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ feqh

        return binding.root
    }//end of onCreateView

    @SuppressLint("SetTextI18n")
    private fun initAlarmBeforeFajr() {
        binding.txtBeforeFajr.text =
            "${binding.txtBeforeFajr.text}(${resources.getString(R.string.str_minute)})"
        binding.numberPickerAlarmBeforeFajr.maxValue = 60
        binding.numberPickerAlarmBeforeFajr.minValue = 10
        binding.numberPickerAlarmBeforeFajr.value = requireContext().appPrefs.getInt(
            PREF_ALARM_BEFORE_FAJR_MIN, DEFAULT_ALARM_BEFORE_FAJR
        )
        if (requireContext().appPrefs.getBoolean(PREF_ALARM_BEFORE_FAJR_ENABLE, false)) {
            binding.switchAlarmBeforeFajr.isChecked = true
            binding.numberPickerAlarmBeforeFajr.isEnabled = true
        } else {
            binding.switchAlarmBeforeFajr.isChecked = false
            binding.numberPickerAlarmBeforeFajr.isEnabled = false
        }
        binding.switchAlarmBeforeFajr.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(
                    PREF_ALARM_BEFORE_FAJR_ENABLE,
                    binding.switchAlarmBeforeFajr.isChecked
                )
            }
            binding.numberPickerAlarmBeforeFajr.isEnabled =
                binding.switchAlarmBeforeFajr.isChecked
        }

        binding.numberPickerAlarmBeforeFajr.setOnValueChangedListener { _, _, newVal ->
            requireContext().appPrefs.edit {
                putInt(PREF_ALARM_BEFORE_FAJR_MIN, newVal)
            }
        }
    }//end if initAlarmBeforeFajr

    private fun initSwitches() {
        binding.switchDoa.isChecked = requireContext().appPrefs.getBoolean(PREF_PLAY_DOA, false)
        binding.switchDoa.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(
                    PREF_PLAY_DOA, binding.switchDoa.isChecked
                )
            }
        }

        binding.switchSummerTime.isChecked =
            requireContext().appPrefs.getBoolean(PREF_SUMMER_TIME, true)
        binding.switchSummerTime.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(PREF_SUMMER_TIME, binding.switchSummerTime.isChecked)
            }
        }

        binding.switchPlayAthanAsNotification.isChecked =
            requireContext().appPrefs.getBoolean(
                PREF_NOTIFICATION_ATHAN,
                DEFAULT_NOTIFICATION_ATHAN
            )
        binding.switchPlayAthanAsNotification.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(
                    PREF_NOTIFICATION_ATHAN, binding.switchPlayAthanAsNotification.isChecked
                )
            }
            binding.switchPlayAthanInNotification.visibility =
                if (binding.switchPlayAthanAsNotification.isChecked)
                    View.VISIBLE
                else
                    View.GONE
        }

        binding.switchPlayAthanInNotification.isChecked =
            requireContext().appPrefs.getBoolean(
                PREF_NOTIFICATION_PLAY_ATHAN,
                DEFAULT_NOTIFICATION_PLAY_ATHAN
            )
        binding.switchPlayAthanInNotification.visibility =
            if (binding.switchPlayAthanAsNotification.isChecked)
                View.VISIBLE
            else
                View.GONE
        binding.switchPlayAthanInNotification.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(
                    PREF_NOTIFICATION_PLAY_ATHAN,
                    binding.switchPlayAthanInNotification.isChecked
                )
            }
        }

        listOf(
            binding.switchFajr,
            binding.switchSunrise,
            binding.switchDhuhr,
            binding.switchAsr,
            binding.switchMaghrib,
            binding.switchIsha
        ).zip(
            listOf("FAJR", "SUNRISE", "DHUHR", "ASR", "MAGHRIB", "ISHA")
        ) { switch, name ->
            switch.isChecked = isExistAthanInPrefs(requireContext(), name)
            switch.setOnClickListener {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && switch.isChecked &&
                        !Settings.canDrawOverlays(requireContext())
                    ) {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(getString(R.string.requset_permision))
                            setMessage(getString(R.string.need_full_screen_permision))
                            setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                                requireActivity().startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + requireActivity().packageName)
                                    )
                                )
                            }
                            setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                                dialog.cancel()
                            }
                            create()
                            show()
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "initSwitches: ", ex)
                }
                updateAthanInPref(requireContext(), name)
            }
        }

        binding.switchAscendingAthanVolume.isChecked = requireContext().appPrefs.getBoolean(
            PREF_ASCENDING_ATHAN_VOLUME, false
        )
        binding.switchAscendingAthanVolume.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(
                    PREF_ASCENDING_ATHAN_VOLUME,
                    binding.switchAscendingAthanVolume.isChecked
                )
            }
            binding.seekBarAthanVolume.isEnabled = !binding.switchAscendingAthanVolume.isChecked
            binding.txtAthanVolume.setTextColor(
                if (binding.switchAscendingAthanVolume.isChecked)
                    getColorFromAttr(requireContext(), R.attr.colorTextSecond)
                else
                    getColorFromAttr(requireContext(), R.attr.colorTextNormal)
            )
        }

        //############################################start athan volume
        binding.seekBarAthanVolume.isEnabled = !binding.switchAscendingAthanVolume.isChecked
        binding.txtAthanVolume.setTextColor(
            if (binding.switchAscendingAthanVolume.isChecked)
                getColorFromAttr(requireContext(), R.attr.colorTextSecond)
            else
                getColorFromAttr(requireContext(), R.attr.colorTextNormal)
        )
        val audioManager = requireContext().getSystemService<AudioManager>()
        val volume = requireContext().appPrefs.getInt(PREF_ATHAN_VOLUME, DEFAULT_ATHAN_VOLUME)
        binding.seekBarAthanVolume.apply {
            max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7
            progress = volume
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    audioManager?.setStreamVolume(AudioManager.STREAM_ALARM, progress, 0)
                    playAthan()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    requireContext().appPrefs.edit {
                        putInt(PREF_ATHAN_VOLUME, progress)
                    }
                }
            })
        }
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ end athan volume

    }//end of initSwitches

    @SuppressLint("PrivateResource")
    private fun initAthans() {

        binding.btnAthanPlay.setOnClickListener {
            it as Chip
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            if (mediaPlayer == null || !mediaPlayer!!.isPlaying)
                playAthan()
            else
                stopAthan()

        }
        binding.btnAthanRest.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            requireContext().appPrefs.edit {
                putString(PREF_NORMAL_ATHAN_URI, "")
                putString(PREF_FAJR_ATHAN_URI, "")
                putString(PREF_BEFORE_FAJR_URI, "")
            }
            binding.txtSelectedAthan.text = resources.getString(R.string.default_athan_name)
            binding.txtSelectedFajrAthan.text =
                resources.getString(R.string.default_fajr_athan_name)
            binding.txtSelectedBeforeFajr.text =
                resources.getString(R.string.default_before_fajr_name)
            binding.spinnerNormalAthan.setSelection(0)
            binding.spinnerBeforeFajrAthan.setSelection(0)
            binding.spinnerFajrAthan.setSelection(0)
        }
        binding.btnAddLocalNormalAthan.setOnClickListener {
            addLocal(it, REQ_CODE_PICK_ATHAN_FILE)
        }
        binding.btnAddLocalFajrAthan.setOnClickListener {
            addLocal(it, REQ_CODE_PICK_FAJR_FILE)
        }
        binding.btnAddLocalBeforeFajrAthan.setOnClickListener {
            addLocal(it, REQ_CODE_PICK_ALARM_FILE)
        }
        binding.btnAddOnlineNormalAthan.setOnClickListener {
            addOnline(it, 0)
        }
        binding.btnAddOnlineFajrAthan.setOnClickListener {
            addOnline(it, 1)
        }
        binding.btnAddOnlineBeforeFajrAthan.setOnClickListener {
            addOnline(it, 2)
        }
        initAthanSpinner()
    }

    @SuppressLint("PrivateResource")
    private fun addOnline(view: View, type: Int) {
        view.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(),
                com.google.android.material.R.anim.abc_fade_in
            )
        )
        if (!isNetworkConnected(requireContext())) {
            val alert: android.app.AlertDialog.Builder =
                android.app.AlertDialog.Builder(context)
            alert.setTitle(resources.getString(R.string.network_error_title))
            alert.setMessage(resources.getString(R.string.network_error_message))
            alert.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            alert.create().show()
        } else
            GetAthansTask(type).execute()
    }

    @SuppressLint("PrivateResource")
    private fun addLocal(view: View, requestCode: Int) {
        view.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(),
                com.google.android.material.R.anim.abc_fade_in
            )
        )
        if (!isHaveStoragePermission(requireActivity()))
            askForStoragePermission(requireActivity())
        else {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/mpeg"
            }
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    getString(R.string.select_athan_file_title)
                ), requestCode
            )
        }
    }

    fun initAthanSpinner() {
        //################################## start normal athan
        var selectedAthan = resources.getString(R.string.default_athan_name)
        if (!requireContext().appPrefs.getString(PREF_NORMAL_ATHAN_URI, "").isNullOrEmpty()) {
            selectedAthan = when (requireContext().appPrefs.getString(PREF_NORMAL_ATHAN_URI, "")) {
                "", null -> resources.getString(R.string.default_athan_name)
                else -> {
                    val name = getFileNameFromLink(
                        requireContext().appPrefs.getString(
                            PREF_NORMAL_ATHAN_URI,
                            ""
                        )!!
                    )
                    val all = AthanDB.getInstance(requireContext()).athanDAO().getAllAthans()
                    var n = ""
                    for (a in all)
                        if (a.link.contains(name))
                            n = a.name
                    n
                }
            }
        }
        binding.txtSelectedAthan.text = selectedAthan
        val names = getNames(0)
        binding.spinnerNormalAthan.adapter = ShapedAdapter(
            requireContext(),
            R.layout.select_dialog_item,
            names
        )
        binding.spinnerNormalAthan.setSelection(names.indexOf(selectedAthan))
        binding.spinnerNormalAthan.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    requireContext().appPrefs.edit {
                        putString(PREF_NORMAL_ATHAN_URI, "")
                    }
                    binding.txtSelectedAthan.text = resources.getString(R.string.default_athan_name)
                } else {
                    val selectedName = names[position]
                    for (d in AthanDB.getInstance(requireContext()).athanDAO().getAllAthans())
                        if (selectedName == d.name) {
                            val athanUri =
                                getAthanUriFor(requireContext(), getFileNameFromLink(d.link))
                            if (athanUri != null) {
                                requireContext().appPrefs.edit {
                                    putString(PREF_NORMAL_ATHAN_URI, athanUri.toString())
                                }
                                binding.txtSelectedAthan.text = selectedName
                            }
                        }
                }
            }
        }
        //################################## end normal athan

        // ################################## start before fajr athan
        var selectedBFajr = resources.getString(R.string.default_before_fajr_name)
        if (!requireContext().appPrefs.getString(PREF_BEFORE_FAJR_URI, "").isNullOrEmpty()) {
            selectedBFajr = when (requireContext().appPrefs.getString(PREF_BEFORE_FAJR_URI, "")) {
                "", null -> resources.getString(R.string.default_before_fajr_name)
                else -> {
                    val name = getFileNameFromLink(
                        requireContext().appPrefs.getString(
                            PREF_BEFORE_FAJR_URI,
                            ""
                        )!!
                    )
                    val all = AthanDB.getInstance(requireContext()).athanDAO().getAllAthans()
                    var n = ""
                    for (a in all)
                        if (a.link.contains(name))
                            n = a.name
                    n
                }
            }
        }
        binding.txtSelectedBeforeFajr.text = selectedBFajr
        val bFajrNames = getNames(2)
        binding.spinnerBeforeFajrAthan.adapter = ShapedAdapter(
            requireContext(),
            R.layout.select_dialog_item,
            bFajrNames
        )
        binding.spinnerBeforeFajrAthan.setSelection(bFajrNames.indexOf(selectedBFajr))
        binding.spinnerBeforeFajrAthan.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    requireContext().appPrefs.edit {
                        putString(PREF_BEFORE_FAJR_URI, "")
                    }
                    binding.txtSelectedBeforeFajr.text =
                        resources.getString(R.string.default_before_fajr_name)
                } else {
                    val selectedName = bFajrNames[position]
                    for (d in AthanDB.getInstance(requireContext()).athanDAO().getAllAthans())
                        if (selectedName == d.name) {
                            val athanUri =
                                getAthanUriFor(requireContext(), getFileNameFromLink(d.link))
                            if (athanUri != null) {
                                requireContext().appPrefs.edit {
                                    putString(PREF_BEFORE_FAJR_URI, athanUri.toString())
                                }
                                binding.txtSelectedBeforeFajr.text = selectedName
                            }
                        }
                }
            }
        }
        //################################## end before fajr athan

        // ################################## start fajr athan
        var selectedFajr = resources.getString(R.string.default_fajr_athan_name)
        if (!requireContext().appPrefs.getString(PREF_FAJR_ATHAN_URI, "").isNullOrEmpty()) {
            selectedFajr = when (requireContext().appPrefs.getString(PREF_FAJR_ATHAN_URI, "")) {
                "", null -> resources.getString(R.string.default_fajr_athan_name)
                else -> {
                    val name = getFileNameFromLink(
                        requireContext().appPrefs.getString(
                            PREF_FAJR_ATHAN_URI,
                            ""
                        )!!
                    )
                    val all = AthanDB.getInstance(requireContext()).athanDAO().getAllAthans()
                    var n = ""
                    for (a in all)
                        if (a.link.contains(name))
                            n = a.name
                    n
                }
            }
        }
        binding.txtSelectedFajrAthan.text = selectedFajr
        val fajrNames = getNames(1)
        binding.spinnerFajrAthan.adapter = ShapedAdapter(
            requireContext(),
            R.layout.select_dialog_item,
            fajrNames
        )
        binding.spinnerFajrAthan.setSelection(fajrNames.indexOf(selectedFajr))
        binding.spinnerFajrAthan.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    requireContext().appPrefs.edit {
                        putString(PREF_FAJR_ATHAN_URI, "")
                    }
                    binding.txtSelectedFajrAthan.text =
                        resources.getString(R.string.default_fajr_athan_name)
                } else {
                    val selectedName = fajrNames[position]
                    for (d in AthanDB.getInstance(requireContext()).athanDAO().getAllAthans())
                        if (selectedName == d.name) {
                            val athanUri =
                                getAthanUriFor(requireContext(), getFileNameFromLink(d.link))
                            if (athanUri != null) {
                                requireContext().appPrefs.edit {
                                    putString(PREF_FAJR_ATHAN_URI, athanUri.toString())
                                }
                                binding.txtSelectedFajrAthan.text = selectedName
                            }
                        }
                }
            }
        }
        //################################## end fajr athan

    }//end of initAthanSpinner

    private fun getNames(type: Int): Array<String> {
        val athanNames = arrayListOf<String>()
        athanNames.add(
            when (type) {
                0 -> resources.getString(R.string.default_athan_name)
                1 -> resources.getString(R.string.default_fajr_athan_name)
                else -> resources.getString(R.string.default_before_fajr_name)
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

    private fun playAthan() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying)
            try {
                binding.btnAthanPlay.setChipIconResource(R.drawable.ic_stop)
                binding.btnAthanPlay.text = resources.getString(R.string.stop)
                mediaPlayer?.apply {
                    try {
                        reset()
                        setDataSource(requireContext(), getAthanUri(requireContext()))
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
                        prepare()
                        start()
                    } catch (ex: Exception) {
                        Log.e(TAG, "prepare media player error : $ex ")
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
    }

    private fun stopAthan() {
        try {
            binding.btnAthanPlay.setChipIconResource(R.drawable.ic_play)
            binding.btnAthanPlay.text = resources.getString(R.string.play_athan)
            if (mediaPlayer?.isPlaying!!) mediaPlayer?.pause()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.location_access)
                .setMessage(resources.getString(R.string.first_setup_location_message))
                .setPositiveButton(R.string.continue_button) { _, _ ->
                    requireActivity().requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        123654
                    )
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    run {
                        dialog.cancel()
                        activity?.finish()
                    }
                }.show()
        }
    }

    //############################

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQ_CODE_PICK_ATHAN_FILE ||
                    requestCode == REQ_CODE_PICK_FAJR_FILE ||
                    requestCode == REQ_CODE_PICK_ALARM_FILE)
            && resultCode == Activity.RESULT_OK
        )
            try {
                if ((data != null) && (data.data != null)) {
                    val athanFileUri = data.data ?: return
                    val inputFile =
                        requireContext().contentResolver.openInputStream(athanFileUri) ?: return
                    val outputFile = File(
                        getAthansDirectoryPath(requireContext()) + "/" + getFileNameFromLink(
                            athanFileUri.path!!
                        )
                    )
                    val outputStream = FileOutputStream(outputFile)
                    inputFile.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                    outputStream.close()
                    inputFile.close()
                    val db = AthanDB.getInstance(requireContext()).athanDAO()
                    val athan = Athan(
                        getFileNameFromLink(outputFile.absolutePath),
                        "local/${getFileNameFromLink(outputFile.absolutePath)}",
                        when (requestCode) {
                            REQ_CODE_PICK_ATHAN_FILE -> 0
                            REQ_CODE_PICK_FAJR_FILE -> 1
                            else -> 2
                        }
                    )
                    db.insert(athan)
                }
            } catch (ex: Exception) {
                Log.e(TAG, "onActivityResult: ", ex)
            }
        initAthanSpinner()
    }//end of onActivityResult

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @SuppressLint("StaticFieldLeak")
    private inner class GetAthansTask(val type: Int) : AsyncTask<String, String, String>() {
        private val url = "http://www.namoo.ir/Home/GetAthans"

        override fun doInBackground(vararg params: String?): String {
            return try {
                val athansList = arrayListOf<Athan>()
                val httpclient: HttpClient = DefaultHttpClient()
                val httpGet = HttpGet(url)
                val response: HttpResponse = httpclient.execute(httpGet)
                if (response.statusLine.statusCode == 200) {
                    val serverResponse = EntityUtils.toString(response.entity)
                    val parser = JSONParser()
                    val jsonArray: JSONArray = parser.parse(serverResponse) as JSONArray
                    val jsonObjectIterator: MutableIterator<Any?> = jsonArray.iterator()
                    while (jsonObjectIterator.hasNext()) {
                        val jt: JSONObject = jsonObjectIterator.next() as JSONObject
                        athansList.add(
                            Athan(
                                name = jt["name"].toString(),
                                link = jt["link"].toString(),
                                type = jt["type"].toString().toInt()
                            )
                        )
                    }
                    if (athansList.size > 0) {
                        val db = AthanDB.getInstance(requireContext().applicationContext)
                        for (a in athansList) {
                            val ad = db.athanDAO().getAthan(a.name)
                            if (ad == null)
                                db.athanDAO().insert(a)
                            else {
                                ad.name = a.name
                                ad.link = a.link
                                ad.type = a.type
                                db.athanDAO().update(ad)
                            }
                        }
                    }
                    "OK"
                } else
                    "Error"
            } catch (ex: Exception) {
                Log.e(TAG, "error get athans: $ex")
                "Error"
            }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            when (type) {
                0 -> binding.btnAddOnlineNormalAthan.isEnabled = false
                1 -> binding.btnAddOnlineFajrAthan.isEnabled = false
                else -> binding.btnAddOnlineBeforeFajrAthan.isEnabled = false
            }
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            when (type) {
                0 -> binding.btnAddOnlineNormalAthan.isEnabled = true
                1 -> binding.btnAddOnlineFajrAthan.isEnabled = true
                else -> binding.btnAddOnlineBeforeFajrAthan.isEnabled = true
            }
            if (!result.isNullOrEmpty() && result == "OK") {
                val dialog = AthanDownloadDialog(this@NSettingFragment,
                    AthanDB.getInstance(requireContext().applicationContext).athanDAO()
                        .getAllAthans().filter {
                            it.type == type && it.link.contains("archive.org")
                        }
                )
                dialog.show(childFragmentManager, AthanDownloadDialog::class.java.name)
            }
        }
    }

}//end of class
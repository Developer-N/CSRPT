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
import android.view.animation.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.FragmentNsettingBinding
import ir.namoo.religiousprayers.databinding.ItemAthanSettingBinding
import ir.namoo.religiousprayers.db.*
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
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var audioManager: AudioManager? = null
    private val locationListener = LocationListener { showLocation(it) }

    private lateinit var athansAdapter: AthansAdapter

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

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ start init audioManager and mediaPlayer
        audioManager = requireContext().getSystemService()
        audioManager?.getStreamVolume(AudioManager.STREAM_ALARM)
        mediaPlayer = MediaPlayer()
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ end init audioManager and mediaPlayer

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Start feqh
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
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ End feqh

        binding.radioNotificationMethod.check(
            if (requireContext().appPrefsLite.getInt(
                    PREF_NOTIFICATION_METHOD,
                    DEFAULT_NOTIFICATION_METHOD
                ) == 1
            )
                R.id.radio_notification_method1
            else
                R.id.radio_notification_method2
        )

        binding.radioNotificationMethod.setOnCheckedChangeListener { _, checkedId ->
            requireContext().appPrefsLite.edit {
                putInt(
                    PREF_NOTIFICATION_METHOD,
                    if (checkedId == R.id.radio_notification_method1) 1 else 2
                )
            }
            loadAlarms(requireContext())
        }

        binding.radioFullscreenMethod.check(
            if (requireContext().appPrefsLite.getInt(
                    PREF_FULL_SCREEN_METHOD,
                    DEFAULT_FULL_SCREEN_METHOD
                ) == 1
            )
                R.id.radio_fullscreen_method1
            else
                R.id.radio_fullscreen_method2
        )
        binding.radioFullscreenMethod.setOnCheckedChangeListener { _, checkedId ->
            requireContext().appPrefsLite.edit {
                putInt(
                    PREF_FULL_SCREEN_METHOD,
                    if (checkedId == R.id.radio_fullscreen_method1) 1 else 2
                )
            }
            loadAlarms(requireContext())
        }
        //##################################### athans
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
        //#####################################
        athansAdapter = AthansAdapter()
        binding.recyclerAthans.adapter = athansAdapter
        binding.recyclerAthans.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }//end of onCreateView

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
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

        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!result.isNullOrEmpty() && result == "OK") {
                val dialog = AthanDownloadDialog(athansAdapter,
                    AthanDB.getInstance(requireContext().applicationContext).athanDAO()
                        .getAllAthans().filter {
                            it.type == type && it.link.contains("archive.org")
                        }
                )
                dialog.show(childFragmentManager, AthanDownloadDialog::class.java.name)
            }
        }
    }

    //########################################################################## Athans Setting Adapter
    inner class AthansAdapter : RecyclerView.Adapter<AthansAdapter.AViewHolder>() {
        private var lastExpand: ItemAthanSettingBinding? = null
        private val aDB = AthanSettingsDB.getInstance(requireContext().applicationContext)
        private val athansName = mutableListOf<String>(
            getString(R.string.fajr),
            getString(R.string.sunrise),
            getString(R.string.dhuhr),
            getString(R.string.asr),
            getString(R.string.maghrib),
            getString(R.string.isha)
        )
        private var athansSetting = listOf<AthanSetting>()

        init {
            athansSetting = aDB.athanSettingsDAO().getAllAthanSettings()!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AViewHolder =
            AViewHolder(
                ItemAthanSettingBinding.inflate(layoutInflater, parent, false)
            )

        override fun onBindViewHolder(holder: AViewHolder, position: Int) {
            holder.bind(athansSetting[position], position)
        }

        override fun getItemCount(): Int = athansSetting.size
        override fun getItemViewType(position: Int): Int = position

        //#################
        inner class AViewHolder(var itemBinding: ItemAthanSettingBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            private var mPosition = 0

            init {
                itemBinding.allAthanSettingsLayouts.visibility = View.GONE
                itemBinding.titleLayout.setOnClickListener {
                    if (itemBinding.allAthanSettingsLayouts.visibility == View.GONE) {
                        if (lastExpand != null && lastExpand != itemBinding)
                            lastExpand?.allAthanSettingsLayouts?.visibility = View.GONE
                        itemBinding.allAthanSettingsLayouts.visibility = View.VISIBLE
                        lastExpand = itemBinding
                        (binding.recyclerAthans.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                            mPosition,
                            0
                        )
                    } else {
                        itemBinding.allAthanSettingsLayouts.visibility = View.GONE
                        lastExpand = null
                    }
                    val transition = ChangeBounds()
                    transition.interpolator = AnticipateOvershootInterpolator()
                    TransitionManager.beginDelayedTransition(binding.recyclerAthans, transition)
                }
            }

            fun bind(athanSetting: AthanSetting, position: Int) {
                mPosition = position
                when (athanSetting.athanKey) {
                    "FAJR" -> {
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.fajr)
                    }
                    "SUNRISE" -> {
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.sunrise)
                        itemBinding.switchDoa.visibility = View.GONE
                        itemBinding.beforeLayout.visibility = View.GONE
                        itemBinding.volumeLayout.visibility = View.GONE
                        itemBinding.selectAthanLayout.visibility = View.GONE
                        itemBinding.playLayout.visibility = View.GONE

                    }
                    "DHUHR" -> {
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.dhuhr)
                    }
                    "ASR" -> {
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.asr)
                    }
                    "MAGHRIB" -> {
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.maghrib)
                    }
                    "ISHA" -> {
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.isha)
                    }
                }

                //######################################## STATE
                itemBinding.itemAthanSettingState.isChecked = athanSetting.state
                itemBinding.itemAthanSettingState.setOnClickListener {
                    athanSetting.state = itemBinding.itemAthanSettingState.isChecked
                    aDB.athanSettingsDAO().update(athanSetting)
                    loadAlarms(requireContext())
                }

                //######################################## AlertType
                itemBinding.itemAthanSettingAlertType.check(
                    when (athanSetting.playType) {
                        0 -> R.id.item_athan_setting_fullscreen
                        1 -> R.id.item_athan_setting_notification
                        else -> R.id.item_athan_setting_just_notification
                    }
                )
                itemBinding.itemAthanSettingAlertType.setOnCheckedChangeListener { _, checkedId ->
                    val type =
                        when (checkedId) {
                            R.id.item_athan_setting_fullscreen -> 0
                            R.id.item_athan_setting_notification -> 1
                            else -> 2
                        }
                    athanSetting.playType = type
                    aDB.athanSettingsDAO().update(athanSetting)
                }

                if (athanSetting.athanKey != "SUNRISE") {
                    //######################################## PlayDOA
                    itemBinding.switchDoa.isChecked = athanSetting.playDoa
                    itemBinding.switchDoa.setOnClickListener {
                        athanSetting.playDoa = itemBinding.switchDoa.isChecked
                        aDB.athanSettingsDAO().update(athanSetting)
                    }

                    //######################################## Before
                    itemBinding.switchAlarmBefore.isChecked = athanSetting.isBeforeEnabled
                    itemBinding.switchAlarmBefore.setOnClickListener {
                        athanSetting.isBeforeEnabled = itemBinding.switchAlarmBefore.isChecked
                        aDB.athanSettingsDAO().update(athanSetting)
                        loadAlarms(requireContext())
                    }
                    //######################################## Before minute
                    itemBinding.numberPickerAlarmBefore.minValue = 5
                    if (athanSetting.athanKey == "FAJR")
                        itemBinding.numberPickerAlarmBefore.maxValue = 90
                    else
                        itemBinding.numberPickerAlarmBefore.maxValue = 60
                    itemBinding.numberPickerAlarmBefore.value =
                        if (athanSetting.beforeAlertMinute >= itemBinding.numberPickerAlarmBefore.minValue && athanSetting.beforeAlertMinute <= itemBinding.numberPickerAlarmBefore.maxValue) athanSetting.beforeAlertMinute else 10
                    itemBinding.numberPickerAlarmBefore.setOnValueChangedListener { numberPicker: NumberPicker, _: Int, _: Int ->
                        athanSetting.beforeAlertMinute = numberPicker.value
                        aDB.athanSettingsDAO().update(athanSetting)
                        loadAlarms(requireContext())
                    }
                    //######################################## isAscending
                    itemBinding.switchAscendingAthanVolume.isChecked = athanSetting.isAscending
                    itemBinding.switchAscendingAthanVolume.setOnClickListener {
                        it as SwitchMaterial
                        athanSetting.isAscending = it.isChecked
                        aDB.athanSettingsDAO().update(athanSetting)
                        itemBinding.seekBarAthanVolume.isEnabled = !it.isChecked
                    }
                    //######################################## AthanVolume
                    itemBinding.seekBarAthanVolume.isEnabled =
                        !itemBinding.switchAscendingAthanVolume.isChecked
                    itemBinding.seekBarAthanVolume.apply {
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
                                aDB.athanSettingsDAO().update(athanSetting)
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
                    val athanNames = getNames(if (athanSetting.athanKey == "FAJR") 1 else 0)
                    itemBinding.spinnerAthanName.adapter = ShapedAdapter(
                        requireContext(),
                        R.layout.select_dialog_item,
                        athanNames
                    )
                    itemBinding.spinnerAthanName.setSelection(athanNames.indexOf(selectedAthan))
                    itemBinding.spinnerAthanName.onItemSelectedListener = object :
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
                                itemBinding.txtSelectedAthanName.text =
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
                                            itemBinding.txtSelectedAthanName.text = selectedName
                                        }
                                    }
                            }
                            aDB.athanSettingsDAO().update(athanSetting)
                        }
                    }
                    //############################################## Alert
                    var selectedAlert = getString(R.string.default_alert_before_name)
                    if (athanSetting.alertURI.isNotEmpty()) {
                        selectedAlert = when (athanSetting.alertURI) {
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
                    val alarmNames = getNames(2)
                    itemBinding.spinnerAlarmName.adapter = ShapedAdapter(
                        requireContext(),
                        R.layout.select_dialog_item,
                        alarmNames
                    )
                    itemBinding.spinnerAlarmName.setSelection(alarmNames.indexOf(selectedAlert))
                    itemBinding.spinnerAlarmName.onItemSelectedListener = object :
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
                                itemBinding.txtSelectedAlarmName.text =
                                    resources.getString(R.string.default_alert_before_name)
                            } else {
                                val selectedName = alarmNames[position]
                                for (d in AthanDB.getInstance(requireContext()).athanDAO()
                                    .getAllAthans())
                                    if (selectedName == d.name) {
                                        val athanUri =
                                            getAthanUriFor(
                                                requireContext(),
                                                getFileNameFromLink(d.link)
                                            )
                                        if (athanUri != null) {
                                            athanSetting.alertURI = athanUri.toString()
                                            itemBinding.txtSelectedAlarmName.text = selectedName
                                        }
                                    }
                            }
                            aDB.athanSettingsDAO().update(athanSetting)
                        }
                    }

                    //########################################### play buttons
                    itemBinding.btnAthanPlay.setOnClickListener {
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
                        if (itemBinding.btnAlertPlay.text == getString(R.string.stop))
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

                    itemBinding.btnAlertPlay.setOnClickListener {
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
                        if (itemBinding.btnAthanPlay.text == getString(R.string.stop))
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

                    itemBinding.btnAthanRest.setOnClickListener {
                        athanSetting.alertURI = ""
                        athanSetting.athanURI = ""
                        athanSetting.athanVolume = 1
                        athanSetting.isAscending = false
                        athanSetting.beforeAlertMinute = 10
                        athanSetting.isBeforeEnabled = false
                        athanSetting.playDoa = false
                        athanSetting.playType = 0
                        athanSetting.state = false
                        aDB.athanSettingsDAO().update(athanSetting)
                        notifyItemChanged(position)
                    }

                }//end of if(!Sunrise)
            }//end of bind

            private fun playAthan(uri: Uri) {
                try {
                    itemBinding.btnAthanPlay.setChipIconResource(R.drawable.ic_stop)
                    itemBinding.btnAthanPlay.text = resources.getString(R.string.stop)
                    mediaPlayer.apply {
                        try {
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
                        } catch (ex: Exception) {
                            Log.e(TAG, "prepare media player error : $ex ")
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "play: ", ex)
                }
            }//end of play

            private fun stopAthan() {
                try {
                    itemBinding.btnAthanPlay.setChipIconResource(R.drawable.ic_play)
                    itemBinding.btnAthanPlay.text = resources.getString(R.string.play_athan)
                    if (mediaPlayer.isPlaying) mediaPlayer.pause()
                } catch (ex: Exception) {
                    Log.e(TAG, "stop: ", ex)
                }
            }//end of stop

            private fun playAlert(uri: Uri) {
                try {
                    itemBinding.btnAlertPlay.setChipIconResource(R.drawable.ic_stop)
                    itemBinding.btnAlertPlay.text = resources.getString(R.string.stop)
                    mediaPlayer.apply {
                        try {
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
                        } catch (ex: Exception) {
                            Log.e(TAG, "prepare media player error : $ex ")
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "play: ", ex)
                }
            }//end of play

            private fun stopAlert() {
                try {
                    itemBinding.btnAlertPlay.setChipIconResource(R.drawable.ic_play)
                    itemBinding.btnAlertPlay.text = resources.getString(R.string.play_alert)
                    if (mediaPlayer.isPlaying) mediaPlayer.pause()
                } catch (ex: Exception) {
                    Log.e(TAG, "stop: ", ex)
                }
            }//end of stop
        }//end of class AViewHolder
    } //end of class AthansAdapter
}//end of class NSettingFragment
package ir.namoo.religiousprayers.ui.preferences

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.view.animation.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.FragmentNsettingBinding
import ir.namoo.religiousprayers.databinding.ItemAthanSettingBinding
import ir.namoo.religiousprayers.db.*
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
import java.util.*


class NSettingFragment : Fragment() {
    private lateinit var binding: FragmentNsettingBinding
    private lateinit var lm: LocationManager
    private var lat = ""
    private var long = ""
    private val locationListener = LocationListener { showLocation(it) }

    private lateinit var athansAdapter: AthansAdapter

    private fun showLocation(it: Location) {
        lat = it.latitude.toString()
        long = it.longitude.toString()
        binding.txtLatitudeNs.text = formatNumber(lat)
        binding.txtLongitudeNs.text = formatNumber(long)
        binding.btnRenewLocation.isEnabled = true
        val gcd = Geocoder(requireContext(), Locale.getDefault())
        runCatching {
            val addresses: List<Address> = gcd.getFromLocation(it.latitude, it.longitude, 1)
            if (addresses.isNotEmpty())
                binding.txtCityNs.setText(addresses[0].locality)
            else {
                binding.txtCityNs.setText("")
                binding.txtCityNs.requestFocus()
                animateVisibility(binding.btnSaveLocation, false)
                snackMessage(binding.txtCityNs, getString(R.string.intro1_msg))
            }
        }.onFailure(logException).getOrElse {
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
                R.id.text,
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
            runCatching {
                gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            }.onFailure(logException)
            runCatching {
                networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }.onFailure(logException)
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
        //#####################################
        binding.btnClearAddedAthans.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            if (!isHaveStoragePermission(requireActivity()))
                askForStoragePermission(requireActivity())
            else {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(R.string.warning)
                    setMessage(R.string.all_added_athans_will_cleared)
                    setPositiveButton(R.string.go) { _, _ ->
                        runCatching {
                            val db =
                                AthanDB.getInstance(requireContext().applicationContext).athanDAO()
                            db.clearDB()

                            val dir = File(getAthansDirectoryPath(requireContext())).listFiles()
                            if (dir != null && dir.isNotEmpty())
                                for (f in dir)
                                    f.delete()
                            snackMessage(it, getString(R.string.done))
                            val aDB =
                                AthanSettingsDB.getInstance(requireContext().applicationContext)
                                    .athanSettingsDAO()
                            val athanSettings = aDB.getAllAthanSettings()
                            if (athanSettings != null && athanSettings.isNotEmpty())
                                for (s in athanSettings) {
                                    s.athanURI = ""
                                    s.alertURI = ""
                                    aDB.update(s)
                                }
                            athansAdapter.notifyDataSetChanged()
                        }.onFailure(logException)
                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    create().show()
                }
            }
        }

        binding.switchSummerTime.isChecked =
            requireContext().appPrefs.getBoolean(PREF_SUMMER_TIME, true)
        binding.switchSummerTime.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(PREF_SUMMER_TIME, binding.switchSummerTime.isChecked)
            }
            update(requireContext(), updateDate = true)
        }

        return binding.root
    }//end of onCreateView

    @SuppressLint("PrivateResource")
    private fun addOnline(view: View, type: Int) {
        view.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(),
                com.google.android.material.R.anim.abc_fade_in
            )
        )
        if (!isNetworkConnected(requireContext())) {
            val alert = AlertDialog.Builder(requireContext())
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
            runCatching {
                if ((data != null) && (data.data != null)) {
                    val athanFileUri = data.data ?: return
                    val inputFile =
                        requireContext().contentResolver.openInputStream(athanFileUri) ?: return
                    val extension = getFileNameFromLink(athanFileUri.path!!).split(".")[1]
                    var outputFile =
                        File(getAthansDirectoryPath(requireContext()) + "/1." + extension)
                    var index = 2
                    while (outputFile.exists()) {
                        outputFile =
                            File(getAthansDirectoryPath(requireContext()) + "/${index++}." + extension)
                    }
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
                    athansAdapter.notifyDataSetChanged()
                }
            }.onFailure(logException)
    }//end of onActivityResult

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @SuppressLint("StaticFieldLeak")
    private inner class GetAthansTask(val type: Int) : AsyncTask<String, String, String>() {
        private val url = "http://www.namoo.ir/Home/GetAthans"

        override fun doInBackground(vararg params: String?): String = runCatching {
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
                    val db = AthanDB.getInstance(requireContext().applicationContext).athanDAO()
                    for (a in athansList) {
                        val ad = db.getAthan(a.name)
                        if (ad == null)
                            db.insert(a)
                        else {
                            ad.name = a.name
                            ad.link = a.link
                            ad.type = a.type
                            db.update(ad)
                        }
                    }
                }
                "OK"
            } else
                "Error"
        }.onFailure(logException).getOrDefault("Error")

        override fun onPreExecute() {
            super.onPreExecute()

        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!result.isNullOrEmpty() && result == "OK") {
                val dialog = AthanDownloadDialog(athansAdapter,
                    AthanDB.getInstance(requireContext().applicationContext).athanDAO()
                        .getAllAthans().filter {
                            it.type == type && it.link.startsWith("http")
                        }
                )
                dialog.show(childFragmentManager, AthanDownloadDialog::class.java.name)
            }
        }
    }

    //########################################################################## Athans Setting Adapter
    inner class AthansAdapter : RecyclerView.Adapter<AthansAdapter.AViewHolder>() {
        private var athansSetting = listOf<AthanSetting>()

        init {
            athansSetting =
                AthanSettingsDB.getInstance(requireContext().applicationContext).athanSettingsDAO()
                    .getAllAthanSettings()!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AViewHolder =
            AViewHolder(
                ItemAthanSettingBinding.inflate(layoutInflater, parent, false)
            )

        override fun onBindViewHolder(holder: AViewHolder, position: Int) {
            holder.bind(athansSetting[position])
        }

        override fun getItemCount(): Int = athansSetting.size
        override fun getItemViewType(position: Int): Int = position

        //#################
        inner class AViewHolder(var itemBinding: ItemAthanSettingBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            init {
                itemBinding.allAthanSettingsLayouts.visibility = View.GONE
                itemBinding.btnAthanSettingClose.visibility = View.GONE

            }

            fun bind(athanSetting: AthanSetting) {

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
                itemBinding.titleLayout.setOnClickListener {
                    AthanSettingDialog(athanSetting).show(
                        childFragmentManager,
                        AthanSettingDialog::class.java.name
                    )
                }

            }//end of bind

        }//end of class AViewHolder
    } //end of class AthansAdapter
}//end of class NSettingFragment

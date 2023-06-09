package ir.namoo.religiousprayers.ui.preferences

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.DEFAULT_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.PREF_ASR_HANAFI_JURISTIC
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_PRAY_TIME_METHOD
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentNPrefsBinding
import com.byagowi.persiancalendar.databinding.ItemAthanSettingBinding
import com.byagowi.persiancalendar.ui.settings.locationathan.location.showGPSLocationDialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.scheduleAlarms
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.utils.update
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.persiancalendar.praytimes.CalculationMethod
import ir.namoo.commons.ATHAN_ID
import ir.namoo.commons.DEFAULT_FULL_SCREEN_METHOD
import ir.namoo.commons.DEFAULT_NOTIFICATION_METHOD
import ir.namoo.commons.DEFAULT_SUMMER_TIME
import ir.namoo.commons.FILE_PICKER_REQUEST_CODE
import ir.namoo.commons.PREF_FULL_SCREEN_METHOD
import ir.namoo.commons.PREF_NOTIFICATION_METHOD
import ir.namoo.commons.PREF_SUMMER_TIME
import ir.namoo.commons.REQ_CODE_PICK_ALARM_FILE
import ir.namoo.commons.REQ_CODE_PICK_ATHAN_FILE
import ir.namoo.commons.model.Athan
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.service.PrayTimesService
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.getAthansDirectoryPath
import ir.namoo.commons.utils.getFileNameFromLink
import ir.namoo.commons.utils.hideKeyBoard
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.snackMessage
import ir.namoo.religiousprayers.ui.AthanSettingActivity
import ir.namoo.religiousprayers.ui.shared.ShapedAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class NPreferenceFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: FragmentNPrefsBinding
    private lateinit var athansAdapter: AthansAdapter

    private val prayTimesService: PrayTimesService by inject()

    private val locationsDB: LocationsDB by inject()

    private val athanDB: AthanDB by inject()

    private val athanSettingsDB: AthanSettingsDB by inject()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNPrefsBinding.inflate(layoutInflater, container, false)

        var isAthanNotificationEnable = false
        athanSettingsDB.athanSettingsDAO().getAllAthanSettings().forEach {
            if (it.state) isAthanNotificationEnable = true
        }
        if (isAthanNotificationEnable && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.cardPhoneStatePermission.visibility = View.VISIBLE
            binding.cardPhoneStatePermission.setOnClickListener {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    Random.nextInt(2000)
                )
                binding.cardPhoneStatePermission.visibility = View.GONE
            }
        }

//        binding.btnTest.setOnClickListener {
////            val audioManager = requireContext().getSystemService<AudioManager>()
////            audioManager?.let {
////                it.ringerMode = if (it.ringerMode == AudioManager.RINGER_MODE_NORMAL)
////                    AudioManager.RINGER_MODE_VIBRATE else AudioManager.RINGER_MODE_NORMAL
////            }
//            startAthan(requireContext(), "S_$ASR_KEY", null)
//        }
//        binding.btnTestAlarm.setOnClickListener {
//            startAthan(requireContext(), "B$DHUHR_KEY", null)
//        }
        initLocation()

        binding.switchAsrHanafiJuristic.isChecked =
            requireContext().appPrefs.getBoolean(PREF_ASR_HANAFI_JURISTIC, false)
        binding.switchAsrHanafiJuristic.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(PREF_ASR_HANAFI_JURISTIC, binding.switchAsrHanafiJuristic.isChecked)
            }
            update(requireContext(), true)
        }

        binding.switchSummerTime.isChecked =
            requireContext().appPrefs.getBoolean(PREF_SUMMER_TIME, DEFAULT_SUMMER_TIME)

        binding.switchSummerTime.setOnClickListener {
            requireContext().appPrefs.edit {
                putBoolean(PREF_SUMMER_TIME, binding.switchSummerTime.isChecked)
            }
            update(requireContext(), true)
        }

        //################################## calculation method
        val names = CalculationMethod.values().map { getString(it.titleStringId) }
        val values = CalculationMethod.values().map { it.name }
        binding.spinnerCalculationMethod.adapter = ShapedAdapter(
            requireContext(), R.layout.select_dialog_item, R.id.text1, names.toTypedArray()
        )
        val selected = requireContext().appPrefs.getString(
            PREF_PRAY_TIME_METHOD,
            DEFAULT_PRAY_TIME_METHOD
        )
        binding.spinnerCalculationMethod.setSelection(values.indexOf(selected))
        binding.spinnerCalculationMethod.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    requireContext().appPrefs.edit {
                        putString(PREF_PRAY_TIME_METHOD, values[position])
                    }
                    binding.switchAsrHanafiJuristic.visibility =
                        if (position == 5 || position == 6)
                            View.GONE
                        else
                            View.VISIBLE
                    update(requireContext(), true)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        //################################## end calculation method

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
            update(requireContext(), true)
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
            update(requireContext(), true)
        }

        //##################################### athans
        binding.btnAddLocalAthan.setOnClickListener {
            addLocal(it, REQ_CODE_PICK_ATHAN_FILE)
        }
        binding.btnAddLocalAlarm.setOnClickListener {
            addLocal(it, REQ_CODE_PICK_ALARM_FILE)
        }
        binding.btnAddOnlineAthan.setOnClickListener {
            addOnline(it, 1)
        }
        binding.btnAddOnlineAlarm.setOnClickListener {
            addOnline(it, 2)
        }

        binding.btnClearAddedAthans.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.warning)
                setMessage(R.string.all_added_athans_will_cleared)
                setPositiveButton(R.string.go) { _, _ ->
                    runCatching {
                        athanDB.athanDAO().clearDB()
                        val dir = File(getAthansDirectoryPath(requireContext())).listFiles()
                        if (dir != null && dir.isNotEmpty())
                            for (f in dir)
                                f.delete()
                        snackMessage(it, getString(R.string.done))
                        val athanSettings =
                            athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
                        if (athanSettings.isNotEmpty())
                            for (s in athanSettings) {
                                s.athanURI = ""
                                s.alertURI = ""
                                athanSettingsDB.athanSettingsDAO().update(s)
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
        //#####################################
        athansAdapter = AthansAdapter()
        binding.recyclerAthans.adapter = athansAdapter
        binding.recyclerAthans.layoutManager = LinearLayoutManager(requireContext())
        //#####################################
        onSharedPreferenceChanged(requireContext().appPrefs, null)
        requireContext().appPrefs.registerOnSharedPreferenceChangeListener(this)

        return binding.root
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
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(resources.getString(R.string.network_error_title))
                setMessage(resources.getString(R.string.network_error_message))
                setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        } else {
            view.isEnabled = false
            AthanDownloadDialog(prayTimesService, athanDB, type).show(
                childFragmentManager, AthanDownloadDialog::class.java.name
            )
            view.isEnabled = true

        }
    }

    @SuppressLint("PrivateResource")
    private fun addLocal(view: View, requestCode: Int) {
        view.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(), com.google.android.material.R.anim.abc_fade_in
            )
        )
        resultLuncher.launch(requestCode)
    }

    private val resultLuncher =
        registerForActivityResult(PickSound()) { intent ->
            intent ?: return@registerForActivityResult
            runCatching {
                if (
                    intent.extras?.getInt(FILE_PICKER_REQUEST_CODE, -1)
                    == REQ_CODE_PICK_ALARM_FILE ||
                    intent.extras?.getInt(FILE_PICKER_REQUEST_CODE, -1)
                    == REQ_CODE_PICK_ATHAN_FILE
                ) {
                    val athanFileUri = intent.data ?: return@registerForActivityResult
                    val inputFile =
                        requireContext().contentResolver.openInputStream(athanFileUri)
                            ?: return@registerForActivityResult
//                    val extension = getFileNameFromLink(athanFileUri.path!!).split(".")[1]
                    var outputFile =
                        File(getAthansDirectoryPath(requireContext()) + "/1." + "mp3")
                    var index = 2
                    while (outputFile.exists()) {
                        outputFile =
                            File(getAthansDirectoryPath(requireContext()) + "/${index++}." + "mp3")
                    }
                    val outputStream = FileOutputStream(outputFile)
                    inputFile.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                    outputStream.close()
                    inputFile.close()
                    val athan = Athan(
                        getFileNameFromLink(outputFile.absolutePath),
                        "local/${getFileNameFromLink(outputFile.absolutePath)}",
                        when (intent.getIntExtra(FILE_PICKER_REQUEST_CODE, -1)) {
                            REQ_CODE_PICK_ATHAN_FILE -> 1
                            REQ_CODE_PICK_ALARM_FILE -> 2
                            else -> 2
                        }
                    )
                    athanDB.athanDAO().insert(athan)
                }
            }.onFailure(logException)
        }

    private fun initLocation() {
        binding.btnRenewLocation.setOnClickListener {
            showGPSLocationDialog(requireActivity(), viewLifecycleOwner)
            binding.btnSaveLocation.visibility = View.VISIBLE

        }
        showLocation()
        binding.txtCityNs.addTextChangedListener { editable ->
            if (!editable.isNullOrEmpty() && editable.isNotBlank() && binding.txtCityNs.hasFocus() &&
                binding.btnSaveLocation.visibility == View.GONE
            )
                binding.btnSaveLocation.visibility = View.VISIBLE
        }
        binding.txtCityNs.setOnKeyListener { v, keyCode, _ ->
            return@setOnKeyListener if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideKeyBoard(v)
                true
            } else false
        }
        lifecycleScope.launch {
            val names = mutableListOf<String>()
            val allCities = locationsDB.cityDAO().getAllCity()
            for (c in allCities)
                names.add(c.name)
            withContext(Dispatchers.Main) {
                binding.txtCityNs.setAdapter(
                    ArrayAdapter(requireContext(), R.layout.suggestion, R.id.text, names)
                )
            }
        }
        binding.btnSaveLocation.setOnClickListener {
            if (binding.txtCityNs.text.isNullOrBlank() || binding.txtCityNs.text.isNullOrBlank()) {
                snackMessage(it, getString(R.string.enter_city_name))
            } else {
                requireContext().appPrefs.edit {
                    putString(PREF_GEOCODED_CITYNAME, binding.txtCityNs.text.toString())
                }
                hideKeyBoard(it)
                binding.btnSaveLocation.visibility = View.GONE
                binding.txtCityNs.clearFocus()
                snackMessage(it, getString(R.string.saved))
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        showLocation()
    }

    private fun showLocation() {
        runCatching {
            binding.txtCityNs.setText(
                requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, DEFAULT_CITY)
            )
            binding.txtLatitudeNs.text = requireContext().appPrefs.getString(PREF_LATITUDE, "0.0")
                ?.let { formatNumber(it) }
            binding.txtLongitudeNs.text = requireContext().appPrefs.getString(PREF_LONGITUDE, "0.0")
                ?.let { formatNumber(it) }
        }.onFailure(logException)
    }

    private val athanSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            athansAdapter.update()
        }

    inner class AthansAdapter : RecyclerView.Adapter<AthansAdapter.AViewHolder>() {
        private var athansSetting = listOf<AthanSetting>()

        init {
            athansSetting = athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun update() {
            athansSetting = athanSettingsDB.athanSettingsDAO().getAllAthanSettings()
            notifyDataSetChanged()
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
        inner class AViewHolder(private var itemBinding: ItemAthanSettingBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(athanSetting: AthanSetting) {
                when (athanSetting.athanKey) {
                    "FAJR" ->
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.fajr)

                    "SUNRISE" ->
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.sunrise)

                    "DHUHR" ->
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.dhuhr)

                    "ASR" ->
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.asr)

                    "MAGHRIB" ->
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.maghrib)

                    "ISHA" ->
                        itemBinding.itemAthanSettingTitle.text = getString(R.string.isha)
                }
                itemBinding.itemAthanSettingState.isChecked = athanSetting.state
                itemBinding.itemAthanSettingState.setOnClickListener {
                    athanSetting.state = itemBinding.itemAthanSettingState.isChecked
                    athanSettingsDB.athanSettingsDAO().update(athanSetting)
                    scheduleAlarms(requireContext())
                    if (athanSetting.state && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        !Settings.canDrawOverlays(requireContext())
                    ) {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(getString(R.string.requset_permision))
                            setMessage(getString(R.string.need_full_screen_permision))
                            setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                                startActivity(
                                    Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + requireContext().packageName)
                                    )
                                )
                            }
                            setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                                dialog.cancel()
                            }
                            show()
                        }
                    }
                }
                itemBinding.root.setOnClickListener {
                    athanSettingLauncher.launch(
                        Intent(requireContext(), AthanSettingActivity::class.java).apply {
                            putExtra(ATHAN_ID, athanSetting.id)
                        }
                    )
                }
            }
        }
    }
}//end of class NPreferenceFragment

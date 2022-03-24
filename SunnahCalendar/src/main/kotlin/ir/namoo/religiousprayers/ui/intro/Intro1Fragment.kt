package ir.namoo.religiousprayers.ui.intro

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.DEFAULT_CITY
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentIntro1Binding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.service.PrayTimesService
import ir.namoo.commons.utils.hideKeyBoard
import ir.namoo.religiousprayers.ui.IntroActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class Intro1Fragment : Fragment() {
    private lateinit var binding: FragmentIntro1Binding
    private var locationManager: LocationManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var latitude: String? = null
    private var longitude: String? = null
    private var cityName: String? = null
    private val checkGPSProviderCallback = Runnable { checkGPSProvider() }
    private var lacksPermission = false
    private var everRegisteredCallback = false
    private lateinit var allCities: List<CityModel>
    private val locationListener =
        LocationListener { Handler(Looper.getMainLooper()).postDelayed({ showLocation(it) }, 2000) }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (!granted)
                askForPermission()
            else
                getLocation()
        }

    @Inject
    lateinit var locationsDB: LocationsDB

    @Inject
    lateinit var prayTimesService: PrayTimesService

    @SuppressLint("PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntro1Binding.inflate(inflater, container, false)
        lifecycleScope.launchWhenStarted {
            if (locationsDB.countryDAO().getAllCountries().isNullOrEmpty()) {
                locationsDB.countryDAO().insert(prayTimesService.getAllCountries())
            }
            if (locationsDB.provinceDAO().getAllProvinces().isNullOrEmpty()) {
                locationsDB.provinceDAO().insert(prayTimesService.getAllProvinces())
            }
            if (locationsDB.cityDAO().getAllCity().isNullOrEmpty()) {
                locationsDB.cityDAO().insert(prayTimesService.getAllCities())
            }
            allCities = locationsDB.cityDAO().getAllCity()
        }
        lifecycleScope.launchWhenStarted {
            withContext(Dispatchers.Main) {
                val animation =
                    AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
                animation.interpolator = LinearInterpolator()
                animation.repeatCount = Animation.INFINITE
                binding.iconLocation.startAnimation(animation)
            }
        }

        locationManager = requireActivity().getSystemService()
        getLocation()
        if (lacksPermission) {
            askForPermission()
        }
        handler.postDelayed(checkGPSProviderCallback, TimeUnit.SECONDS.toMillis(30))
        binding.btnIntro1Next.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            if (latitude?.toDouble() == 0.0 && longitude?.toDouble() == 0.0) {
                binding.txtInfoMsg.text = getString(R.string.location_not_detected)
                requireActivity().finish()
                requireActivity().startActivity(
                    Intent(
                        requireActivity(),
                        IntroActivity::class.java
                    )
                )
                return@setOnClickListener
            }
            requireContext().appPrefs.edit {
                putString(
                    PREF_GEOCODED_CITYNAME,
                    binding.txtCityName.text.toString().trimStart().trimEnd()
                )
                putString(PREF_LATITUDE, latitude)
                putString(PREF_LONGITUDE, longitude)
                putBoolean(PREF_FIRST_START, false)
            }
            hideKeyBoard(it)
            startActivity(Intent(requireContext().applicationContext, MainActivity::class.java))
            requireActivity().finish()
        }
        binding.btnIntro1Prev.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            (requireActivity() as IntroActivity).goTo(0)
        }
        return binding.root
    }//end of onCreateView

    private fun goNext() {
        requireActivity().appPrefs.edit {
            putString(PREF_LATITUDE, latitude)
            putString(PREF_LONGITUDE, longitude)
            putString(PREF_GEOCODED_CITYNAME, cityName ?: "")
            putString(PREF_SELECTED_LOCATION, DEFAULT_CITY)
            apply()
        }
        (requireActivity() as IntroActivity).goTo(1)
    }

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.location_access)
                .setMessage(resources.getString(R.string.first_setup_location_message))
                .setPositiveButton(R.string.continue_button) { _, _ ->
                    permReqLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
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

    private fun checkGPSProvider() {
        if (latitude != null && longitude != null) return
        runCatching {
            val gps = activity?.getSystemService<LocationManager>()
            if (gps?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setMessage(resources.getString(R.string.turn_on_location))
                    .setPositiveButton(R.string.accept) { _, _ ->
                        runCatching {
                            requireActivity().startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                        }.onFailure(logException)
                    }.show()
            }
        }.onFailure(logException)
    }

    @SuppressLint("PrivateResource")
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            lacksPermission = true
            return
        }
        // request for new location
        var gpsEnabled = false
        var networkEnabled = false
        runCatching {
            gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        }.onFailure(logException)
        runCatching {
            networkEnabled =
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
        }.onFailure(logException)
        if (!gpsEnabled && !networkEnabled) {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setMessage(requireContext().resources.getString(R.string.gps_network_not_enabled))
                setPositiveButton(
                    requireContext().resources.getString(R.string.open_location_setting)
                ) { _: DialogInterface?, _: Int ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    requireContext().startActivity(myIntent)
                }
                setNegativeButton(
                    requireContext().getString(R.string.cancel)
                ) { paramDialogInterface: DialogInterface, _: Int -> paramDialogInterface.dismiss() }
                show()
            }
        } else {
            runCatching {
                val animation =
                    AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
                animation.interpolator = LinearInterpolator()
                animation.repeatCount = Animation.INFINITE
                binding.iconLocation.startAnimation(animation)
            }.onFailure(logException)
            locationManager?.apply {
                if (LocationManager.GPS_PROVIDER in allProviders) {
                    requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                    everRegisteredCallback = true
                }

                if (LocationManager.NETWORK_PROVIDER in allProviders) {
                    requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0,
                        0f,
                        locationListener
                    )
                    everRegisteredCallback = true
                }
            }
        }
    }

    private fun showLocation(location: Location) {
        latitude = "%f".format(Locale.ENGLISH, location.latitude)
        longitude = "%f".format(Locale.ENGLISH, location.longitude)
        val gcd = Geocoder(requireContext(), Locale.getDefault())
        runCatching {
            val addresses: List<Address> =
                gcd.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNotEmpty()) {
                cityName = addresses[0].locality
            }
        }.onFailure(logException)
        save()
    }

    @SuppressLint("SetTextI18n", "PrivateResource")
    private fun save() {
        if (latitude != null && longitude != null) {
            binding.iconLocation.clearAnimation()
            binding.txtLatitude.text =
                "${getString(R.string.latitude)} : ${formatNumber(latitude!!)}"
            binding.txtLongitude.text =
                "${getString(R.string.longitude)} : ${formatNumber(longitude!!)}"
            if (cityName.isNullOrEmpty()) {
                binding.txtInfoMsg.text = getString(R.string.intro1_msg)
                binding.txtInfoMsg.visibility = View.VISIBLE
                binding.txtCityName.requestFocus()
                val names = mutableListOf<String>()
                for (c in allCities)
                    names.add(c.name)
                binding.txtCityName.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        R.layout.suggestion,
                        R.id.text,
                        names
                    )
                )
                binding.txtCityName.addTextChangedListener { editable ->
                    if (!editable?.toString().isNullOrEmpty()) {
                        val c = allCities.find { it.name == editable.toString() }
                        if (c != null) {
                            binding.txtLatitude.text = formatNumber(c.latitude.toString())
                            latitude = c.latitude.toString()
                            binding.txtLongitude.text = formatNumber(c.longitude.toString())
                            longitude = c.longitude.toString()
                        }
                        if (binding.btnIntro1Next.visibility == View.GONE) {
                            binding.btnIntro1Next.visibility = View.VISIBLE
                            binding.txtInfoMsg.visibility = View.GONE
                            binding.btnIntro1Next.startAnimation(
                                AnimationUtils.loadAnimation(
                                    requireContext(),
                                    com.google.android.material.R.anim.abc_grow_fade_in_from_bottom
                                )
                            )
                        }
                        cityName = editable.toString()
                    } else {
                        binding.btnIntro1Next.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                com.google.android.material.R.anim.abc_shrink_fade_out_from_bottom
                            )
                        )
                        binding.btnIntro1Next.visibility = View.GONE
                        binding.txtInfoMsg.visibility = View.VISIBLE
                    }
                    val transition = ChangeBounds()
                    transition.interpolator = LinearOutSlowInInterpolator()
                    TransitionManager.beginDelayedTransition(binding.materialCardView, transition)
                }
                binding.txtCityName.setOnKeyListener { v, keyCode, _ ->
                    return@setOnKeyListener if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        hideKeyBoard(v)
                        goNext()
                        true
                    } else false
                }
            } else {
                binding.txtCityName.setText(cityName)
                binding.btnIntro1Next.visibility = View.VISIBLE
            }

        }
        if (everRegisteredCallback) {
            locationManager?.removeUpdates(locationListener)
        }
        handler.removeCallbacks(checkGPSProviderCallback)
    }

    override fun onResume() {
        super.onResume()
        getLocation()
    }

}//end of class Intro1Fragment

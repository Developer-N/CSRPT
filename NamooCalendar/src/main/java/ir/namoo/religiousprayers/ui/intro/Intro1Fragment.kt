package ir.namoo.religiousprayers.ui.intro

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.FragmentIntro1Binding
import ir.namoo.religiousprayers.db.CityDB
import ir.namoo.religiousprayers.db.CityInDB
import ir.namoo.religiousprayers.ui.IntroActivity
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class Intro1Fragment : Fragment() {
    private lateinit var binding: FragmentIntro1Binding
    private var locationManager: LocationManager? = null
    private val handler = Handler()
    private var latitude: String? = null
    private var longitude: String? = null
    private var cityName: String? = null
    private val checkGPSProviderCallback = Runnable { checkGPSProvider() }
    private var lacksPermission = false
    private var everRegisteredCallback = false
    private lateinit var allCities: List<CityInDB>
    private val locationListener =
        LocationListener { Handler(Looper.getMainLooper()).postDelayed({ showLocation(it) }, 2000) }

    @SuppressLint("PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntro1Binding.inflate(inflater, container, false)
        allCities = CityDB.getInstance(requireContext().applicationContext).cityDBDAO()
            .getAllCity()
        if (allCities.isNullOrEmpty()) {
            copyCityDB(requireContext().applicationContext)
            allCities = CityDB.getInstance(requireContext().applicationContext).cityDBDAO()
                .getAllCity()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            val animation =
                AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
            animation.interpolator = LinearInterpolator()
            animation.repeatCount = Animation.INFINITE
            binding.iconLocation.startAnimation(animation)
        }, 1000)
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
            requireActivity().appPrefs.edit {
                putBoolean(PREF_FIRST_START, false)
            }
            requireActivity().finish()
            startActivity(Intent(requireContext().applicationContext, MainActivity::class.java))
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

    private fun checkGPSProvider() {
        if (latitude != null && longitude != null) return
        try {
            val gps = activity?.getSystemService<LocationManager>()
            if (gps?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                AlertDialog.Builder(requireActivity())
                    .setMessage(resources.getString(R.string.turn_on_location))
                    .setPositiveButton(R.string.accept) { _, _ ->
                        try {
                            requireActivity().startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.create().show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        try {
            gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        } catch (ex: Exception) {
            Log.d(TAG, "checkLocationEnabled: $ex")
        }
        try {
            networkEnabled =
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
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
            try {
                val animation =
                    AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_fade_in)
                animation.interpolator = LinearInterpolator()
                animation.repeatCount = Animation.INFINITE
                binding.iconLocation.startAnimation(animation)
            } catch (ex: Exception) {
            }
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
        val addresses: List<Address>
        try {
            addresses = gcd.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNotEmpty()) {
                cityName = addresses[0].locality
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
                binding.txtCityName.isEnabled = true
                binding.txtCityName.requestFocus()
                val names = mutableListOf<String>()
                for (c in allCities)
                    names.add(c.name)
                binding.txtCityName.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        R.layout.suggestion,
                        android.R.id.text1,
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123654) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            )
                askForPermission()
            else
                getLocation()
        }
    }

    override fun onResume() {
        super.onResume()
        getLocation()
    }

}//end of class Intro1Fragment
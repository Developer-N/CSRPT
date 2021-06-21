package ir.namoo.religiousprayers.ui.compass

import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentCompassBinding
import ir.namoo.religiousprayers.utils.getCityName
import ir.namoo.religiousprayers.utils.getCoordinate
import ir.namoo.religiousprayers.utils.logException
import ir.namoo.religiousprayers.utils.setupUpNavigation
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.praytimes.Coordinate
import kotlin.math.abs

/**
 * Compass/Qibla activity
 */
class CompassFragment : Fragment() {

    var stopped = false
    private var binding: FragmentCompassBinding? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var orientation = 0f
    private var sensorNotFound = false
    private var coordinate: Coordinate? = null

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private val compassListener = object : SensorEventListener {
        /*
         * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
         * value basically means more smoothing See:
         * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
         */
        val ALPHA = 0.15f
        var azimuth: Float = 0f

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            // angle between the magnetic north direction
            // 0=North, 90=East, 180=South, 270=West
            if (event == null) return
            var angle = event.values[0] + orientation
            if (stopped)
                angle = 0f
            else
                binding?.compassView?.isOnDirectionAction()

            azimuth = lowPass(angle, azimuth)
            binding?.compassView?.setBearing(azimuth)
        }

        /**
         * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
         * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
         */
        private fun lowPass(input: Float, output: Float): Float = when {
            abs(180 - input) > 170 -> input
            else -> output + ALPHA * (input - output)
        }
    }

    private fun showLongSnackbar(@StringRes messageId: Int, duration: Int) {
        val rootView = view ?: return
        Snackbar.make(rootView, messageId, duration).apply {
            view.setOnClickListener { dismiss() }
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5
            anchorView = binding?.fab
        }.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCompassBinding.inflate(inflater, container, false).apply {
            coordinate = getCoordinate(inflater.context)

            appBar.toolbar.let {
                it.setTitle(R.string.compass)
                it.subtitle = getCityName(inflater.context, true)
                it.setupUpNavigation()
            }

            bottomAppbar.replaceMenu(R.menu.compass_menu_buttons)
            bottomAppbar.setOnMenuItemClickListener { clickedMenuItem ->
                when (clickedMenuItem.itemId) {
                    R.id.level -> findNavController().navigate(CompassFragmentDirections.actionCompassToLevel())
                    R.id.map -> runCatching {
                        CustomTabsIntent.Builder().build().launchUrl(
                            activity ?: return@runCatching,
                            "https://g.co/qiblafinder".toUri()
                        )
                    }.onFailure(logException)
                    R.id.help -> showLongSnackbar(
                        when {
                            sensorNotFound -> R.string.compass_not_found
                            else -> R.string.calibrate_compass_summary
                        },
                        5000
                    )
                    else -> Unit
                }
                true
            }
            fab.setOnClickListener {
                stopped = !stopped
                fab.setImageResource(if (stopped) R.drawable.ic_play else R.drawable.ic_stop)
                fab.contentDescription = resources
                    .getString(if (stopped) R.string.resume else R.string.stop)
            }
        }
        this.binding = binding

        setCompassMetrics()
        coordinate?.apply {
            binding.compassView.setLongitude(longitude)
            binding.compassView.setLatitude(latitude)
        }
        binding.compassView.initCompassView()

        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setCompassMetrics()
    }

    private fun setCompassMetrics() {
        val activity = activity ?: return
        val displayMetrics = DisplayMetrics()

        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        binding?.compassView?.setScreenResolution(width, height - 2 * height / 8)

        when (activity.getSystemService<WindowManager>()?.defaultDisplay?.rotation) {
            Surface.ROTATION_0 -> orientation = 0f
            Surface.ROTATION_90 -> orientation = 90f
            Surface.ROTATION_180 -> orientation = 180f
            Surface.ROTATION_270 -> orientation = 270f
        }
    }

    override fun onResume() {
        super.onResume()

        sensorManager = activity?.getSystemService()
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        when {
            sensor != null -> {
                sensorManager?.registerListener(
                    compassListener,
                    sensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                if (coordinate == null) showLongSnackbar(
                    R.string.set_location,
                    Snackbar.LENGTH_SHORT
                )
            }
            else -> {
                showLongSnackbar(R.string.compass_not_found, Snackbar.LENGTH_SHORT)
                sensorNotFound = true
            }
        }
    }

    override fun onPause() {
        if (sensor != null) sensorManager?.unregisterListener(compassListener)
        super.onPause()
    }
}

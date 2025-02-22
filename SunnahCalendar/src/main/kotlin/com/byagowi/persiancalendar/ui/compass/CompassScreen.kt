package com.byagowi.persiancalendar.ui.compass

import android.animation.ValueAnimator
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_SHOW_QIBLA_IN_COMPASS
import com.byagowi.persiancalendar.PREF_TRUE_NORTH_IN_COMPASS
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_COMPASS
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_LEVEL
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_MAP
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_STOP
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.cityName
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.common.AppBottomAppBar
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuCheckableItem
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.StopButton
import com.byagowi.persiancalendar.ui.common.ThreeDotsDropdownMenu
import com.byagowi.persiancalendar.ui.icons.In24HoursIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.utils.TEN_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.formatCoordinateISO6709
import com.byagowi.persiancalendar.utils.preferences
import com.byagowi.persiancalendar.variants.debugLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CompassScreen(
    openDrawer: () -> Unit,
    navigateToLevel: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToSettingsLocationTab: () -> Unit,
    animatedContentScope: AnimatedContentScope,
) {
    val context = LocalContext.current
    val orientation = remember(LocalConfiguration.current) {
        when (context.getSystemService<WindowManager>()?.defaultDisplay?.rotation) {
            android.view.Surface.ROTATION_0 -> 0f
            android.view.Surface.ROTATION_90 -> 90f
            android.view.Surface.ROTATION_180 -> 180f
            android.view.Surface.ROTATION_270 -> 270f
            else -> 0f
        }
    }
    var sensorNotFound by remember { mutableStateOf(false) }
    var timeShift by remember { mutableFloatStateOf(0f) }
    var isTimeShiftAnimate by remember { mutableStateOf(false) }
    val timeShiftAnimate by animateFloatAsState(
        if (isTimeShiftAnimate) 24f else 0f,
        animationSpec = tween(durationMillis = if (isTimeShiftAnimate) TEN_SECONDS_IN_MILLIS.toInt() else 0),
        label = "time shift",
    ) {
        if (isTimeShiftAnimate) {
            timeShift = 0f
            isTimeShiftAnimate = false
        }
    }
    val cityName by cityName.collectAsState()
    val coordinates by coordinates.collectAsState()
    val sliderValue = if (isTimeShiftAnimate) timeShiftAnimate else timeShift
    val isSliderShown = sliderValue != 0f
    var baseTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(THIRTY_SECONDS_IN_MILLIS)
            baseTime = Date()
        }
    }
    val time = GregorianCalendar().also {
        it.time = baseTime
        it.add(GregorianCalendar.MINUTE, (sliderValue * 60f).roundToInt())
    }
    var isStopped by remember { mutableStateOf(false) }
    var compassView by remember { mutableStateOf<CompassView?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    fun showSnackbarMessage(message: String, duration: SnackbarDuration) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message, duration = duration) }
    }

    fun showSetLocationMessage() {
        coroutineScope.launch {
            if (snackbarHostState.showSnackbar(
                    context.getString(R.string.set_location),
                    duration = SnackbarDuration.Long,
                    actionLabel = context.getString(R.string.settings),
                    withDismissAction = true,
                ) == SnackbarResult.ActionPerformed
            ) navigateToSettingsLocationTab()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isSliderShown) Clock(time).toBasicFormatString() else stringResource(
                                R.string.compass
                            )
                        )
                        val subtitle = cityName ?: coordinates?.run {
                            formatCoordinateISO6709(
                                latitude,
                                longitude,
                                elevation.takeIf { it != 0.0 },
                            )
                        }
                        if (subtitle != null) SelectionContainer {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationOpenDrawerIcon(animatedContentScope, openDrawer) },
                actions = {
                    if (coordinates != null) AppIconButton(
                        icon = In24HoursIcon,
                        title = stringResource(R.string.show_sun_and_moon_path_in_24_hours),
                        iconModifier = Modifier.rotate(sliderValue / 24f * 360f),
                    ) {
                        if (isTimeShiftAnimate) {
                            isTimeShiftAnimate = false
                            timeShift = 0f
                        } else isTimeShiftAnimate = true
                    }
                    var showTrueNorth by rememberSaveable {
                        mutableStateOf(
                            context.preferences.getBoolean(PREF_TRUE_NORTH_IN_COMPASS, false)
                        )
                    }
                    var showQibla by rememberSaveable {
                        mutableStateOf(
                            context.preferences.getBoolean(PREF_SHOW_QIBLA_IN_COMPASS, true)
                        )
                    }
                    if (cityName != null || BuildConfig.DEVELOPMENT) ThreeDotsDropdownMenu(
                        animatedContentScope
                    ) { closeMenu ->
                        AppDropdownMenuCheckableItem(
                            stringResource(R.string.true_north), showTrueNorth
                        ) {
                            showTrueNorth = it
                            closeMenu()
                            compassView?.isTrueNorth = it
                        }
                        AppDropdownMenuCheckableItem(stringResource(R.string.qibla), showQibla) {
                            showQibla = it
                            closeMenu()
                            compassView?.isShowQibla = it
                            context.preferences.edit { putBoolean(PREF_SHOW_QIBLA_IN_COMPASS, it) }
                        }
                        if (BuildConfig.DEVELOPMENT) {
                            AppDropdownMenuItem({ Text("Do a rotation") }) {
                                closeMenu()
                                // Ugly, but is test only
                                val animator = ValueAnimator.ofFloat(0f, 1f)
                                animator.duration = TEN_SECONDS_IN_MILLIS
                                animator.addUpdateListener {
                                    compassView?.angle = it.animatedFraction * 360
                                }
                                if (Random.nextBoolean()) animator.start() else animator.reverse()
                            }
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(Modifier.padding(top = paddingValues.calculateTopPadding())) {
            ScreenSurface(animatedContentScope) {
                Column {
                    Box(Modifier.weight(1f, fill = false)) {
                        val surfaceColor = MaterialTheme.colorScheme.surface
                        AndroidView(
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_COMPASS),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                            factory = { CompassView(it).also { view -> compassView = view } },
                            update = {
                                it.setSurfaceColor(surfaceColor.toArgb())
                                it.setTime(time)
                            },
                        )
                        Column {
                            AnimatedVisibility(
                                visible = isSliderShown,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                Slider(
                                    valueRange = 0f..24f,
                                    value = sliderValue,
                                    onValueChange = {
                                        isTimeShiftAnimate = false
                                        timeShift = if (it == 24f) 0f else it
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        }
                        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
                    }
                    AppBottomAppBar {
                        AppIconButton(
                            icon = ImageVector.vectorResource(R.drawable.ic_level),
                            title = stringResource(R.string.level),
                            iconModifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_LEVEL),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                            onClick = navigateToLevel,
                        )
                        AppIconButton(
                            icon = Icons.Default.Map,
                            title = stringResource(R.string.map),
                            iconModifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_MAP),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                            onClick = navigateToMap,
                        )
                        AppIconButton(
                            icon = Icons.Default.Info,
                            title = stringResource(R.string.help),
                        ) {
                            if (coordinates == null) {
                                showSetLocationMessage()
                            } else showSnackbarMessage(
                                context.getString(
                                    if (sensorNotFound) R.string.compass_not_found
                                    else R.string.calibrate_compass_summary
                                ),
                                SnackbarDuration.Long,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier.sharedElement(
                                rememberSharedContentState(SHARED_CONTENT_KEY_STOP),
                                animatedVisibilityScope = animatedContentScope,
                            )
                        ) { StopButton(isStopped) { isStopped = it } }
                    }
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val sensorManager =
            context.getSystemService<SensorManager>() ?: return@DisposableEffect onDispose {}
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        @Suppress("DEPRECATION") val orientationSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        // Accessibility announcing helpers on when the phone is headed on a specific direction
        val checkIfA11yAnnounceIsNeeded = run {
            val northAnnouncer = SensorEventAnnouncer(R.string.north)
            val eastAnnouncer = SensorEventAnnouncer(R.string.east, false)
            val westAnnouncer = SensorEventAnnouncer(R.string.west, false)
            val southAnnouncer = SensorEventAnnouncer(R.string.south, false)
            val qiblaAnnouncer = SensorEventAnnouncer(R.string.qibla, false);
            { angle: Float ->
                northAnnouncer.check(context, isNearToDegree(0f, angle))
                eastAnnouncer.check(context, isNearToDegree(90f, angle))
                southAnnouncer.check(context, isNearToDegree(180f, angle))
                westAnnouncer.check(context, isNearToDegree(270f, angle))
                compassView?.qiblaHeading?.heading?.also {
                    qiblaAnnouncer.check(context, isNearToDegree(it, angle))
                }
                Unit
            }
        }
        val orientationSensorListener = object : OrientationSensorListener() {
            override val compassView: CompassView? get() = compassView
            override val isStopped: Boolean get() = isStopped
            override val orientation: Float get() = orientation
            override fun checkIfA11yAnnounceIsNeeded(angle: Float) =
                checkIfA11yAnnounceIsNeeded(angle)
        }
        val accelerometerMagneticSensorListener = object : AccelerometerMagneticSensorListener() {
            override val compassView: CompassView? get() = compassView
            override val isStopped: Boolean get() = isStopped
            override val orientation: Float get() = orientation
            override fun checkIfA11yAnnounceIsNeeded(angle: Float) =
                checkIfA11yAnnounceIsNeeded(angle)
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                debugLog("compass: ON_RESUME")
                if (coordinates == null) showSetLocationMessage()
                if (orientationSensor != null) {
                    sensorManager.registerListener(
                        orientationSensorListener,
                        orientationSensor,
                        SensorManager.SENSOR_DELAY_FASTEST
                    )
                    if (BuildConfig.DEVELOPMENT) Toast.makeText(
                        context, "dev: orientation", Toast.LENGTH_SHORT
                    ).show()
                } else if (accelerometerSensor != null && magnetometerSensor != null) {
                    sensorManager.registerListener(
                        accelerometerMagneticSensorListener,
                        accelerometerSensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                    sensorManager.registerListener(
                        accelerometerMagneticSensorListener,
                        magnetometerSensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                    if (BuildConfig.DEVELOPMENT) Toast.makeText(
                        context, "dev: acc+magnet", Toast.LENGTH_SHORT
                    ).show()
                } else if (coordinates != null) {
                    showSnackbarMessage(
                        context.getString(R.string.compass_not_found),
                        SnackbarDuration.Short,
                    )
                    sensorNotFound = true
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                debugLog("compass: ON_PAUSE")
                if (orientationSensor != null) {
                    sensorManager.unregisterListener(orientationSensorListener)
                } else if (accelerometerSensor != null && magnetometerSensor != null) {
                    sensorManager.unregisterListener(accelerometerMagneticSensorListener)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

private abstract class BaseSensorListener : SensorEventListener {
    /*
     * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
     * value basically means more smoothing See:
     * https://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
     */
    private val alpha = 0.15f
    private var azimuth: Float = 0f

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    abstract val compassView: CompassView?
    abstract val isStopped: Boolean
    abstract val orientation: Float
    abstract fun checkIfA11yAnnounceIsNeeded(angle: Float)

    protected fun update(value: Float) {
        // angle between the magnetic north direction
        // 0=North, 90=East, 180=South, 270=West
        val angle = if (isStopped) 0f else value + orientation
        if (!isStopped) checkIfA11yAnnounceIsNeeded(angle)
        azimuth = lowPass(angle, azimuth)
        compassView?.angle = azimuth
    }

    /**
     * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * https://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */
    private fun lowPass(input: Float, output: Float): Float = when {
        abs(180 - input) > 170 -> input
        else -> output + alpha * (input - output)
    }
}

private abstract class OrientationSensorListener : BaseSensorListener() {
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        update(event.values[0])
    }
}

private abstract class AccelerometerMagneticSensorListener : BaseSensorListener() {
    private val acceleration = FloatArray(3)
    private val magneticField = FloatArray(3)
    private var isAccelerationsAvailable = false
    private var isMagneticFieldAvailable = false
    private val rotationMatrix = FloatArray(9)
    private val orientationMatrix = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            acceleration[0] = event.values[0]
            acceleration[1] = event.values[1]
            acceleration[2] = event.values[2]
            isAccelerationsAvailable = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticField[0] = event.values[0]
            magneticField[1] = event.values[1]
            magneticField[2] = event.values[2]
            isMagneticFieldAvailable = true
        }

        if (isAccelerationsAvailable && isMagneticFieldAvailable && SensorManager.getRotationMatrix(
                rotationMatrix, null, acceleration, magneticField
            )
        ) {
            SensorManager.getOrientation(rotationMatrix, orientationMatrix)
            update(Math.toDegrees(orientationMatrix[0].toDouble()).toFloat())
            isAccelerationsAvailable = false
            isMagneticFieldAvailable = false
        }
    }
}

@VisibleForTesting
fun isNearToDegree(compareTo: Float, degree: Float): Boolean {
    val difference = abs(degree - compareTo)
    return if (difference > 180) 360 - difference < 3f else difference < 3f
}

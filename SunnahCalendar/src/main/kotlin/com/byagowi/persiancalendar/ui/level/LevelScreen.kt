package com.byagowi.persiancalendar.ui.level

import android.content.pm.ActivityInfo
import android.os.PowerManager
import android.view.Surface
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_COMPASS
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_LEVEL
import com.byagowi.persiancalendar.SHARED_CONTENT_KEY_STOP
import com.byagowi.persiancalendar.ui.common.AppBottomAppBar
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.common.NavigationNavigateUpIcon
import com.byagowi.persiancalendar.ui.common.ScreenSurface
import com.byagowi.persiancalendar.ui.common.ShrinkingFloatingActionButton
import com.byagowi.persiancalendar.ui.common.StopButton
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.ExtraLargeShapeCornerSize
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.utils.FIFTEEN_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.variants.debugLog

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LevelScreen(
    navigateUp: () -> Unit,
    navigateToCompass: () -> Unit,
    animatedContentScope: AnimatedContentScope,
) {
    var isStopped by remember { mutableStateOf(false) }
    var orientationProvider by remember { mutableStateOf<OrientationProvider?>(null) }
    val announcer = remember { SensorEventAnnouncer(R.string.level) }
    var cmInchFlip by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val activity = context.getActivity() ?: return@DisposableEffect onDispose {}
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                debugLog("level: ON_RESUME")
                // Rotation lock, https://stackoverflow.com/a/75984863
                val destination =
                    @Suppress("DEPRECATION") activity.windowManager?.defaultDisplay?.rotation
                activity.requestedOrientation = when (destination) {
                    Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                if (orientationProvider?.isListening == false && !isStopped) {
                    orientationProvider?.startListening()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                debugLog("level: ON_PAUSE")
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                if (orientationProvider?.isListening == true) orientationProvider?.stopListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isFullscreen) DisposableEffect(Unit) {
        val lock = context.getSystemService<PowerManager>()
            ?.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "persiancalendar:level")
        lock?.acquire(FIFTEEN_MINUTES_IN_MILLIS)

        val activity = context.getActivity() ?: return@DisposableEffect onDispose {}
        val windowInsetsController =
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            lock?.release()
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Column {
        AnimatedVisibility(visible = !isFullscreen) {
            @OptIn(ExperimentalMaterial3Api::class) TopAppBar(
                title = { Text(stringResource(R.string.level)) },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationNavigateUpIcon(navigateUp) },
                actions = {
                    run {
                        val rotation by animateFloatAsState(
                            if (cmInchFlip) 180f else 0f, label = "rotation",
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        )
                        AppIconButton(
                            icon = Icons.Default.SyncAlt,
                            title = "cm / in",
                            iconModifier = Modifier.rotate(rotation),
                        ) { cmInchFlip = !cmInchFlip }
                    }
                    AppIconButton(
                        icon = Icons.Default.Fullscreen,
                        title = stringResource(R.string.full_screen),
                    ) { isFullscreen = true }
                },
            )
        }

        val topCornersRoundness by animateDpAsState(
            if (isFullscreen) 0.dp else ExtraLargeShapeCornerSize.dp,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing),
            label = "corner",
        )
        ScreenSurface(
            animatedContentScope = animatedContentScope,
            shape = MaterialTheme.shapes.large.copy(
                topStart = CornerSize(topCornersRoundness),
                topEnd = CornerSize(topCornersRoundness),
                bottomStart = ZeroCornerSize,
                bottomEnd = ZeroCornerSize,
            ),
        ) {
            Box {
                Crossfade(targetState = cmInchFlip, label = "ruler") { state ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (isFullscreen) Modifier.safeDrawingPadding()
                                else Modifier.padding(top = topCornersRoundness)
                            ),
                        factory = ::RulerView,
                        update = { it.cmInchFlip = state },
                    )
                }
                Column {
                    AndroidView(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .then(if (isFullscreen) Modifier.safeDrawingPadding() else Modifier)
                            .sharedBounds(
                                rememberSharedContentState(key = SHARED_CONTENT_KEY_LEVEL),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                        factory = {
                            val levelView = LevelView(it)
                            context.getActivity()?.let { activity ->
                                orientationProvider = OrientationProvider(activity, levelView)
                            }
                            levelView
                        },
                        update = update@{ levelView ->
                            val provider = orientationProvider ?: return@update
                            if (isStopped && provider.isListening) {
                                levelView.onIsLevel = {}
                                provider.stopListening()
                            } else if (!provider.isListening) {
                                levelView.onIsLevel =
                                    { isLevel -> announcer.check(context, isLevel) }
                                provider.startListening()
                            }
                        },
                    )
                    AnimatedVisibility(visible = !isFullscreen) {
                        AppBottomAppBar {
                            AppIconButton(
                                icon = Icons.Default.Explore,
                                title = stringResource(R.string.compass),
                                iconModifier = Modifier.sharedBounds(
                                    rememberSharedContentState(key = SHARED_CONTENT_KEY_COMPASS),
                                    animatedVisibilityScope = animatedContentScope,
                                ),
                                onClick = navigateToCompass,
                            )
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

                var bottomWindowInset by remember { mutableStateOf(0.dp) }
                if (!isFullscreen) bottomWindowInset = with(LocalDensity.current) {
                    WindowInsets.systemBars.getBottom(this).toDp()
                }

                if (isFullscreen) Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = bottomWindowInset + 16.dp),
                ) { StopButton(isStopped) { isStopped = it } }

                ShrinkingFloatingActionButton(
                    Modifier
                        .align(Alignment.TopCenter)
                        .safeGesturesPadding()
                        .padding(top = 32.dp),
                    isVisible = isFullscreen,
                    action = { isFullscreen = false },
                    icon = Icons.Default.FullscreenExit,
                    title = stringResource(R.string.exit_full_screen),
                )
            }
        }
    }
}

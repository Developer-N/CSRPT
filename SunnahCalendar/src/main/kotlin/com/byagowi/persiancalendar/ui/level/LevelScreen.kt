package com.byagowi.persiancalendar.ui.level

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PowerManager
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.LevelScreenBinding
import com.byagowi.persiancalendar.ui.utils.SensorEventAnnouncer
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.FIFTEEN_MINUTES_IN_MILLIS
import com.byagowi.persiancalendar.utils.THREE_SECONDS_AND_HALF_IN_MILLIS
import com.google.android.material.shape.ShapeAppearanceModel

class LevelScreen : Fragment(R.layout.level_screen) {

    private var provider: OrientationProvider? = null
    private var isStopped = false
    private var lockCleanup: (() -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRotationLock()
        val binding = LevelScreenBinding.bind(view)
        binding.appBar.toolbar.setTitle(R.string.level)
        binding.appBar.toolbar.setupUpNavigation()
        val activity = activity ?: return
        provider = OrientationProvider(activity, binding.levelView)
        val announcer = SensorEventAnnouncer(R.string.level)
        binding.levelView.onIsLevel = { isLevel ->
            announcer.check(activity, isLevel, lockCleanup != null)
        }
        binding.bottomAppbar.menu.add(R.string.level).also {
            it.icon = binding.bottomAppbar.context.getCompatDrawable(R.drawable.ic_compass_menu)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }.onClick {
            // If compass wasn't in backstack (level is brought from shortcut), navigate to it
            if (!findNavController().popBackStack(R.id.compass, false))
                findNavController().navigateSafe(LevelScreenDirections.actionLevelToCompass())
        }

        binding.appBar.toolbar.menu.add("cm / in").also { menuItem ->
            val toolbarContext = binding.appBar.toolbar.context
            menuItem.icon = toolbarContext.getCompatDrawable(R.drawable.ic_sync_alt)
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menuItem.onClick {
                binding.rulerView.cmInchFlip = !binding.rulerView.cmInchFlip
            }
        }
        binding.appBar.toolbar.menu.add(getString(R.string.full_screen)).also { menuItem ->
            val defaultMask = binding.maskableFrameLayout.shapeAppearanceModel
            val rulerNonFullScreenTopSpace = (25 * resources.dp).toInt()
            binding.paddingFrameLayout.updatePadding(top = rulerNonFullScreenTopSpace)
            val toolbarContext = binding.appBar.toolbar.context
            menuItem.icon = toolbarContext.getCompatDrawable(R.drawable.ic_fullscreen)
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            var lock: PowerManager.WakeLock? = null
            menuItem.onClick {
                if (lock != null) return@onClick lockCleanup?.invoke().let { }

                lock = activity.getSystemService<PowerManager>()
                    ?.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "SunnahCalendar:level")
                lock?.acquire(FIFTEEN_MINUTES_IN_MILLIS)

                binding.bottomAppbar.performHide(true)
                binding.appBar.toolbar.isVisible = false
                binding.maskableFrameLayout.shapeAppearanceModel = ShapeAppearanceModel()
                binding.paddingFrameLayout.updatePadding(top = 0)
                binding.exitFullScreen.show()
                binding.exitFullScreen.postDelayed(THREE_SECONDS_AND_HALF_IN_MILLIS) {
                    binding.exitFullScreen.shrink()
                }

                val windowInsetsController =
                    WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                // TODO: We should fill the system status bar space also

                lockCleanup = {
                    binding.appBar.toolbar.isVisible = true
                    binding.maskableFrameLayout.shapeAppearanceModel = defaultMask
                    binding.paddingFrameLayout.updatePadding(top = rulerNonFullScreenTopSpace)
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                    binding.exitFullScreen.hide()
                    lock?.release()
                    lock = null
                    binding.bottomAppbar.performShow(true)
                    lockCleanup = null
                }
            }
        }
        binding.exitFullScreen.setOnClickListener { lockCleanup?.invoke() }
        binding.fab.setOnClickListener {
            val provider = provider ?: return@setOnClickListener
            val stop = !provider.isListening
            binding.fab.setImageResource(if (stop) R.drawable.ic_stop else R.drawable.ic_play)
            binding.fab.contentDescription = getString(if (stop) R.string.stop else R.string.resume)
            isStopped = !stop
            if (stop) provider.startListening() else provider.stopListening()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            binding.bottomAppbar.updatePadding(bottom = insets.bottom)
            binding.fab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom / 2
            }
            binding.levelView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin =
                    if (insets.bottom != 0) insets.bottom + (75 * resources.dp).toInt() else 0
            }
            WindowInsetsCompat.CONSUMED
        }

        // It could be animated but it doesn't look that smooth so skip for now
        // val insetsCallback = object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
        //     override fun onProgress(
        //         insets: WindowInsetsCompat,
        //         runningAnimations: MutableList<WindowInsetsAnimationCompat>
        //     ): WindowInsetsCompat {
        //         return WindowInsetsCompat.CONSUMED
        //     }
        // }
        // ViewCompat.setWindowInsetsAnimationCallback(binding.root, insetsCallback)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        if (!isStopped) provider?.startListening()
    }

    override fun onPause() {
        if (provider?.isListening == true) provider?.stopListening()
        lockCleanup?.invoke()
        super.onPause()
    }
}

// https://stackoverflow.com/a/75984863
private fun Fragment.setupRotationLock() {
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        val activity = activity ?: return@LifecycleEventObserver
        if (event != Lifecycle.Event.ON_PAUSE && event != Lifecycle.Event.ON_RESUME)
            return@LifecycleEventObserver
        val destination =
            if (event == Lifecycle.Event.ON_PAUSE) null
            else @Suppress("DEPRECATION") activity.windowManager?.defaultDisplay?.rotation
        activity.requestedOrientation = when (destination) {
            Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    })
}

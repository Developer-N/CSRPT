package com.byagowi.persiancalendar.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlarmManager
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.byagowi.persiancalendar.*
import com.byagowi.persiancalendar.databinding.ActivityMainBinding
import com.byagowi.persiancalendar.databinding.NavigationHeaderBinding
import com.byagowi.persiancalendar.entities.*
import com.byagowi.persiancalendar.global.*
import com.byagowi.persiancalendar.service.ApplicationService
import com.byagowi.persiancalendar.ui.calendar.CalendarScreenDirections
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.utils.*
import com.byagowi.persiancalendar.utils.*
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import io.github.persiancalendar.calendar.PersianDate
import ir.namoo.commons.BASE_API_URL
import ir.namoo.commons.NAVIGATE_TO_DOWNLOAD_FRAGMENT
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.PREF_LAST_UPDATE_CHECK
import ir.namoo.commons.model.AthanSettingsDB
import ir.namoo.commons.model.UpdateModel
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.repository.asDataState
import ir.namoo.commons.utils.*
import ir.namoo.quran.ui.QuranActivity
import ir.namoo.religiousprayers.ui.donate.DonateFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import kotlin.math.roundToInt


/**
 * Program activity for android
 */
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    NavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener,
    DrawerHost {

    private var creationDateJdn = Jdn.today()
    private var settingHasChanged = false
    private lateinit var binding: ActivityMainBinding

    private val onBackPressedCloseDrawerCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = binding.root.closeDrawer(GravityCompat.START)
    }

    private val exitId = View.generateViewId()
    private val donateID = 62366236
    private val quranID = 6236

    private val prayTimeRepository: PrayTimeRepository = get()
    private val athanSettings: AthanSettingsDB = get()

    private val receiverUD = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            navigateTo(R.id.downup)
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = false
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCloseDrawerCallback)
        initGlobal(this)

        overrideFont("SANS_SERIF", getAppFont(applicationContext))

        startEitherServiceOrWorker(this)

        readAndStoreDeviceCalendarEventsOfTheDay(applicationContext)
        update(applicationContext, false)

        binding = ActivityMainBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        ensureDirectionality()

        if (enableNewInterface && getSystemService<ActivityManager>()?.isLowRamDevice == false) {
            window?.makeWallpaperTransparency()
            binding.root.fitsSystemWindows = false
            binding.root.background = MaterialShapeDrawable().also {
                it.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(16.dp)
            }
            binding.root.clipToOutline = true
            binding.root.alpha = 0.96f
            binding.root.fitsSystemWindows = false
        }

        binding.root.addDrawerListener(createDrawerListener())
        val typeface = getAppFont(this)

        listOf(
            Triple(R.id.calendar, R.drawable.ic_date_range, R.string.calendar),
            Triple(quranID, R.drawable.ic_baseline_menu_book, R.string.quran),
            Triple(R.id.azkar, R.drawable.ic_azkar, R.string.azkar),
            Triple(R.id.monthly, R.drawable.ic_date_range, R.string.monthly_times),
            Triple(R.id.edit, R.drawable.ic_edit, R.string.edit_times),
            Triple(R.id.downup, R.drawable.ic_synce, R.string.download_upload),
            Triple(R.id.converter, R.drawable.ic_swap_vertical_circle, R.string.date_converter),
            Triple(R.id.compass, R.drawable.ic_explore, R.string.compass),
            Triple(R.id.astronomy, R.drawable.ic_astrology_horoscope, R.string.astronomy),
            Triple(R.id.settings, R.drawable.ic_settings, R.string.settings),
            Triple(R.id.about, R.drawable.ic_info, R.string.about),
            Triple(donateID, R.drawable.ic_baseline_monetization_on_24, R.string.donate),
            Triple(exitId, R.drawable.ic_cancel, R.string.exit)
        ).forEach { (id, icon, title) ->
            val spannable = SpannableString(getString(title)).apply {
                setSpan(
                    CustomTypefaceSpan("", typeface), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.navigation.menu.add(Menu.NONE, id, Menu.NONE, spannable).setIcon(icon)
        }
        binding.navigation.setNavigationItemSelectedListener(this)

        navHostFragment?.navController?.addOnDestinationChangedListener(this)
        when (intent?.action) {
            "COMPASS" -> R.id.compass
            "LEVEL" -> R.id.level
            "MAP" -> R.id.map
            "CONVERTER" -> R.id.converter
            "ASTRONOMY" -> R.id.astronomy
            "SETTINGS" -> R.id.settings
            "DEVICE" -> R.id.deviceInformation
            "AZKAR" -> R.id.azkar
            else -> null // unsupported action. ignore
        }?.also {
            navigateTo(it)
            // So it won't happen again if the activity is restarted
            intent?.action = ""
        }

        appPrefs.registerOnSharedPreferenceChangeListener(this)

        if (isShowDeviceCalendarEvents && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission()

        val persian = creationDateJdn.toPersianCalendar()
        run {
            val header = NavigationHeaderBinding.bind(binding.navigation.getHeaderView(0))
            val season = Season.fromPersianCalendar(persian, coordinates.value)
//            header.seasonImage.setImageResource(season.imageId)
            header.seasonImage.contentDescription = getString(season.nameStringId)
            header.seasonImage.setImageResource(run {
                val c = Jdn.today().toIslamicCalendar()
                when {
                    c.month == 9 -> R.drawable.ramadhan
                    c.month == 10 && c.dayOfMonth in 1..3 -> R.drawable.eid
                    else -> R.drawable.drawer_background
                }
            })
        }

        if (!appPrefs.getBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false)) {
            showChangeLanguageSnackbar()
            appPrefs.edit { putBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true) }
        }

        if (mainCalendar == CalendarType.SHAMSI && isIranHolidaysEnabled &&
            persian.year > supportedYearOfIranCalendar
        ) showAppIsOutDatedSnackbar()

        applyAppLanguage(this)

        previousAppThemeValue = appPrefs.getString(PREF_THEME, null)

        //check for update
        val persianDate = PersianDate(Jdn.today().value)
        if (isNetworkConnected(this) && appPrefs.getInt(PREF_LAST_UPDATE_CHECK, 1) != getDayNum(
                persianDate.month, persianDate.dayOfMonth
            )
        ) checkForUpdate()

        runCatching {
            val intentFilter = IntentFilter(NAVIGATE_TO_DOWNLOAD_FRAGMENT)
            registerReceiver(receiverUD, intentFilter)
        }.onFailure(logException)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService<AlarmManager>()
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) MaterialAlertDialogBuilder(
                this
            ).apply {
                setTitle(R.string.requset_permision)
                setMessage(R.string.schedule_permission_message)
                setPositiveButton(R.string.ok) { _, _ ->
                    startActivity(Intent().apply {
                        action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    })
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            MaterialAlertDialogBuilder(this).apply {
                setTitle(R.string.requset_permision)
                setMessage(R.string.post_notification_permission_message)
                setPositiveButton(R.string.ok) { _, _ ->
                    askForPostNotificationPermission()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                show()
            }
        }
        lifecycleScope.launch {
            checkAndAskPhoneStatePermission(athanSettings)
        }
    }

    // This shouldn't be needed but as a the last resort
    private fun ensureDirectionality() {
        binding.root.layoutDirection =
            if (language.isArabicScript) View.LAYOUT_DIRECTION_RTL // just in case resources isn't correct
            else resources.configuration.layoutDirection
    }

    private var previousAppThemeValue: String? = null

    private val navHostFragment by lazy {
        (supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment)
            .debugAssertNotNull
    }

    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        binding.navigation.menu.findItem(
            when (destination.id) {
                // We don't have a menu entry for compass, so
                R.id.level -> R.id.compass
                else -> destination.id
            }
        )?.also {
            it.isCheckable = true
            it.isChecked = true
        }

        if (settingHasChanged) { // update when checked menu item is changed
            applyAppLanguage(this)
            update(applicationContext, true)
            settingHasChanged = false // reset for the next time
        }
    }

    private fun navigateTo(@IdRes id: Int) {
        navHostFragment?.navController?.navigate(id, null, navOptions {
            anim {
                enter = R.anim.nav_enter_anim
                exit = R.anim.nav_exit_anim
                popEnter = R.anim.nav_enter_anim
                popExit = R.anim.nav_exit_anim
            }
        })
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        settingHasChanged = true

        prefs ?: return

        // If it is the first initiation of preference, don't call the rest multiple times
        if (key == PREF_HAS_EVER_VISITED || PREF_HAS_EVER_VISITED !in prefs) return

        when (key) {
            PREF_LAST_APP_VISIT_VERSION -> return // nothing needs to be updated
            LAST_CHOSEN_TAB_KEY -> return // don't run the expensive update and etc on tab changes
            PREF_ISLAMIC_OFFSET ->
                prefs.edit { putJdn(PREF_ISLAMIC_OFFSET_SET_DATE, Jdn.today()) }
            PREF_SHOW_DEVICE_CALENDAR_EVENTS -> {
                if (prefs.getBoolean(
                        PREF_SHOW_DEVICE_CALENDAR_EVENTS,
                        true
                    ) && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.READ_CALENDAR
                    ) != PackageManager.PERMISSION_GRANTED
                ) askForCalendarPermission()
            }
            PREF_APP_LANGUAGE -> restartToSettings()
            PREF_NEW_INTERFACE -> restartToSettings()
            PREF_THEME, PREF_APP_FONT -> {
                // Restart activity if theme is changed and don't if app theme
                // has just got a default value by preferences as going
                // from null => SystemDefault which makes no difference
                if (previousAppThemeValue != null || !Theme.isDefault(prefs)) restartToSettings()
            }
            PREF_NOTIFY_DATE -> {
                if (!prefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE)) {
                    stopService(Intent(this, ApplicationService::class.java))
                    startEitherServiceOrWorker(applicationContext)
                }
            }
            PREF_EASTERN_GREGORIAN_ARABIC_MONTHS -> loadLanguageResources(this)
        }

        configureCalendarsAndLoadEvents(this)
        updateStoredPreference(this)
        update(applicationContext, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALENDAR_READ_PERMISSION_REQUEST_CODE -> {
                val isGranted = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
                appPrefs.edit { putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, isGranted) }
                if (isGranted) {
                    val navController = navHostFragment?.navController
                    if (navController?.currentDestination?.id == R.id.calendar) navController.navigateSafe(
                        CalendarScreenDirections.navigateToSelf()
                    )
                }
            }
            POST_NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                val isGranted = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                appPrefs.edit { putBoolean(PREF_NOTIFY_DATE, isGranted) }
                updateStoredPreference(this)
                if (isGranted) update(this, updateDate = true)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
        ensureDirectionality()
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        val today = Jdn.today()
        if (creationDateJdn != today) {
            creationDateJdn = today
            val navController = navHostFragment?.navController
            if (navController?.currentDestination?.id == R.id.calendar) {
                navController.navigateSafe(CalendarScreenDirections.navigateToSelf())
            }
        }
    }

    // Checking for the ancient "menu" key
    override fun onKeyDown(keyCode: Int, event: KeyEvent?) = when (keyCode) {
        KeyEvent.KEYCODE_MENU -> {
            if (binding.root.isDrawerOpen(GravityCompat.START)) binding.root.closeDrawer(
                GravityCompat.START
            )
            else binding.root.openDrawer(GravityCompat.START)
            true
        }
        else -> super.onKeyDown(keyCode, event)
    }

    private fun restartToSettings() {
        val intent = intent
        intent?.action = "SETTINGS"
        finish()
        startActivity(intent)
    }

    private var clickedItem = 0

    override fun onNavigationItemSelected(selectedMenuItem: MenuItem): Boolean {
        when (val itemId = selectedMenuItem.itemId) {
            exitId -> finish()
            donateID -> {
                binding.root.closeDrawer(GravityCompat.START)
                DonateFragment().show(supportFragmentManager, DonateFragment::class.java.name)
            }
            quranID -> {
                binding.root.closeDrawer(GravityCompat.START)
                startActivity(Intent(this, QuranActivity::class.java))
            }
            else -> {
                binding.root.closeDrawer(GravityCompat.START)
                if (navHostFragment?.navController?.currentDestination?.id != itemId) {
                    clickedItem = itemId
                }
                applyAppLanguage(this)
            }
        }
        return true
    }

    @VisibleForTesting
    fun showChangeLanguageSnackbar() {
        if (Language.userDeviceLanguage == Language.FA.language) return
        Snackbar.make(
            binding.root, "✖  Change app language?", Snackbar.LENGTH_INDEFINITE
        ).also {
            it.view.layoutDirection = View.LAYOUT_DIRECTION_LTR
            it.view.setOnClickListener { _ -> it.dismiss() }
            it.setAction("Settings") {
                navHostFragment?.navController?.navigateSafe(
                    CalendarScreenDirections.navigateToSettings(
                        SettingsScreen.INTERFACE_CALENDAR_TAB, PREF_APP_LANGUAGE
                    )
                )
            }
        }.show()
    }

    @VisibleForTesting
    fun showAppIsOutDatedSnackbar() = Snackbar.make(
        binding.root, getString(R.string.outdated_app), 10000
    ).also {
        it.setAction(getString(R.string.update)) { bringMarketPage() }
        it.setActionTextColor(ContextCompat.getColor(it.context, R.color.dark_accent))
    }.show()

    override fun setupToolbarWithDrawer(toolbar: Toolbar) {
        val listener = ActionBarDrawerToggle(
            this, binding.root, toolbar,
            androidx.navigation.ui.R.string.nav_app_bar_open_drawer_description, R.string.close
        ).also { it.syncState() }

        binding.root.addDrawerListener(listener)
        toolbar.setNavigationOnClickListener { binding.root.openDrawer(GravityCompat.START) }
        toolbar.findViewTreeLifecycleOwner()?.lifecycle?.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                binding.root.removeDrawerListener(listener)
                toolbar.setNavigationOnClickListener(null)
            }
        })
    }

    private fun createDrawerListener() = object : DrawerLayout.SimpleDrawerListener() {
        val slidingDirection = if (resources.isRtl) -1f else +1f

        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            super.onDrawerSlide(drawerView, slideOffset)
            slidingAnimation(drawerView, slideOffset)
        }

        private val blurs = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowManager.isCrossWindowBlurEnabled
        ) (0..4).map {
            if (it == 0) null
            else RenderEffect.createBlurEffect(it * 6f, it * 6f, Shader.TileMode.CLAMP)
        } else emptyList()

        private fun slidingAnimation(drawerView: View, slideOffset: Float) {
            binding.navHostFragment.translationX =
                slideOffset * drawerView.width.toFloat() * slidingDirection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurs.isNotEmpty()) {
                val blurIndex =
                    if (slideOffset.isNaN()) 0 else ((blurs.size - 1) * slideOffset).roundToInt()
                binding.navHostFragment.setRenderEffect(blurs[blurIndex])
                binding.navigation.getHeaderView(0)
                    .setRenderEffect(blurs[blurs.size - 1 - blurIndex])
            }
            binding.root.bringChildToFront(drawerView)
            binding.root.requestLayout()
        }

        override fun onDrawerOpened(drawerView: View) {
            super.onDrawerOpened(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = true
        }

        override fun onDrawerClosed(drawerView: View) {
            super.onDrawerClosed(drawerView)
            onBackPressedCloseDrawerCallback.isEnabled = false
            if (clickedItem != 0) {
                navigateTo(clickedItem)
                clickedItem = 0
            }
        }
    }

    override fun onStop() {
        super.onStop()
        runCatching {
            unregisterReceiver(receiverUD)
        }.onFailure {}
    }

    private fun checkForUpdate() {
        lifecycleScope.launch(Dispatchers.IO) {
            prayTimeRepository.getLastUpdateInfo().collect { result ->
                when (result.asDataState()) {
                    is DataState.Error -> {}
                    DataState.Loading -> {}
                    is DataState.Success -> {
                        val serverLastUpdate =
                            (result.asDataState() as DataState.Success<List<UpdateModel>>).data.last()
                        val pInfo: PackageInfo =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                packageManager.getPackageInfo(
                                    packageName, PackageManager.PackageInfoFlags.of(0)
                                )
                            } else {
                                @Suppress("DEPRECATION") packageManager.getPackageInfo(
                                    packageName, 0
                                )
                            }
                        val versionCode: Long = PackageInfoCompat.getLongVersionCode(pInfo)
                        if (versionCode < serverLastUpdate.versionCode) withContext(Dispatchers.Main) {
                            MaterialAlertDialogBuilder(this@MainActivity).apply {
                                setTitle(R.string.update)
                                setMessage(getString(R.string.update_available) + "\n" + serverLastUpdate.changes)
                                setNegativeButton(R.string.cancel) { dialog, _ ->
                                    dialog.dismiss()
                                }
                                setNeutralButton(R.string.download) { _, _ ->
                                    openUrlInCustomTab("$BASE_API_URL/app/downloadApp/${serverLastUpdate.id}")
                                }
                                setPositiveButton(R.string.markets) { _, _ ->
                                    bringMarketPage()
                                }
                                show()
                            }
                        }
                    }
                }
            }
        }
    }
}

package ir.namoo.religiousprayers.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.github.persiancalendar.calendar.PersianDate
import ir.namoo.quran.ui.QuranActivity
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.ActivityMainBinding
import ir.namoo.religiousprayers.databinding.NavigationHeaderBinding
import ir.namoo.religiousprayers.service.ApplicationService
import ir.namoo.religiousprayers.ui.calendar.CalendarFragment
import ir.namoo.religiousprayers.utils.*
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.util.*

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    NavigationView.OnNavigationItemSelectedListener {

    private var creationDateJdn: Long = 0
    private var settingHasChanged = false
    private lateinit var binding: ActivityMainBinding

    private val receiverUD = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            navigateTo(R.id.downup)
        }
    }
    val coordinator: CoordinatorLayout
        get() = binding.coordinator

    private var clickedItem = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))

        applyAppLanguage(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
//        ReleaseDebugDifference.startLynxListenerIfIsDebug(this)
        initUtils(this)

        // Don't apply font override to English and Japanese locale
        if (language !in listOf(LANG_EN_US, LANG_JA))
            overrideFont("SANS_SERIF", getAppFont(applicationContext))


        startEitherServiceOrWorker(this)

        // Doesn't matter apparently
        // oneTimeClockDisablingForAndroid5LE();
        setDeviceCalendarEvents(applicationContext)
        update(applicationContext, false)
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        setSupportActionBar(binding.toolbar)
        changeToolbarTypeface(binding.toolbar)
        changeNavigationItemTypeface(binding.navigation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.apply {
            // https://learnpainless.com/android/material/make-fully-android-transparent-status-bar
            attributes = attributes.apply {
                flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
            }
            statusBarColor = Color.TRANSPARENT
        }

        val isRTL = isRTL(this)

        val drawerToggle = object : ActionBarDrawerToggle(
            this, binding.drawer, binding.toolbar, R.string.openDrawer, R.string.closeDrawer
        ) {
            val slidingDirection = if (isRTL) -1 else +1

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                slidingAnimation(drawerView, slideOffset / 1.5f)
            }

            private fun slidingAnimation(drawerView: View, slideOffset: Float) = binding.apply {
                appMainLayout.translationX =
                    slideOffset * drawerView.width.toFloat() * slidingDirection.toFloat()
                drawer.bringChildToFront(drawerView)
                drawer.requestLayout()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (clickedItem != 0) {
                    navigateTo(clickedItem)
                    clickedItem = 0
                }
            }
        }

        binding.drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        intent?.run {
            navigateTo(
                when (action) {
                    "COMPASS" -> R.id.compass
                    "LEVEL" -> R.id.level
                    "CONVERTER" -> R.id.converter
                    "SETTINGS" -> R.id.settings
                    "DEVICE" -> R.id.deviceInformation
                    "AZKAR" -> R.id.azkar
                    else -> R.id.calendar
                }
            )

            // So it won't happen again if the activity restarted
            action = ""
        }

        appPrefs.registerOnSharedPreferenceChangeListener(this)

        if (isShowDeviceCalendarEvents && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission(this)

        binding.navigation.setNavigationItemSelectedListener(this)

        NavigationHeaderBinding.bind(binding.navigation.getHeaderView(0))
            .seasonImage.setImageResource(run {
//                var season = (getTodayOfCalendar(CalendarType.SHAMSI).month - 1) / 3
//
//                 Southern hemisphere
//                if ((getCoordinate(this)?.latitude ?: 1.0) < .0) season = (season + 2) % 4
//
//                when (season) {
//                    0 -> R.drawable.spring
//                    1 -> R.drawable.summer
//                    2 -> R.drawable.fall
//                    else -> R.drawable.winter
//                }
                val c = getTodayOfCalendar(CalendarType.ISLAMIC)
                when {
                    c.month == 9 -> R.drawable.ramadhan
                    c.month == 10 && c.dayOfMonth in 1..3 -> R.drawable.eid
                    else -> R.drawable.drawer_background
                }
            })

//        if (appPrefs.getString(PREF_APP_LANGUAGE, null) == null &&
//            !appPrefs.getBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, false)
//        ) {
//            Snackbar.make(coordinator, "✖  Change app language?", 7000).apply {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    view.layoutDirection = View.LAYOUT_DIRECTION_LTR
//                }
//                view.setOnClickListener { dismiss() }
//                setAction("Settings") {
//                    appPrefs.edit {
//                        putString(PREF_APP_LANGUAGE, LANG_EN_US)
//                    }
//                }
//                setActionTextColor(resources.getColor(R.color.dark_accent))
//            }.show()
//            appPrefs.edit { putBoolean(CHANGE_LANGUAGE_IS_PROMOTED_ONCE, true) }
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            binding.appbarLayout.outlineProvider = null

        creationDateJdn = getTodayJdn()

        if (mainCalendar == CalendarType.SHAMSI &&
            isIranHolidaysEnabled &&
            getTodayOfCalendar(CalendarType.SHAMSI).year > supportedYearOfIranCalendar
        ) outDatedSnackbar().show()

        applyAppLanguage(this)


        //check for update
        val persianDate = PersianDate(getTodayJdn())
        if (isNetworkConnected(this) && appPrefs.getInt(PREF_LAST_UPDATE_CHECK, 1) != getDayNum(
                persianDate.month,
                persianDate.dayOfMonth
            )
        )
            UpdateChecker().execute()
        try {
            val intentFilter = IntentFilter(NAVIGATE_TO_UD)
            registerReceiver(receiverUD, intentFilter)
        } catch (ex: java.lang.Exception) {
            Log.d(TAG, "register error: $ex")
        }

        //check for overly permission
        Handler(Looper.getMainLooper()).post {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(binding.root.context)
                ) {
                    AlertDialog.Builder(binding.root.context).apply {
                        setTitle(binding.root.context.getString(R.string.requset_permision))
                        setMessage(binding.root.context.getString(R.string.need_full_screen_permision))
                        setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                            binding.root.context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + binding.root.context.packageName)
                                )
                            )
                        }
                        setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                            dialog.cancel()
                        }
                        create()
                        show()
                    }
                }
            } catch (ex: Exception) {
                Log.e(TAG, "getPermission: ", ex)
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
//            runOnUiThread { createShortcut() }

    }//end of onCreate

    @SuppressLint("PrivateResource")
    fun navigateTo(@IdRes id: Int) {
        binding.navigation.menu.findItem(
            // We don't have a menu entry for compass, so
            if (id == R.id.level) R.id.compass else id
        )?.apply {
            isCheckable = true
            isChecked = true
        }

        if (settingHasChanged) { // update when checked menu item is changed
            initUtils(this)
            update(applicationContext, true)
            settingHasChanged = false // reset for the next time
        }
        val navO = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        navController?.navigate(id, null, navO)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        settingHasChanged = true
        if (key == PREF_APP_LANGUAGE) {
            var persianDigits = false
            var changeToAfghanistanHolidays = false
            var changeToIslamicCalendar = false
            var changeToGregorianCalendar = false
            var changeToPersianCalendar = false
            var changeToIranEvents = false
            when (sharedPreferences?.getString(PREF_APP_LANGUAGE, null) ?: DEFAULT_APP_LANGUAGE) {
                LANG_EN_US -> {
                    changeToGregorianCalendar = true
                }
                LANG_JA -> {
                    changeToGregorianCalendar = true
                    persianDigits = true
                }
                LANG_AZB, LANG_GLK, LANG_FA -> {
                    persianDigits = true
                    changeToPersianCalendar = true
                    changeToIranEvents = true
                }
                LANG_EN_IR -> {
                    persianDigits = false
                    changeToPersianCalendar = true
                    changeToIranEvents = true
                }
                LANG_UR -> {
                    persianDigits = false
                    changeToGregorianCalendar = true
                }
                LANG_AR -> {
                    persianDigits = true
                    changeToIslamicCalendar = true
                }
                LANG_FA_AF -> {
                    persianDigits = true
                    changeToPersianCalendar = true
                    changeToAfghanistanHolidays = true
                }
                LANG_PS -> {
                    persianDigits = true
                    changeToPersianCalendar = true
                    changeToAfghanistanHolidays = true
                }
                else -> persianDigits = true
            }

            sharedPreferences?.edit {
                putBoolean(PREF_PERSIAN_DIGITS, persianDigits)
                // Enable Afghanistan holidays when Dari or Pashto is set
                if (changeToAfghanistanHolidays) {
                    val currentHolidays =
                        sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()

                    if (currentHolidays.isEmpty() || currentHolidays.size == 1 &&
                        "iran_holidays" in currentHolidays
                    ) putStringSet(PREF_HOLIDAY_TYPES, setOf("afghanistan_holidays"))

                }
                if (changeToIranEvents) {
                    val currentHolidays =
                        sharedPreferences.getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()

                    if (currentHolidays.isEmpty() ||
                        (currentHolidays.size == 1 && "afghanistan_holidays" in currentHolidays)
                    ) putStringSet(PREF_HOLIDAY_TYPES, setOf("iran_holidays"))
                }
                when {
                    changeToGregorianCalendar -> {
                        putString(PREF_MAIN_CALENDAR_KEY, "GREGORIAN")
                        putString(PREF_OTHER_CALENDARS_KEY, "ISLAMIC,SHAMSI")
                        putString(PREF_WEEK_START, "1")
                        putStringSet(PREF_WEEK_ENDS, setOf("1"))
                    }
                    changeToIslamicCalendar -> {
                        putString(PREF_MAIN_CALENDAR_KEY, "ISLAMIC")
                        putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,SHAMSI")
                        putString(PREF_WEEK_START, DEFAULT_WEEK_START)
                        putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
                    }
                    changeToPersianCalendar -> {
                        putString(PREF_MAIN_CALENDAR_KEY, "SHAMSI")
                        putString(PREF_OTHER_CALENDARS_KEY, "GREGORIAN,ISLAMIC")
                        putString(PREF_WEEK_START, DEFAULT_WEEK_START)
                        putStringSet(PREF_WEEK_ENDS, DEFAULT_WEEK_ENDS)
                    }
                }
            }
        }

        if (key == PREF_SHOW_DEVICE_CALENDAR_EVENTS &&
            sharedPreferences?.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true) == true
            && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission(this)

        if (key == PREF_APP_LANGUAGE || key == PREF_THEME) restartToSettings()

        if (key == PREF_NOTIFY_DATE &&
            sharedPreferences?.getBoolean(PREF_NOTIFY_DATE, true) == false
        ) {
            stopService(Intent(this, ApplicationService::class.java))
            startEitherServiceOrWorker(applicationContext)
        }
        updateStoredPreference(this)
        update(applicationContext, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALENDAR_READ_PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                toggleShowDeviceCalendarOnPreference(this, true)
                if (getCurrentDestinationId() == R.id.calendar) restartActivity()
            } else toggleShowDeviceCalendarOnPreference(this, false)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initUtils(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            binding.drawer.layoutDirection =
                if (isRTL(this)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        update(applicationContext, false)
        if (creationDateJdn != getTodayJdn()) restartActivity()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean =
        // Checking for the ancient "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawers()
            } else {
                binding.drawer.openDrawer(GravityCompat.START)
            }
            true
        } else {
            super.onKeyDown(keyCode, event)
        }

    fun restartActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    private fun restartToSettings() {
        val intent = intent
        intent?.action = "SETTINGS"
        finish()
        startActivity(intent)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.exit -> {
                finish()
            }
            R.id.quran -> {
                binding.drawer.closeDrawers()
                startActivity(Intent(this, QuranActivity::class.java))
            }
            else -> {
                binding.drawer.closeDrawers()
                clickedItem = menuItem.itemId
            }
        }
        return true
    }

    fun setTitleAndSubtitle(title: String, subtitle: String): Unit = supportActionBar?.let {
        it.title = title
        it.subtitle = subtitle
    } ?: Unit

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers()
        } else {
            val calendarFragment = supportFragmentManager
                .findFragmentByTag(CalendarFragment::class.java.name) as CalendarFragment?
            if (calendarFragment?.closeSearch() == true) return

            if (getCurrentDestinationId() == R.id.calendar)
                finish()
            else
                navigateTo(R.id.calendar)
        }
    }

    private val navController: NavController?
        get() =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment)
                ?.findNavController()

    private fun getCurrentDestinationId(): Int? = navController?.currentDestination?.id
    private fun outDatedSnackbar() =
        Snackbar.make(coordinator, getString(R.string.outdated_app), 10000).apply {
            setAction(getString(R.string.update)) {
                bringMarketPage(this@MainActivity)
            }
            getColorFromAttr(this@MainActivity, R.attr.colorAccent)
        }

    //######################################## Update Checker
    @SuppressLint("StaticFieldLeak")
    inner class UpdateChecker : AsyncTask<String?, String?, String?>() {
        override fun doInBackground(vararg strings: String?): String {
            try {
                val t = AppInfoEntity()
                val httpclient: HttpClient = DefaultHttpClient()
                val uri = "http://www.namoo.ir/Home/GetAppInfo/1"
                val httpGet = HttpGet(uri)
                val response: HttpResponse = httpclient.execute(httpGet)
                if (response.statusLine.statusCode == 200) {
                    val serverResponse = EntityUtils.toString(response.entity)
                    val parser = JSONParser()
                    val jsonArray: JSONArray = parser.parse(serverResponse) as JSONArray
                    val jsonObjectIterator = jsonArray.iterator()
                    while (jsonObjectIterator.hasNext()) {
                        val jt: JSONObject = jsonObjectIterator.next() as JSONObject
                        t.id = (jt["id"].toString().toInt())
                        t.name = (jt["name"].toString())
                        val d: String = jt["last_update"].toString()
                        val dd = StringBuilder()
                        for (c in d.toCharArray()) if (c in '0'..'9') dd.append(c)
                        val date = dd.toString().toLong()
                        t.lastUpdate = (Date(date))
                        t.lastVersion = (jt["last_version"].toString())
                    }
                    var versionInfo = ""
                    var lastUpdate: Date? = null
                    try {
                        val pInfo: PackageInfo =
                            packageManager.getPackageInfo(packageName, 0)
                        versionInfo = pInfo.versionName
                        lastUpdate = Date(pInfo.lastUpdateTime)
                    } catch (ex: java.lang.Exception) {
                        Log.d(TAG, "Error : $ex")
                    }
                    if (versionInfo != t.lastVersion && lastUpdate != null && lastUpdate.before(t.lastUpdate)) {
                        val channel: NotificationChannel?
                        val notificationManager: NotificationManager? =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            channel = NotificationChannel(
                                1002.toString(),
                                getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH
                            )
                            channel.setShowBadge(true)
                            notificationManager?.createNotificationChannel(channel)
                        }
                        // pending implicit intent to view url
                        val resultIntent = Intent(Intent.ACTION_VIEW)
                        resultIntent.data = Uri.parse(appLink)
                        val pending: PendingIntent = PendingIntent.getActivity(
                            applicationContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        val mBuilder: NotificationCompat.Builder =
                            NotificationCompat.Builder(applicationContext, "1002")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(
                                    RLM.toString() + "" + resources.getString(
                                        R.string.app_name
                                    )
                                )
                                .setContentText(
                                    RLM.toString() + "" + resources.getString(
                                        R.string.update_available
                                    )
                                )
                                .setContentIntent(pending)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        notificationManager?.notify(1002, mBuilder.build())
                    }
                }
            } catch (ex: java.lang.Exception) {
                return "Error"
            }
            //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4 check if new version
            return "OK"
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            if (!s.isNullOrEmpty()) {
                val persianDate = PersianDate(getTodayJdn())
                appPrefs.edit {
                    putInt(
                        PREF_LAST_UPDATE_CHECK,
                        getDayNum(persianDate.month, persianDate.dayOfMonth)
                    )
                }
            }
        }
    } //end of class Update Checker

    class AppInfoEntity {
        var id = 0
        var name: String? = null
        var lastVersion: String? = null
        var lastUpdate: Date? = null

        override fun toString(): String {
            return "{id: " + id +
                    " , \"name\": \"" + name +
                    "\" , \"last_version\": \"" + lastVersion +
                    "\" , \"last_update\": \"" + lastUpdate +
                    "\" " +
                    "}"
        }
    } //end of class

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(receiverUD)
        } catch (e: Exception) {
        }
    }

//    @TargetApi(25)
//    private fun createShortcut() {
//        val shortcutManager = getSystemService(ShortcutManager::class.java)
//        val intent1 = Intent(applicationContext, MainActivity::class.java)
//        intent1.action = "AZKAR"
//
//        val azkarShortcut = ShortcutInfo.Builder(this, "azkar")
//            .setIntent(intent1)
//            .setShortLabel(getString(R.string.azkar))
//            .setLongLabel(getString(R.string.azkar))
//            .setIcon(Icon.createWithResource(this, R.drawable.ic_azkar2))
//            .build()
//        if (!shortcutManager.dynamicShortcuts.contains(azkarShortcut))
//            shortcutManager.dynamicShortcuts.add(azkarShortcut)
//    }
}//end of MainActivity

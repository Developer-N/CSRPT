package ir.namoo.quran.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.core.view.iterator
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import ir.namoo.quran.ui.fragments.chapter.ChapterFragment
import ir.namoo.quran.utils.ACTION_CHANGE_SURA
import ir.namoo.quran.utils.ACTION_GO_TO_DOWNLOAD_PAGE
import ir.namoo.quran.utils.initQuranUtils
import ir.namoo.religiousprayers.LANG_EN_US
import ir.namoo.religiousprayers.LANG_JA
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.ActivityQuranBinding
import ir.namoo.religiousprayers.databinding.NavigationHeaderBinding
import ir.namoo.religiousprayers.utils.*


class QuranActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityQuranBinding
    private var clickedItem = 0
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))
        initQuranUtils(this)
        applyAppLanguage(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        if (language !in listOf(LANG_EN_US, LANG_JA))
            overrideFont("SANS_SERIF", getAppFont(applicationContext))
        binding = ActivityQuranBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        setSupportActionBar(binding.quranToolbar)
        changeToolbarTypeface(binding.quranToolbar)
        changeNavigationItemTypeface(binding.quranNavigation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) window.apply {
            // https://learnpainless.com/android/material/make-fully-android-transparent-status-bar
            attributes = attributes.apply {
                flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
            }
            statusBarColor = Color.TRANSPARENT
        }
        val isRTL = isRTL(this)

        val drawerToggle = object : ActionBarDrawerToggle(
            this,
            binding.quranDrawer,
            binding.quranToolbar,
            R.string.openDrawer,
            R.string.closeDrawer
        ) {
            val slidingDirection = if (isRTL) -1 else +1

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                slidingAnimation(drawerView, slideOffset / 1.5f)
            }

            private fun slidingAnimation(drawerView: View, slideOffset: Float) = binding.apply {
                quranMainLayout.translationX =
                    slideOffset * drawerView.width.toFloat() * slidingDirection.toFloat()
                quranDrawer.bringChildToFront(drawerView)
                quranDrawer.requestLayout()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (clickedItem != 0) {
                    navigateTo(clickedItem)
                    clickedItem = 0
                }
            }
        }

        binding.quranDrawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.quranNavigation.setNavigationItemSelectedListener(this)
        NavigationHeaderBinding.bind(binding.quranNavigation.getHeaderView(0))
            .seasonImage.setImageResource(run {
                R.drawable.quran_drawer2
            })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            binding.quranbarLayout.outlineProvider = null
        intent?.run {
            navigateTo(
                when (action) {
                    "SETTINGS" -> R.id.quran_setting
                    else -> R.id.quran_chapter
                }
            )
            action = ""
        }
        applyAppLanguage(this)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
//            Handler(Looper.getMainLooper()).postDelayed(1000) {
//                createShortcut()
//            }
        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ register receiver for change sura
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    if (intent.action == ACTION_GO_TO_DOWNLOAD_PAGE) {
                        this@QuranActivity.intent.putExtra("sura", intent.extras?.getInt("sura"))
                        this@QuranActivity.intent.putExtra(
                            "folder",
                            intent.extras?.getString("folder")
                        )
                        Handler(Looper.getMainLooper()).postDelayed(1000) {
                            navigateTo(R.id.quran_download_manager)
                        }
                    } else
                        try {
                            val sura: Int = intent.extras!!.getInt("sura")
                            val aya: Int = intent.extras!!.getInt("aya")
                            val sIntent =
                                Intent(applicationContext, SuraViewActivity::class.java)
                            sIntent.putExtra("sura", sura)
                            sIntent.putExtra("aya", aya)
                            startActivity(sIntent)
                        } catch (ex: Exception) {
                            Log.e(TAG, "receiver error : $ex")
                        }
                }

            }
        }

        registerReceiver(receiver, IntentFilter().apply {
            addAction(ACTION_CHANGE_SURA)
            addAction(ACTION_GO_TO_DOWNLOAD_PAGE)
        })
    }//end of onCreate

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initUtils(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            binding.quranDrawer.layoutDirection =
                if (isRTL(this)) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        }
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
    }

//    fun restartActivity() {
//        val intent = intent
//        finish()
//        startActivity(intent)
//    }
//
//    private fun restartToSettings() {
//        val intent = intent
//        intent?.action = "SETTINGS"
//        finish()
//        startActivity(intent)
//    }

    @SuppressLint("PrivateResource")
    fun navigateTo(@IdRes id: Int) {
        binding.quranNavigation.menu.findItem(id)?.apply {
            isCheckable = true
            isChecked = true
        }
        val navO = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
        navController?.navigate(id, null, navO)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.quran_create_shortcut -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val shortcutManager = getSystemService(ShortcutManager::class.java)
                    if (shortcutManager.isRequestPinShortcutSupported) {
                        val intent1 = Intent(applicationContext, QuranActivity::class.java)
                        intent1.action = Intent.ACTION_VIEW
                        val quranShortcut = ShortcutInfo.Builder(this, "quran")
                            .setIntent(intent1)
                            .setShortLabel(getString(R.string.quran))
                            .setLongLabel(getString(R.string.quran))
                            .setIcon(Icon.createWithResource(this, R.drawable.ic_quran))
                            .build()
                        shortcutManager.requestPinShortcut(quranShortcut, null)
                        binding.quranDrawer.closeDrawers()
                    } else {
                        snackMessage(binding.root, getString(R.string.pin_not_supported))
                        binding.quranDrawer.closeDrawers()
                        return true
                    }
                } else {
                    val shortcutIntent = Intent(
                        applicationContext,
                        QuranActivity::class.java
                    )
                    shortcutIntent.action = Intent.ACTION_MAIN
                    val addIntent = Intent()
                    addIntent
                        .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.quran))
                    addIntent.putExtra(
                        Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(
                            applicationContext,
                            R.drawable.ic_quran
                        )
                    )
                    addIntent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
                    addIntent.putExtra(
                        "duplicate",
                        false
                    ) //may it's already there so   don't duplicate
                    applicationContext.sendBroadcast(addIntent)

                    binding.quranDrawer.closeDrawers()
                    snackMessage(binding.root, getString(R.string.shortcut_created))
                }

            }
            else -> {
                binding.quranDrawer.closeDrawers()
                clickedItem = item.itemId
            }
        }
        return true
    }

//    @TargetApi(25)
//    private fun createShortcut() {
//        val shortcutManager = getSystemService(ShortcutManager::class.java)
//        val intent1 = Intent(applicationContext, QuranActivity::class.java)
//        intent1.action = Intent.ACTION_VIEW
//
//        val quranShortcut = ShortcutInfo.Builder(this, "quran")
//            .setIntent(intent1)
//            .setShortLabel(getString(R.string.quran))
//            .setLongLabel(getString(R.string.quran))
//            .setIcon(Icon.createWithResource(this, R.drawable.ic_quran))
//            .build()
//        if (!shortcutManager.dynamicShortcuts.contains(quranShortcut))
//            shortcutManager.dynamicShortcuts.add(quranShortcut)
//    }

    fun setTitleAndSubtitle(title: String, subtitle: String): Unit = supportActionBar?.let {
        it.title = title
        it.subtitle = subtitle
    } ?: Unit

    override fun onBackPressed() {
        if (binding.quranDrawer.isDrawerOpen(GravityCompat.START)) {
            binding.quranDrawer.closeDrawers()
        } else {
            val chapterFragment = supportFragmentManager
                .findFragmentByTag(ChapterFragment::class.java.name) as ChapterFragment?
            if (chapterFragment?.closeSearch() == true) return

            if (getCurrentDestinationId() != R.id.quran_chapter)
                navigateTo(R.id.quran_chapter)
            else
                finish()
        }
    }

    private val navController: NavController?
        get() =
            (supportFragmentManager.findFragmentById(R.id.quran_nav_host_fragment) as? NavHostFragment)
                ?.findNavController()

    private fun getCurrentDestinationId(): Int? = navController?.currentDestination?.id

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    fun enableDrawerMenu(enable: Boolean) {
        binding.quranNavigation.menu.let {
            it.iterator().forEach { item -> item.isEnabled = enable }
        }
    }

}//end of QuranActivity
package ir.namoo.quran.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.postDelayed
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.viewpager2.widget.MarginPageTransformer
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityQuranBinding
import com.byagowi.persiancalendar.databinding.NavigationHeaderBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.DrawerHost
import com.byagowi.persiancalendar.ui.utils.SystemBarsTransparency
import com.byagowi.persiancalendar.ui.utils.dp
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.navigation.NavigationView
import ir.namoo.commons.utils.CustomTypefaceSpan
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.getAppFont
import ir.namoo.commons.utils.overrideFont
import ir.namoo.commons.utils.snackMessage
import ir.namoo.quran.ui.fragments.ChapterFragmentDirections
import ir.namoo.quran.utils.ACTION_CHANGE_SURA
import ir.namoo.quran.utils.ACTION_GO_TO_DOWNLOAD_PAGE
import ir.namoo.quran.utils.initQuranUtils
import kotlin.math.roundToInt

class QuranActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NavController.OnDestinationChangedListener, DrawerHost {
    //end of class QuranActivity
    //end of QuranActivity
    private lateinit var binding: ActivityQuranBinding
    private lateinit var receiver: BroadcastReceiver

    private val onBackPressedCloseDrawerCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = binding.root.closeDrawer(GravityCompat.START)
    }
    private var creationDateJdn = Jdn.today()
    private val exitId = View.generateViewId()
    private val createShortcutID = 114
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        transparentSystemBars()

        initQuranUtils(this, appPrefsLite)
        onBackPressedDispatcher.addCallback(this, onBackPressedCloseDrawerCallback)
        overrideFont("SANS_SERIF", getAppFont(applicationContext))
        binding = ActivityQuranBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }
        ensureDirectionality()

        binding.root.addDrawerListener(createDrawerListener())
        val typeface = getAppFont(this)
        listOf(
            Triple(R.id.quran_chapter, R.drawable.ic_list, R.string.chapter),
            Triple(R.id.quran_search, R.drawable.ic_search, R.string.search_the_whole_quran),
            Triple(R.id.quran_notes, R.drawable.ic_note, R.string.notes),
            Triple(R.id.quran_bookmarks, R.drawable.ic_bookmark1, R.string.bookmarks),
            Triple(R.id.quran_download_manager, R.drawable.ic_download, R.string.download_audios),
            Triple(R.id.quran_setting, R.drawable.ic_settings, R.string.settings),
            Triple(createShortcutID, R.drawable.ic_shortcut, R.string.create_shortcut),
            Triple(exitId, R.drawable.ic_cancel, R.string.exit)
        ).forEach { (id, icon, title) ->
            val spannable = SpannableString(getString(title)).apply {
                setSpan(
                    CustomTypefaceSpan("", typeface), 0, length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.quranNavigation.menu.add(Menu.NONE, id, Menu.NONE, spannable).setIcon(icon)
        }
        binding.quranNavigation.setNavigationItemSelectedListener(this)
        navHostFragment?.navController?.addOnDestinationChangedListener(this)
//        NavigationHeaderBinding.bind(binding.quranNavigation.getHeaderView(0))
//            .seasonImage.setImageResource(run {
//                R.drawable.quran_drawer2
//            })
        NavigationHeaderBinding.bind(binding.quranNavigation.getHeaderView(0)).seasonsPager.also {
            it.adapter = QuranDrawerAdapter()
            it.currentItem = 0
            it.setPageTransformer(MarginPageTransformer((8 * resources.dp).toInt()))
        }
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }
            val transparencyState = SystemBarsTransparency(this@QuranActivity)
            binding.quranNavigation.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = if (transparencyState.shouldStatusBarBeTransparent) 0 else insets.top
                bottomMargin =
                    if (transparencyState.shouldNavigationBarBeTransparent) 0 else insets.bottom
            }
            NavigationHeaderBinding.bind(binding.quranNavigation.getHeaderView(0))
                .statusBarPlaceHolder.let { placeHolder ->
                    placeHolder.updateLayoutParams {
                        this@updateLayoutParams.height =
                            if (transparencyState.shouldStatusBarBeTransparent) insets.top else 0
                    }
                    placeHolder.isInvisible = !transparencyState.needsVisibleStatusBarPlaceHolder
                }
            windowInsets
        }
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
                        runCatching {
                            val sura: Int = intent.extras!!.getInt("sura")
                            val aya: Int = intent.extras!!.getInt("aya")
                            val sIntent =
                                Intent(applicationContext, SuraViewActivity::class.java)
                            sIntent.putExtra("sura", sura)
                            sIntent.putExtra("aya", aya)
                            startActivity(sIntent)
                        }.onFailure(logException)
                }

            }
        }

        registerReceiver(receiver, IntentFilter().apply {
            addAction(ACTION_CHANGE_SURA)
            addAction(ACTION_GO_TO_DOWNLOAD_PAGE)
        })
    }//end of onCreate

    private fun ensureDirectionality() {
        binding.root.layoutDirection =
            if (language.isArabicScript) View.LAYOUT_DIRECTION_RTL // just in case resources isn't correct
            else resources.configuration.layoutDirection
    }

    private val navHostFragment by lazy {
        (supportFragmentManager.findFragmentById(R.id.quranNavHostFragment) as? NavHostFragment)
            .debugAssertNotNull
    }

    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        binding.quranNavigation.menu.findItem(destination.id)?.also {
            it.isCheckable = true
            it.isChecked = true
        }
    }

    fun navigateTo(@IdRes id: Int) {
        navHostFragment?.navController?.navigate(id, null,
            navOptions {
                anim {
                    enter = R.anim.nav_enter_anim
                    exit = R.anim.nav_exit_anim
                    popEnter = R.anim.nav_enter_anim
                    popExit = R.anim.nav_exit_anim
                }
            })
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyAppLanguage(this)
        ensureDirectionality()
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
        val today = Jdn.today()
        if (creationDateJdn != today) {
            creationDateJdn = today
            val navController = navHostFragment?.navController
            if (navController?.currentDestination?.id == R.id.quran_chapter) {
                navController.navigateSafe(ChapterFragmentDirections.navigateToSelf())
            }
        }
    }

    private var clickedItem = 0

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (val itemId = item.itemId) {
            exitId -> finish()
            createShortcutID -> {
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
                        binding.root.closeDrawers()
                    } else {
                        snackMessage(binding.root, getString(R.string.pin_not_supported))
                        binding.root.closeDrawers()
                    }
                } else {
                    val shortcutIntent = Intent(
                        applicationContext,
                        QuranActivity::class.java
                    )
                    shortcutIntent.action = Intent.ACTION_MAIN
                    val addIntent = Intent().apply {
                        putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                        putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.quran))
                        putExtra(
                            Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource
                                .fromContext(applicationContext, R.drawable.ic_quran)
                        )
                        action = "com.android.launcher.action.INSTALL_SHORTCUT"
                        putExtra("duplicate", false) //may it's already there so   don't duplicate
                    }
                    applicationContext.sendBroadcast(addIntent)

                    binding.root.closeDrawer(GravityCompat.START)
                    snackMessage(binding.root, getString(R.string.shortcut_created))
                }

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

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }.onFailure(logException)
        super.onDestroy()
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
            binding.quranNavHostFragment.translationX =
                slideOffset * drawerView.width.toFloat() * slidingDirection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurs.isNotEmpty()) {
                binding.quranNavHostFragment.setRenderEffect(
                    blurs[((blurs.size - 1) * slideOffset).roundToInt()]
                )
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

    // Checking for the ancient "menu" key
    override fun onKeyDown(keyCode: Int, event: KeyEvent?) = when (keyCode) {
        KeyEvent.KEYCODE_MENU -> {
            if (binding.root.isDrawerOpen(GravityCompat.START))
                binding.root.closeDrawer(GravityCompat.START)
            else
                binding.root.openDrawer(GravityCompat.START)
            true
        }

        else -> super.onKeyDown(keyCode, event)
    }


}

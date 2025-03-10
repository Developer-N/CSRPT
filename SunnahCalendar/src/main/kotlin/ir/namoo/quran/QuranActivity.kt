package ir.namoo.quran

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.initGlobal
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.utils.applyAppLanguage
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.toastMessage
import ir.namoo.quran.home.DownloadQuranDBScreen
import ir.namoo.quran.home.QuranDownloadViewModel
import ir.namoo.quran.home.QuranHomeScreen
import ir.namoo.quran.qari.getQariFolder
import ir.namoo.quran.utils.EXTRA_AYA
import ir.namoo.quran.utils.EXTRA_SURA
import ir.namoo.quran.utils.initQuranUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import java.io.File

class QuranActivity : ComponentActivity() {
    private val viewModel: QuranActivityViewModel = get()
    private val downloadViewModel: QuranDownloadViewModel = get()

    @SuppressLint("SdCardPath")
    override fun onCreate(savedInstanceState: Bundle?) {
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)
        setTheme(R.style.BaseTheme)
        applyAppLanguage(this)

        super.onCreate(savedInstanceState)

        initGlobal(this)
        initQuranUtils(this)
        viewModel.checkDBAndQari(packageName)
        val startSura = intent.getIntExtra(EXTRA_SURA, -1)
        val startAya = intent.getIntExtra(EXTRA_AYA, -1)
        setContent {
            AppTheme {
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }
                val isDBExist by viewModel.isDBExist.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val qariList by viewModel.qariList.collectAsState()

                AnimatedVisibility(visible = isLoading) {
                    Surface {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp),
                                strokeWidth = 8.dp,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
                if (!isLoading) if (!isDBExist) {
                    DownloadQuranDBScreen(download = {
                        if (isNetworkConnected(this)) {
                            downloadViewModel.download(this) {
                                viewModel.checkDBAndQari(packageName)
                            }
                        } else {
                            toastMessage(getString(R.string.network_error_message))
                        }
                    }, downloadViewModel)
                } else if (qariList.isEmpty()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                text = stringResource(id = R.string.network_error_message),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            )
                            Button(modifier = Modifier.padding(4.dp), onClick = { onResume() }) {
                                Text(
                                    text = stringResource(id = R.string.str_retry),
                                    fontSize = 20.sp
                                )

                                Icon(
                                    modifier = Modifier.padding(vertical = 0.dp, horizontal = 8.dp),
                                    imageVector = Icons.Filled.Autorenew,
                                    contentDescription = stringResource(id = R.string.str_retry)
                                )
                            }
                        }
                    }
                } else {
                    QuranHomeScreen(
                        startSura = startSura,
                        startAya = startAya,
                        exit = { finish() },
                        createShortcut = { createShortcut() },
                        checkFiles = { viewModel.checkDBAndQari(packageName) }
                    )
                }
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val noMediaFile = File("${getQariFolder()}/.nomedia")
            if (!noMediaFile.exists()) noMediaFile.createNewFile()
        }

        // There is a window:enforceNavigationBarContrast set to false in styles.xml as the following
        // isn't as effective in dark themes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }//end of onCreate

    @Suppress("DEPRECATION")
    private fun createShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                val intent1 = Intent(applicationContext, QuranActivity::class.java)
                intent1.action = Intent.ACTION_VIEW
                val quranShortcut = ShortcutInfo.Builder(this, "quran").setIntent(intent1)
                    .setShortLabel(getString(R.string.quran))
                    .setLongLabel(getString(R.string.quran)).setIcon(
                        android.graphics.drawable.Icon.createWithResource(
                            this, R.drawable.ic_quran
                        )
                    ).build()
                shortcutManager.requestPinShortcut(quranShortcut, null)
            } else {
                toastMessage(getString(R.string.pin_not_supported))
            }
        } else {
            val shortcutIntent = Intent(
                applicationContext, QuranActivity::class.java
            )
            shortcutIntent.action = Intent.ACTION_MAIN
            val addIntent = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.quran))
                putExtra(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(applicationContext, R.drawable.ic_quran)
                )
                action = "com.android.launcher.action.INSTALL_SHORTCUT"
                putExtra("duplicate", false) //may it's already there so   don't duplicate
            }
            applicationContext.sendBroadcast(addIntent)
            toastMessage(getString(R.string.shortcut_created))
        }
    }

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        val statusBarStyle =
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            statusBarStyle,
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge(
            statusBarStyle,
            // Just don't tweak navigation bar in older Android versions, leave it to default
        )
    }
}//end of QuranActivity

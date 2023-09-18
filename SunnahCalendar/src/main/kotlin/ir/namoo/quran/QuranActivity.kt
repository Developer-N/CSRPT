package ir.namoo.quran

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.initUtils
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.toastMessage
import ir.namoo.quran.home.DownloadQuranDBScreen
import ir.namoo.quran.home.QuranDownloadViewModel
import ir.namoo.quran.home.QuranHomeScreen
import ir.namoo.quran.utils.initQuranUtils
import org.koin.android.ext.android.get

class QuranActivity : AppCompatActivity() {

    private val viewModel: QuranActivityViewModel = get()
    private val downloadViewModel: QuranDownloadViewModel = get()

    @SuppressLint("SdCardPath")
    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        super.onCreate(savedInstanceState)
        transparentSystemBars()
        initUtils(this)
        initQuranUtils(this)
        setContent {
            Mdc3Theme {
                val isDBExist by viewModel.isDBExist.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val qariList by viewModel.qariList.collectAsState()

                AnimatedVisibility(visible = isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            strokeWidth = 8.dp,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
                if (!isLoading)
                    if (!isDBExist) {
                        DownloadQuranDBScreen(download = {
                            if (isNetworkConnected(this)) {
                                downloadViewModel.download(this, ::onResume)
                            } else {
                                MaterialAlertDialogBuilder(this).setTitle(resources.getString(R.string.network_error_title))
                                    .setMessage(resources.getString(R.string.network_error_message))
                                    .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                                        dialog.dismiss()
                                    }.show()
                            }
                        }, downloadViewModel)
                    } else if (qariList.isEmpty()) {
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
                                fontFamily = FontFamily(appFont),
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp
                            )
                            Button(modifier = Modifier.padding(4.dp), onClick = { onResume() }) {
                                Text(
                                    text = stringResource(id = R.string.str_retry),
                                    fontFamily = FontFamily(appFont),
                                    fontSize = 20.sp
                                )

                                Icon(
                                    modifier = Modifier.padding(vertical = 0.dp, horizontal = 8.dp),
                                    imageVector = Icons.Filled.Autorenew,
                                    contentDescription = stringResource(id = R.string.str_retry)
                                )
                            }
                        }
                    } else {
                        QuranHomeScreen(
                            exit = { finish() },
                            createShortcut = { createShortcut() },
                            ::onResume
                        )
                    }
            }
        }
    }//end of onCreate

    override fun onResume() {
        super.onResume()
        viewModel.checkDBAndQari(packageName)
    }

    @Suppress("DEPRECATION")
    private fun createShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager.isRequestPinShortcutSupported) {
                val intent1 = Intent(applicationContext, QuranActivity::class.java)
                intent1.action = Intent.ACTION_VIEW
                val quranShortcut = ShortcutInfo.Builder(this, "quran").setIntent(intent1)
                    .setShortLabel(getString(R.string.quran))
                    .setLongLabel(getString(R.string.quran))
                    .setIcon(
                        android.graphics.drawable.Icon.createWithResource(
                            this,
                            R.drawable.ic_quran
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

}//end of QuranActivity

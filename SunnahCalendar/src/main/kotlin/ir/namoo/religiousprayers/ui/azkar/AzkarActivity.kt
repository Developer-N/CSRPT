package ir.namoo.religiousprayers.ui.azkar

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.isSystemInDarkTheme
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.applyAppLanguage
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.quran.utils.KeepScreenOn
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class AzkarActivity : ComponentActivity() {

    private val viewModel: AzkarActivityViewModel by viewModel()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            if (isSystemInDarkTheme(resources.configuration)) SystemBarStyle.dark(Color.TRANSPARENT)
            else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        applyAppLanguage(this)
        super.onCreate(savedInstanceState)

        val chapterID = intent.extras?.getInt("chapterID") ?: -1
        if (chapterID == -1) finish()
        val arabicFont = Typeface.createFromAsset(assets, "fonts/Quran_Bahij_Bold.ttf")
        val fileLocation = getExternalFilesDir("azkar")?.absolutePath + File.separator
        val dir = File(fileLocation)
        viewModel.loadItems(chapterID, fileLocation)
        if (!dir.exists()) dir.mkdirs()
        setContent {
            AppTheme {
                val state = rememberLazyListState()
                val isLoading by viewModel.isLoading.collectAsState()
                val azkarLang by viewModel.azkarLang.collectAsState()
                val chapter by viewModel.chapter.collectAsState()
                val azkarItems = viewModel.azkarItems
                val azkarReferences = viewModel.azkarReferences
                val itemsState = viewModel.itemsState
                val currentPlayingItem by viewModel.currentPlayingItem.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val duration by viewModel.totalDuration.collectAsState()
                val currentPosition by viewModel.currentPosition.collectAsState()

                val clipboard = LocalClipboard.current

                Scaffold(topBar = {
                    TopAppBar(title = {
                        Text(
                            modifier = Modifier.basicMarquee(), text = when (azkarLang) {
                                Language.FA.code -> chapter?.persian
                                Language.CKB.code -> chapter?.kurdish
                                else -> chapter?.arabic
                            } ?: "-"
                        )
                    }, colors = appTopAppBarColors(), navigationIcon = {
                        AppIconButton(
                            icon = Icons.AutoMirrored.Default.ArrowBack, title = stringResource(
                                id = R.string.close
                            )
                        ) {
                            finish()
                        }
                    }, actions = {
                        AppIconButton(
                            title = stringResource(id = R.string.share), onClick = {
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.type = "text/plain"
                                intent.putExtra(Intent.EXTRA_TEXT, viewModel.description)
                                startActivity(
                                    Intent.createChooser(
                                        intent, resources.getString(R.string.share)
                                    )
                                )
                            }, icon = Icons.Default.Share
                        )
                        AppIconButton(
                            title = stringResource(id = R.string.copy), onClick = {
                                lifecycleScope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                getString(R.string.azkar), viewModel.description
                                            )
                                        )
                                    )
                                }
                            }, icon = Icons.Default.CopyAll
                        )

                    })
                }) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding()),
                        shape = materialCornerExtraLargeTop()
                    ) {
                        KeepScreenOn()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            AnimatedVisibility(visible = isLoading) {
                                LoadingUIElement()
                            }
                            if (azkarItems.isNotEmpty() && azkarReferences.isNotEmpty() && itemsState.isNotEmpty())
                                LazyColumn(state = state) {
                                    items(items = azkarItems, key = { zkr -> zkr.id }) { item ->
                                        AzkarItemUIElement(
                                            item = item,
                                            reference = azkarReferences[azkarItems.indexOf(
                                                item
                                            )],
                                            lang = azkarLang,
                                            arabicTypeface = arabicFont,
                                            itemState = itemsState[azkarItems.indexOf(item)],
                                            isPlaying = isPlaying,
                                            isPlayingCurrentItem = currentPlayingItem == item.id,
                                            duration = duration,
                                            currentPosition = currentPosition,
                                            play = {
                                                viewModel.play(this@AzkarActivity, item)
                                            },
                                            pause = { viewModel.pause() },
                                            resume = { viewModel.resume() },
                                            seekTo = { viewModel.seekTo(it) },
                                            stop = { viewModel.stop() },
                                            download = {
                                                if (isNetworkConnected(this@AzkarActivity))
                                                    viewModel.downloadSound(item)
                                                else
                                                    Toast.makeText(
                                                        this@AzkarActivity,
                                                        getString(R.string.network_error_message),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                            }, delete = { viewModel.delete(item) },
                                            addReadCount = { viewModel.addReadCount(item) },
                                            resetReadCount = { viewModel.resetReadCount(item) })
                                    }
                                }
                        }
                    }
                }

            }
        }
    }//end of onCreate

    override fun onPause() {
        viewModel.stop()
        super.onPause()
    }
}//end of class

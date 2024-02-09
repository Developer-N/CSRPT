package ir.namoo.religiousprayers.ui.azkar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.ui.common.AppIconButton
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.isLight
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.quran.utils.KeepScreenOn
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class AzkarActivity : ComponentActivity() {

    private val viewModel: AzkarActivityViewModel by viewModel()
    private var mediaPlayer: MediaPlayer? = null

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Just to make sure we have an initial transparent system bars
        // System bars are tweaked later with project's with real values
        applyEdgeToEdge(isBackgroundColorLight = false, isSurfaceColorLight = true)

        setTheme(R.style.BaseTheme)
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
                val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
                val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
                LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
                    applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
                }
                val state = rememberLazyListState()
                val isLoading by viewModel.isLoading.collectAsState()
                val azkarLang by viewModel.azkarLang.collectAsState()
                val chapter by viewModel.chapter.collectAsState()
                val azkarItems by viewModel.azkarItems.collectAsState()
                val azkarReferences by viewModel.azkarReferences.collectAsState()
                val itemsState by viewModel.itemsState.collectAsState()
                val lastPlay by viewModel.lastPlay.collectAsState()
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
                                val clipboard =
                                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip: ClipData = ClipData.newPlainText(
                                    getString(R.string.azkar), viewModel.description
                                )
                                clipboard.setPrimaryClip(clip)
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
                            LazyColumn(state = state) {
                                if (azkarItems.isNotEmpty() && azkarReferences.isNotEmpty() && itemsState.isNotEmpty()) items(
                                    items = azkarItems,
                                    key = { zkr -> zkr.id }) { item ->

                                    val fileName = item.sound + ".mp3"
                                    val mp3File = File(fileLocation + fileName)

                                    AzkarItemUIElement(item = item,
                                        reference = azkarReferences[azkarItems.indexOf(
                                            item
                                        )],
                                        lang = azkarLang,
                                        arabicTypeface = arabicFont,
                                        itemState = itemsState[azkarItems.indexOf(item)],
                                        play = {
                                            if (lastPlay != -1) stop()
                                            viewModel.play(item.id)
                                            play(mp3File, item.id)
                                        },
                                        stop = {
                                            viewModel.stop(item.id)
                                            stop()
                                        },
                                        download = {
                                            if (isNetworkConnected(this@AzkarActivity)) viewModel.downloadSound(
                                                item.id
                                            )
//                                    else
//                                        snackMessage(
//                                            binding.root,
//                                            getString(R.string.network_error_message)
//                                        )
                                        }) {
                                        viewModel.delete(item.id)
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }//end of onCreate

    private fun applyEdgeToEdge(isBackgroundColorLight: Boolean, isSurfaceColorLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) enableEdgeToEdge(
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
            if (isSurfaceColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT),
        ) else enableEdgeToEdge( // Just don't tweak navigation bar in older Android versions
            if (isBackgroundColorLight) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            else SystemBarStyle.dark(Color.TRANSPARENT)
        )
    }

    private fun play(mp3File: File, id: Int) {
        runCatching {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(applicationContext, Uri.fromFile(mp3File))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                viewModel.stop(id)
                stop()
            }
        }.onFailure(logException)
    }

    private fun stop() {
        runCatching {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }.onFailure(logException)
    }

    override fun onPause() {
        stop()
        super.onPause()
    }
}//end of class

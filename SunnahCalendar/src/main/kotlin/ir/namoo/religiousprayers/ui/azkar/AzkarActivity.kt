package ir.namoo.religiousprayers.ui.azkar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAzkarBinding
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.logException
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_LANG
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.getAppFont
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.snackMessage
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import org.koin.android.ext.android.get
import java.io.File

class AzkarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAzkarBinding

    private val azkarActivityViewModel: AzkarActivityViewModel = get()
    private lateinit var fileLocation: String
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resolveColor(android.R.attr.colorPrimaryDark)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        findViewById<View>(android.R.id.content).transitionName = "shared_element_container"
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }
        super.onCreate(savedInstanceState)

        binding = ActivityAzkarBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        setupMenu(binding.appBar.toolbar)
        binding.appBar.root.hideToolbarBottomShadow()

        val chapterID = intent.extras?.getInt("chapterID") ?: -1
        if (chapterID == -1) finish()
        val arabicFont = Typeface.createFromAsset(assets, "fonts/arabic_UthmanTahaN1B.ttf")
        azkarActivityViewModel.setLang(
            appPrefsLite.getString(PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG) ?: DEFAULT_AZKAR_LANG
        )
        azkarActivityViewModel.loadItems(chapterID)
        fileLocation =
            getExternalFilesDir(null)?.absolutePath + File.separator + "azkar" + File.separator
        val dir = File(fileLocation)
        if (!dir.exists()) dir.mkdirs()
        binding.composeView.setContent {
            Mdc3Theme {
                val appFont = remember { getAppFont(this@AzkarActivity) }
                val normalTextColor = remember { Color(resolveColor(R.attr.colorTextNormal)) }
                val iconColor = remember { Color(resolveColor(R.attr.colorIcon)) }
                val cardColor = remember { Color(resolveColor(R.attr.colorCard)) }
                val state = rememberLazyListState()
                if (!azkarActivityViewModel.isLoading) binding.appBar.toolbar.title =
                    when (azkarActivityViewModel.azkarLang) {
                        Language.FA.code -> azkarActivityViewModel.chapter?.persian
                        Language.CKB.code -> azkarActivityViewModel.chapter?.kurdish
                        else -> azkarActivityViewModel.chapter?.arabic
                    }
                Column {
                    AnimatedVisibility(visible = azkarActivityViewModel.isLoading) {
                        LoadingUIElement(typeface = appFont, normalTextColor = normalTextColor)
                    }
                    LazyColumn(state = state) {
                        if (azkarActivityViewModel.azkarItems.isNotEmpty() && azkarActivityViewModel.azkarReferences.isNotEmpty() && azkarActivityViewModel.itemsState.isNotEmpty()) items(
                            items = azkarActivityViewModel.azkarItems,
                            key = { it.id }) { item ->

                            val fileName = item.sound + ".mp3"
                            val mp3File = File(fileLocation + fileName)

                            AzkarItemUIElement(item = item,
                                reference = azkarActivityViewModel.azkarReferences[azkarActivityViewModel.azkarItems.indexOf(
                                    item
                                )],
                                lang = azkarActivityViewModel.azkarLang,
                                cardColor = cardColor,
                                textColor = normalTextColor,
                                iconColor = iconColor,
                                typeface = appFont,
                                arabicTypeface = arabicFont,
                                soundFile = mp3File,
                                itemState = azkarActivityViewModel.itemsState[azkarActivityViewModel.azkarItems.indexOf(
                                    item
                                )],
                                play = {
                                    if (azkarActivityViewModel.lastPlay != -1)
                                        stop()
                                    azkarActivityViewModel.play(item.id)
                                    play(mp3File, item.id)
                                },
                                stop = {
                                    azkarActivityViewModel.stop(item.id)
                                    stop()
                                },
                                download = {
                                    if (isNetworkConnected(this@AzkarActivity))
                                        azkarActivityViewModel.downloadSound(
                                            item.sound ?: "",
                                            mp3File,
                                            item.id
                                        )
                                    else
                                        snackMessage(
                                            binding.root,
                                            getString(R.string.network_error_message)
                                        )
                                })
                        }
                    }
                }
            }
        }


    }//end of onCreate

    private fun setupMenu(toolbar: MaterialToolbar) {
        toolbar.navigationIcon =
            AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.menu.add(R.string.share)?.also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_share)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, azkarActivityViewModel.description)
                startActivity(
                    Intent.createChooser(
                        intent, resources.getString(R.string.share)
                    )
                )
            }
        }
        toolbar.menu.add(R.string.copy)?.also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_copy)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.onClick {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText(
                    getString(R.string.azkar), azkarActivityViewModel.description
                )
                clipboard.setPrimaryClip(clip)
                Snackbar.make(
                    binding.azkarActivityRoot, getString(R.string.copied), Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun play(mp3File: File, id: Int) {
        runCatching {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(applicationContext, Uri.fromFile(mp3File))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                azkarActivityViewModel.stop(id)
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

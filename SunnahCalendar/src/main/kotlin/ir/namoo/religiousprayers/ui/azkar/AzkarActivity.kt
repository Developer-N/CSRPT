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
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAzkarBinding
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.transparentSystemBars
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.logException
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import ir.namoo.commons.utils.initUtils
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.snackMessage
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class AzkarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAzkarBinding

    private val viewModel: AzkarActivityViewModel by viewModel()
    private lateinit var fileLocation: String
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        findViewById<View>(android.R.id.content).transitionName = "shared_element_container"
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementsUseOverlay = true
        super.onCreate(savedInstanceState)
        initUtils(this)
        transparentSystemBars()

        binding = ActivityAzkarBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        setupMenu(binding.appBar.toolbar)
        binding.appBar.root.hideToolbarBottomShadow()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.composeView.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        val chapterID = intent.extras?.getInt("chapterID") ?: -1
        if (chapterID == -1) finish()
        val arabicFont = Typeface.createFromAsset(assets, "fonts/Quran_Bahij_Bold.ttf")
        viewModel.loadItems(chapterID)
        fileLocation =
            getExternalFilesDir(null)?.absolutePath + File.separator + "azkar" + File.separator
        val dir = File(fileLocation)
        if (!dir.exists()) dir.mkdirs()
        binding.composeView.setContent {
            Mdc3Theme {
                val state = rememberLazyListState()

                val isLoading by viewModel.isLoading.collectAsState()
                val azkarLang by viewModel.azkarLang.collectAsState()
                val chapter by viewModel.chapter.collectAsState()
                val azkarItems by viewModel.azkarItems.collectAsState()
                val azkarReferences by viewModel.azkarReferences.collectAsState()
                val itemsState by viewModel.itemsState.collectAsState()
                val lastPlay by viewModel.lastPlay.collectAsState()

                if (!isLoading) binding.appBar.toolbar.title =
                    when (azkarLang) {
                        Language.FA.code -> chapter?.persian
                        Language.CKB.code -> chapter?.kurdish
                        else -> chapter?.arabic
                    }
                Column {
                    AnimatedVisibility(visible = isLoading) {
                        LoadingUIElement()
                    }
                    LazyColumn(state = state) {
                        if (azkarItems.isNotEmpty() && azkarReferences.isNotEmpty() && itemsState.isNotEmpty()) items(
                            items = azkarItems,
                            key = { it.id }) { item ->

                            val fileName = item.sound + ".mp3"
                            val mp3File = File(fileLocation + fileName)

                            AzkarItemUIElement(item = item,
                                reference = azkarReferences[azkarItems.indexOf(
                                    item
                                )],
                                lang = azkarLang,
                                arabicTypeface = arabicFont,
                                soundFile = mp3File,
                                itemState = itemsState[azkarItems.indexOf(item)],
                                play = {
                                    if (lastPlay != -1)
                                        stop()
                                    viewModel.play(item.id)
                                    play(mp3File, item.id)
                                },
                                stop = {
                                    viewModel.stop(item.id)
                                    stop()
                                },
                                download = {
                                    if (isNetworkConnected(this@AzkarActivity))
                                        viewModel.downloadSound(
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
        toolbar.setNavigationIconTint(resolveColor(R.attr.colorOnAppBar))
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.menu.add(R.string.share)?.also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_share)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, viewModel.description)
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
                    getString(R.string.azkar), viewModel.description
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

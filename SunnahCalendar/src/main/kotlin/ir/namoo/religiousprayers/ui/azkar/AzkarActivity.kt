package ir.namoo.religiousprayers.ui.azkar

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.ActivityAzkarBinding
import com.byagowi.persiancalendar.databinding.ItemAzkarSubitemsBinding
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.applyAppLanguage
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.commons.appLink
import ir.namoo.commons.utils.askForStoragePermission
import ir.namoo.commons.utils.isHaveStoragePermission
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.toastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AzkarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAzkarBinding

    @Inject
    lateinit var db: AzkarDB
    private var description = " \uD83E\uDD32\uD83C\uDFFB "
    private var mp: MediaPlayer? = null
    private lateinit var fileLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.apply(this)
        applyAppLanguage(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        }
        super.onCreate(savedInstanceState)
        fileLocation =
            getExternalFilesDir(null)?.absolutePath + File.separator + "azkar" + File.separator
        binding = ActivityAzkarBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        setupMenu(binding.appBar.toolbar)
        binding.appBar.root.hideToolbarBottomShadow()
        val id = intent.extras?.getInt("id")
        lifecycleScope.launchWhenStarted {
            if (id != null) {
                withContext(Dispatchers.Main) {
                    binding.appBar.toolbar.let { toolbar ->
                        toolbar.title = when (language) {
                            Language.FA -> db.azkarsDAO().getAzkarTitleFor(id.toInt()).title_fa
                            Language.CKB -> db.azkarsDAO().getAzkarTitleFor(id.toInt()).title_ku
                            else -> db.azkarsDAO().getAzkarTitleFor(id.toInt()).title_en
                        }
                    }
                }

                binding.recyclerSubAzkars.layoutManager = LinearLayoutManager(this@AzkarActivity)
                binding.recyclerSubAzkars.adapter = AAdapter(db.azkarsDAO().getAzkarsFor(id))

                //set description
                description += "${binding.appBar.toolbar.title}\n"
                val azkars = db.azkarsDAO().getAzkarsFor(id)
                for (zkr in azkars) {
                    description += when (language) {
                        Language.FA -> "\n${zkr.title}\n" +
                                "${zkr.descryption_fa}\n" +
                                "------------------\n" +
                                "${zkr.info_fa}" +
                                "\n------------------\n"
                        Language.CKB -> "${zkr.title}\n" +
                                "${zkr.descryption_ku}\n" +
                                "------------------\n" +
                                "${zkr.info_ku}" +
                                "\n------------------\n"
                        else -> "${zkr.title}\n" +
                                "${zkr.descryption_en}\n" +
                                "------------------\n" +
                                "${zkr.info_en}" +
                                "\n------------------\n"
                    }
                }
                description += appLink
            }
        }

    }//end of onCreate

    private fun setupMenu(toolbar: MaterialToolbar) {
        toolbar.navigationIcon =
            AppCompatResources.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.menu.add(R.string.share)?.also {
            it.icon = binding.appBar.toolbar.context.getCompatDrawable(R.drawable.ic_share)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, description)
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
                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData =
                    ClipData.newPlainText(getString(R.string.azkar), description)
                clipboard.setPrimaryClip(clip)
                Snackbar.make(
                    binding.azkarActivityRoot,
                    getString(R.string.copied),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private inner class AAdapter(val azkars: List<AzkarsEntity>) :
        RecyclerView.Adapter<AAdapter.AVH>() {
        private var lastPlay: AVH? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AVH {
            return AVH(
                ItemAzkarSubitemsBinding.inflate(parent.context.layoutInflater, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return azkars.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onBindViewHolder(holder: AVH, position: Int) {
            holder.bind(azkars[position])
        }

        //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$444
        private inner class AVH(val itemBinding: ItemAzkarSubitemsBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            private var downloadCompleteReceiver: BroadcastReceiver? = null
            private val inProgressDownloads = arrayListOf<Long>()

            @SuppressLint("PrivateResource")
            fun bind(azkar: AzkarsEntity) {
                itemBinding.title.text = azkar.title
                when (language) {
                    Language.FA -> {
                        itemBinding.description.text = azkar.descryption_fa
                        itemBinding.info.text = azkar.info_fa
                    }
                    Language.CKB -> {
                        itemBinding.description.text = azkar.descryption_ku
                        itemBinding.info.text = azkar.info_ku
                    }
                    else -> {
                        itemBinding.description.text = azkar.descryption_en
                        itemBinding.info.text = azkar.info_en
                    }
                }
                if (azkar.muzic == "ندارد")
                    itemBinding.btnAzkarPlay.visibility = View.GONE
                val dir = File(fileLocation)
                if (!dir.exists())
                    dir.mkdirs()
                val fileName = azkar.muzic + ".mp3"
                val mp3File = File(fileLocation + fileName)
                if (!mp3File.exists())
                    itemBinding.btnAzkarPlay.setImageResource(R.drawable.ic_download)
                itemBinding.btnAzkarPlay.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@AzkarActivity,
                            androidx.appcompat.R.anim.abc_fade_in
                        )
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        !isHaveStoragePermission(this@AzkarActivity)
                    )
                        askForStoragePermission(this@AzkarActivity)
                    else if (!mp3File.exists()) {
                        if (!isNetworkConnected(this@AzkarActivity)) {
                            MaterialAlertDialogBuilder(this@AzkarActivity).apply {
                                setTitle(resources.getString(R.string.network_error_title))
                                setMessage(resources.getString(R.string.network_error_message))
                                show()
                            }
                        } else {
                            val url = "https://archive.org/download/azkar_n/" + azkar.muzic + ".MP3"
                            download(url, mp3File.absolutePath)
                        }
                    } else {
                        if (lastPlay != null) {
                            mp?.stop()
                            mp?.release()
                            mp = null
                            lastPlay?.itemBinding?.btnAzkarPlay?.setImageResource(R.drawable.ic_baseline_play_circle_filled)
                        }
                        if (lastPlay == null || (lastPlay != null && lastPlay != this)) {
                            lastPlay = this
                            play(mp3File)
                        } else {
                            lastPlay = null
                        }

                    }
                }

            }//end of bind

            private fun download(url: String, filePath: String) {

                val animation = AnimationUtils.loadAnimation(
                    this@AzkarActivity,
                    androidx.appcompat.R.anim.abc_fade_in
                )
                animation.interpolator = LinearInterpolator()
                animation.repeatCount = Animation.INFINITE
                itemBinding.btnAzkarPlay.startAnimation(animation)

                val request = DownloadManager.Request(url.toUri())
                    .setTitle(getString(R.string.downloading_azkar))
                    .setDestinationUri(File(filePath).toUri())
                val downloadManager = getSystemService<DownloadManager>()
                if (downloadManager == null) {
                    toastMessage(getString(R.string.download_failed_tray_again))
                    return
                }
                val downloadRequestID = downloadManager.enqueue(request)
                listenDownloadProgress(downloadRequestID, filePath)
            }

            private fun listenDownloadProgress(downloadID: Long, filePath: String) {
                downloadCompleteReceiver = DownloadCompleteReceiver(filePath)
                inProgressDownloads.add(downloadID)
                val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                registerReceiver(downloadCompleteReceiver, intentFilter)
            }

            private inner class DownloadCompleteReceiver(val filePath: String) :
                BroadcastReceiver() {

                override fun onReceive(context: Context?, intent: Intent?) {
                    if (inProgressDownloads.isNullOrEmpty()) {
                        unregisterReceiver(this)
                        downloadCompleteReceiver = null
                    }
                    val downloadRequestID =
                        intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (downloadRequestID == -1L) return
                    inProgressDownloads.find { it == downloadRequestID }?.let {
                        checkDownloadResult(it, filePath)
                    }
                }
            }

            private fun checkDownloadResult(downloadID: Long, filePath: String) {

                val downloadManager = getSystemService<DownloadManager>()
                if (downloadManager == null) {
                    toastMessage(getString(R.string.download_failed_tray_again))
                    return
                }
                inProgressDownloads.find { it == downloadID }?.let {
                    val query = DownloadManager.Query().setFilterById(it)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToNext()) {
                            inProgressDownloads.remove(inProgressDownloads.find { f -> f == downloadID })
                            inProgressDownloads.remove(downloadID)

                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)

                            when (cursor.getInt(statusIndex)) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    toastMessage(getString(R.string.downloaded))
                                    lifecycleScope.launch {
                                        itemBinding.btnAzkarPlay.clearAnimation()
                                        itemBinding.btnAzkarPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled)
                                        if (mp == null || !mp!!.isPlaying) {
                                            lastPlay = this@AVH
                                            play(File(filePath))
                                        }
                                    }
                                }

                                DownloadManager.STATUS_FAILED -> {
                                    toastMessage(getString(R.string.download_failed_tray_again))
                                }
                            }
                        }
                    }
                }
            }

            fun play(mp3File: File) {
                runCatching {
                    mp = MediaPlayer()
                    mp?.setDataSource(applicationContext, Uri.fromFile(mp3File))
                    mp?.prepare()
                    mp?.start()
                    itemBinding.btnAzkarPlay.setImageResource(R.drawable.ic_baseline_stop_24)
                    mp?.setOnCompletionListener {
                        itemBinding.btnAzkarPlay.setImageResource(R.drawable.ic_baseline_play_circle_filled)
                        lastPlay = null
                    }
                }.onFailure(logException)
            }
        }//end of view holder
    }//end of Adapter

    override fun onBackPressed() {
        runCatching {
            if (mp != null && mp?.isPlaying == true) {
                mp?.stop()
                mp?.release()
                mp = null
            }
        }.onFailure(logException)
        super.onBackPressed()
    }

}//end of class

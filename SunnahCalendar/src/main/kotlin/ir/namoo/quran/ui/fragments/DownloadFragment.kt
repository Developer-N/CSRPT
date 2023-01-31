package ir.namoo.quran.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentDownloadBinding
import com.byagowi.persiancalendar.databinding.ItemQuranDownloadBinding
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.toastMessage
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.FileDownloadEntity
import ir.namoo.quran.utils.getAyaFileName
import ir.namoo.quran.utils.getQuranDirectoryInInternal
import ir.namoo.quran.utils.getQuranDirectoryInSD
import ir.namoo.quran.utils.getSelectedQuranDirectoryPath
import ir.namoo.quran.utils.getSuraFileName
import ir.namoo.quran.viewmodels.DownloadViewModel
import ir.namoo.religiousprayers.ui.shared.ShapedAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.util.*
import kotlin.concurrent.timer

class DownloadFragment : Fragment() {

    private lateinit var binding: FragmentDownloadBinding
    private lateinit var chapterList: MutableList<ChapterEntity>
    private lateinit var names: Array<String>
    private lateinit var folders: Array<String>
    private lateinit var links: Array<String>
    private var sura = -1
    private var folder = "-"
    private val viewModel: DownloadViewModel by viewModel()
    private val inProgressDownloads = arrayListOf<FileDownloadEntity>()
    private lateinit var downloadedMSG: Toast


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        downloadedMSG =
            Toast.makeText(requireContext(), getString(R.string.downloaded), Toast.LENGTH_SHORT)
        sura = requireActivity().intent.extras?.getInt("sura") ?: -3
        folder = requireActivity().intent.extras?.getString("folder") ?: "-"
        binding = FragmentDownloadBinding.inflate(inflater)
        viewModel.chapterList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) chapterList = it
        }
        binding.appBar.toolbar.let {
            it.setTitle(R.string.download_audios)
            it.setupMenuNavigation()
        }
        binding.appBar.root.hideToolbarBottomShadow()

        names = resources.getStringArray(R.array.quran_names)
        folders = resources.getStringArray(R.array.quran_folders)
        links = resources.getStringArray(R.array.quran_links)
        binding.spinnerQuranDownloadType.apply {
            adapter =
                ShapedAdapter(requireContext(), R.layout.select_dialog_item, R.id.text1, names)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) =
                    updateRecycler()
            }
            setSelection(if (folder == "-") 0 else folders.indexOf(folder))
        }
        binding.recyclerQuranDownload.layoutManager = LinearLayoutManager(requireContext())

        viewModel.reqIDs.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) inProgressDownloads.addAll(it)
        }

        viewModel.checkDownloads()

        return binding.root
    }//end of onCreateView

    fun updateRecycler() {
        binding.recyclerQuranDownload.adapter = QDownloadAdapter()
        if (sura > 0) (binding.recyclerQuranDownload.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            sura - 1,
            0
        )
    }

    private inner class QDownloadAdapter :
        RecyclerView.Adapter<QDownloadAdapter.QDownloadViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QDownloadViewHolder =
            QDownloadViewHolder(ItemQuranDownloadBinding.inflate(layoutInflater, parent, false))

        override fun getItemCount(): Int = chapterList.size
        override fun getItemViewType(position: Int): Int = position
        override fun onBindViewHolder(holder: QDownloadViewHolder, position: Int) = holder.bind(
            chapterList[position],
            position,
            (binding.spinnerQuranDownloadType.selectedItemPosition * 1000) + position + 1
        )


        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        inner class QDownloadViewHolder(private val itemBinding: ItemQuranDownloadBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            private var downloadProgressTimer: Timer? = null
            private var downloadCompleteReceiver: BroadcastReceiver? = null

            init {
                itemBinding.txtDownloadInfo.typeface = Typeface.DEFAULT_BOLD
            }

            @SuppressLint("PrivateResource", "SetTextI18n")
            fun bind(chapter: ChapterEntity, position: Int, downloadID: Int) {
                itemBinding.txtItemQdSuraName.text =
                    "${formatNumber(position + 1)}: ${chapter.nameArabic}"
                itemBinding.btnItemQdDownload.visibility = View.INVISIBLE
                itemBinding.btnItemQdDownloadStop.visibility = View.INVISIBLE
                itemBinding.progressLoadingFileState.visibility = View.VISIBLE

                runCatching {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val isFileExist = File(
                            getQuranDirectoryInInternal(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition] + "/" + getAyaFileName(
                                chapter.sura,
                                1
                            )
                        ).exists() || File(
                            getQuranDirectoryInSD(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition] + "/" + getAyaFileName(
                                chapter.sura,
                                1
                            )
                        ).exists()
                        requireActivity().runOnUiThread {
                            itemBinding.btnItemQdDownload.setImageResource(
                                if (isFileExist) R.drawable.ic_files_ok
                                else R.drawable.ic_files_download
                            )
                        }
                        val isInProgress = inProgressDownloads.find { f ->
                            f.id == downloadID
                        } != null
                        requireActivity().runOnUiThread {
                            itemBinding.btnItemQdDownload.visibility = View.VISIBLE
                            itemBinding.progressLoadingFileState.visibility = View.INVISIBLE
                            if (isInProgress) {
                                itemBinding.btnItemQdDownloadStop.visibility = View.VISIBLE
                                itemBinding.btnItemQdDownload.visibility = View.INVISIBLE
                            }
                        }
                    }
                }.onFailure(logException)//end of check file exist

                itemBinding.btnItemQdDownload.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(), com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (!isNetworkConnected(requireContext())) {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(resources.getString(R.string.network_error_title))
                            setMessage(resources.getString(R.string.network_error_message))
                            setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            show()
                        }
                    } else { // start downloading
                        it.visibility = View.INVISIBLE
                        itemBinding.btnItemQdDownloadStop.visibility = View.VISIBLE
                        download(chapter, downloadID, position)
                    }
                }//end of btnDownloadClickListener

                itemBinding.btnItemQdDownloadStop.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(), com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    inProgressDownloads.find { f ->
                        f.id == downloadID
                    }?.let { fd ->
                        it.visibility = View.INVISIBLE
                        itemBinding.btnItemQdDownload.visibility = View.VISIBLE
                        cancelDownload(fd)
                        visibleDownloadViews(false)
                    }
                }//end of btnStopClickListener

                resumeDownload(downloadID)
            }//end of bind

            private fun cancelDownload(fileDownload: FileDownloadEntity) {
                val downloadManager = requireContext().getSystemService<DownloadManager>()

                if (downloadManager == null) {
                    requireContext().toastMessage(getString(R.string.download_failed_tray_again))
                    return
                }
                downloadManager.remove(fileDownload.downloadRequest)
                viewModel.removeDownload(fileDownload.id)
                inProgressDownloads.remove(fileDownload)
                visibleDownloadViews(false)
                notifyItemChanged(fileDownload.position)
            }

            private fun resumeDownload(downloadID: Int) {
                inProgressDownloads.find { it.id == downloadID }?.let {
                    val downloadManager = requireContext().getSystemService<DownloadManager>()
                    if (downloadManager == null) {
                        requireContext().toastMessage(getString(R.string.download_failed_tray_again))
                        return
                    }
                    val query = DownloadManager.Query().setFilterById(it.downloadRequest)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            when (cursor.getInt(statusIndex)) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    unzip(it)
                                    inProgressDownloads.remove(inProgressDownloads.find { f -> f.id == it.id })
                                    viewModel.removeDownload(it.id)
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    visibleDownloadViews(false)
                                }
                                DownloadManager.STATUS_PENDING, DownloadManager.STATUS_RUNNING -> {
                                    visibleDownloadViews(true)
                                    lifecycleScope.launch {
                                        delay(1000)
                                        requireActivity().runOnUiThread {
                                            itemBinding.btnItemQdDownloadStop.visibility =
                                                View.VISIBLE
                                            itemBinding.btnItemQdDownload.visibility =
                                                View.INVISIBLE
                                        }
                                    }
                                    listenDownloadProgress(downloadID)
                                }
                            }
                        } else {
                            inProgressDownloads.remove(inProgressDownloads.find { f -> f.id == it.id })
                            viewModel.removeDownload(it.id)
                        }
                    }
                }
            }

            private fun download(chapter: ChapterEntity, downloadID: Int, position: Int) {
                val spinnerSelectedPosition = binding.spinnerQuranDownloadType.selectedItemPosition
                val downloadFile =
                    getSelectedQuranDirectoryPath(requireContext()) + "/" + folders[spinnerSelectedPosition] + "/" + getSuraFileName(
                        chapter.sura
                    )
                val folderPath =
                    getSelectedQuranDirectoryPath(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition]


                val url = links[spinnerSelectedPosition] + getSuraFileName(chapter.sura)
                val request = DownloadManager.Request(url.toUri())
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setTitle(chapter.nameArabic).setDescription(names[spinnerSelectedPosition])
                    .setDestinationUri(File(downloadFile).toUri()).setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    request.setRequiresCharging(false)
                }

                val downloadManager = requireContext().getSystemService<DownloadManager>()
                if (downloadManager == null) {
                    requireContext().toastMessage(getString(R.string.download_failed_tray_again))
                    return
                }
                val downloadRequestID = downloadManager.enqueue(request)
                val d = FileDownloadEntity(
                    downloadID, downloadRequestID, downloadFile, folderPath, position
                )
                viewModel.addDownload(d)
                inProgressDownloads.add(d)
                listenDownloadProgress(downloadID)
            }

            private fun listenDownloadProgress(downloadID: Int) {
                downloadCompleteReceiver = DownloadCompleteReceiver()
                downloadProgressTimer = timer(period = 500) { updateDownloadProgress(downloadID) }
                val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                requireContext().registerReceiver(downloadCompleteReceiver, intentFilter)
            }

            private inner class DownloadCompleteReceiver : BroadcastReceiver() {

                override fun onReceive(context: Context?, intent: Intent?) {
                    runCatching {
                        if (inProgressDownloads.isEmpty()) {
                            requireContext().unregisterReceiver(this)
                            downloadCompleteReceiver = null

                            downloadProgressTimer?.cancel()
                            downloadProgressTimer = null
                        }
                        val downloadRequestID =
                            intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (downloadRequestID == -1L) return
                        inProgressDownloads.find { it.downloadRequest == downloadRequestID }?.let {
                            checkDownloadResult(it.id)
                        }
                    }.onFailure(logException)
                }
            }

            private fun checkDownloadResult(downloadID: Int) {

                val downloadManager = requireContext().getSystemService<DownloadManager>()
                if (downloadManager == null) {
                    requireContext().toastMessage(getString(R.string.download_failed_tray_again))
                    return
                }
                inProgressDownloads.find { it.id == downloadID }?.let {
                    val query = DownloadManager.Query().setFilterById(it.downloadRequest)
                    downloadManager.query(query).use { cursor ->
                        if (cursor.moveToNext()) {
                            inProgressDownloads.remove(inProgressDownloads.find { f -> f.id == downloadID })
                            viewModel.removeDownload(downloadID)
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                            when (cursor.getInt(statusIndex)) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    downloadedMSG.show()
                                    unzip(it)
                                    inProgressDownloads.remove(inProgressDownloads.find { f -> f.id == it.id })
                                    viewModel.removeDownload(it.id)
                                }

                                DownloadManager.STATUS_FAILED -> {
                                    requireContext().toastMessage(getString(R.string.download_failed_tray_again))
                                }
                            }
                            visibleDownloadViews(false)
                            itemBinding.btnItemQdDownloadStop.visibility = View.INVISIBLE
                            itemBinding.btnItemQdDownload.visibility = View.VISIBLE
                            notifyItemChanged(it.position)
                        }
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            private fun updateDownloadProgress(downloadID: Int) {
                runCatching {
                    val downloadManager = requireContext().getSystemService<DownloadManager>()
                    if (downloadManager == null) {
                        requireContext().toastMessage(getString(R.string.download_failed_tray_again))
                        return
                    }
                    inProgressDownloads.find { it.id == downloadID }?.let {
                        val query = DownloadManager.Query().setFilterById(it.downloadRequest)
                        downloadManager.query(query).use { cursor ->
                            if (cursor.moveToNext()) {
                                val totalSizeIndex =
                                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                                val bytesDownloadedIndex =
                                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)

                                val totalBytes = cursor.getInt(totalSizeIndex)
                                val downloadedBytes = cursor.getInt(bytesDownloadedIndex)

                                if (downloadedBytes == totalBytes && totalBytes > 0) {
                                    downloadProgressTimer?.cancel()
                                } else {
                                    requireActivity().runOnUiThread {
                                        val progress =
                                            (downloadedBytes.toFloat() / totalBytes * 100).toInt()
                                        if (itemBinding.progressItemQuranDownload.visibility != View.VISIBLE) {
                                            visibleDownloadViews(true)
                                        }
                                        if (totalBytes > 0) runCatching {
                                            itemBinding.progressItemQuranDownload.isIndeterminate =
                                                false
                                            ObjectAnimator.ofInt(
                                                itemBinding.progressItemQuranDownload,
                                                "progress",
                                                itemBinding.progressItemQuranDownload.progress,
                                                progress
                                            ).apply {
                                                interpolator = AccelerateDecelerateInterpolator()
                                                start()
                                            }
                                            itemBinding.txtDownloadInfo.text = "${
                                                String.format(
                                                    "%.2f",
                                                    (downloadedBytes.toDouble() / 1024) / 1024
                                                )
                                            } mb => ${
                                                String.format(
                                                    "%.2f", (totalBytes.toDouble() / 1024) / 1024
                                                )
                                            } mb"
                                        }.onFailure(logException)
                                        else itemBinding.progressItemQuranDownload.isIndeterminate =
                                            true
                                    }
                                }
                            }
                        }
                    }
                    Unit
                }.onFailure(logException).onFailure {
                    downloadProgressTimer?.cancel()
                    downloadProgressTimer = null
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            private fun unzip(fileDownload: FileDownloadEntity) {
                runCatching {
                    lifecycleScope.launch(Dispatchers.IO) {
                        requireActivity().runOnUiThread {
                            itemBinding.txtDownloadInfo.text =
                                getString(R.string.download_completed_unzip)
                        }
                        File(fileDownload.downloadFile).let {
                            if (it.exists()) {
                                ZipFile(it).extractAll(fileDownload.folderPath)
                                it.delete()
                            }
                        }
                        Handler(Looper.getMainLooper()).postDelayed({
                            requireActivity().runOnUiThread {
                                itemBinding.btnItemQdDownloadStop.visibility = View.INVISIBLE
                                itemBinding.btnItemQdDownload.visibility = View.VISIBLE
                                visibleDownloadViews(false)
                                notifyDataSetChanged()
                            }
                        }, 1000)

                    }
                }.onFailure(logException)
            }

            fun visibleDownloadViews(visible: Boolean) = requireActivity().runOnUiThread {
                if (visible) {
                    itemBinding.progressItemQuranDownload.visibility = View.VISIBLE
                    itemBinding.txtDownloadInfo.visibility = View.VISIBLE
                } else {
                    itemBinding.progressItemQuranDownload.visibility = View.GONE
                    itemBinding.txtDownloadInfo.visibility = View.GONE
                }
                val transition = ChangeBounds().apply {
                    interpolator = LinearOutSlowInInterpolator()
                }
                TransitionManager.beginDelayedTransition(
                    binding.recyclerQuranDownload as ViewGroup, transition
                )
            }

        }//end of class QDownloadViewHolder
    }//end of class QDownloadAdapter
}//end of class

package ir.namoo.quran.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.ui.QuranActivity
import ir.namoo.quran.utils.*
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentDownloadBinding
import ir.namoo.religiousprayers.databinding.ItemQuranDownloadBinding
import ir.namoo.religiousprayers.ui.edit.ShapedAdapter
import ir.namoo.religiousprayers.utils.*
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadFragment : Fragment() {

    private lateinit var binding: FragmentDownloadBinding
    private lateinit var chapterList: MutableList<ChapterEntity>
    private lateinit var names: Array<String>
    private lateinit var folders: Array<String>
    private lateinit var links: Array<String>
    private var sura = -1
    private var folder = "-"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as QuranActivity).setTitleAndSubtitle(
            resources.getString(R.string.download_audios),
            ""
        )

        sura = requireActivity().intent.extras?.getInt("sura") ?: -3
        folder = requireActivity().intent.extras?.getString("folder") ?: "-"

        Log.e(TAG, "onCreateView: $sura")
        binding = FragmentDownloadBinding.inflate(inflater)
        chapterList =
            QuranDB.getInstance(requireContext().applicationContext).chaptersDao().getAllChapters()
        names = resources.getStringArray(R.array.quran_names)
        folders = resources.getStringArray(R.array.quran_folders)
        links = resources.getStringArray(R.array.quran_links)
        binding.spinnerQuranDownloadType.apply {
            adapter = ShapedAdapter(requireContext(), R.layout.select_dialog_item, names)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) =
                    updateRecycler()
            }
            setSelection(if (folder == "-") 0 else folders.indexOf(folder))
        }
        binding.recyclerQuranDownload.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }//end of onCreateView

    fun updateRecycler() {
        binding.recyclerQuranDownload.adapter = QDownloadAdapter()
        if (sura > 0)
            (binding.recyclerQuranDownload.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
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

        override fun onBindViewHolder(holder: QDownloadViewHolder, position: Int) =
            holder.bind(chapterList[position], position)


        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        inner class QDownloadViewHolder(private val itemBinding: ItemQuranDownloadBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            init {
                itemBinding.txtDownloadInfo.typeface = Typeface.DEFAULT_BOLD
            }

            @SuppressLint("PrivateResource")
            fun bind(chapter: ChapterEntity, position: Int) {
                itemBinding.txtItemQdSuraName.text = chapter.nameArabic
                try {
//                    var allFileAreExists = true
//                    for (i in 1..chapter.ayaCount!!)
//                        if (!File(
//                                getQuranDirectoryPath(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition] + "/" +
//                                        getAyaFileName(chapter.sura, i)
//                            ).exists()
//                        ) {
//                            allFileAreExists = false
//                            break
//                        }
                    itemBinding.btnItemQdDownload.setImageResource(
                        when {
//                            allFileAreExists -> R.drawable.ic_files_ok
                            File(
                                getQuranDirectoryInInternal(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition] + "/" +
                                        getAyaFileName(chapter.sura, 1)
                            ).exists() || File(
                                getQuranDirectoryInSD(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition] + "/" +
                                        getAyaFileName(chapter.sura, 1)
                            ).exists() -> R.drawable.ic_files_ok
                            else -> R.drawable.ic_files_download
                        }
                    )
                } catch (ex: Exception) {
                    Log.e(TAG, "on bind file error : ", ex)
                }
                itemBinding.btnItemQdDownload.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (!isHaveStoragePermission(requireActivity())) {
                        askForStoragePermission(requireActivity())
                    } else if (!isNetworkConnected(requireContext())) {
                        val alert: android.app.AlertDialog.Builder =
                            android.app.AlertDialog.Builder(context)
                        alert.setTitle(resources.getString(R.string.network_error_title))
                        alert.setMessage(resources.getString(R.string.network_error_message))
                        alert.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        alert.create().show()
                    } else { // start downloading
                        DownloadTask(chapter, position).execute()
                    }
                }//end of setOnClickListener
            }//end of bind

            @SuppressLint("StaticFieldLeak")
            inner class DownloadTask(val chapter: ChapterEntity, val position: Int) :
                AsyncTask<String, Int, String>() {
                private var length = 0
                private var isDownloading = true
                val folderPath =
                    getSelectedQuranDirectoryPath(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition]

                override fun onPreExecute() {
                    super.onPreExecute()
                    Log.e(TAG, "folder path : $folderPath ")
                    if (!File(folderPath).exists())
                        File(folderPath).mkdirs()
                    binding.spinnerQuranDownloadType.isEnabled = false
                    itemBinding.progressItemQuranDownload.visibility = View.VISIBLE
                    itemBinding.progressItemQuranDownload.isIndeterminate = true
                    itemBinding.txtDownloadInfo.visibility = View.VISIBLE
                    itemBinding.btnItemQdDownload.isEnabled = false
                    val transition = ChangeBounds().apply {
                        interpolator = LinearOutSlowInInterpolator()
                    }
                    TransitionManager.beginDelayedTransition(
                        binding.recyclerQuranDownload as ViewGroup,
                        transition
                    )
                }

                override fun doInBackground(vararg p0: String?): String {
                    return try {
                        val url = URL(
                            links[binding.spinnerQuranDownloadType.selectedItemPosition] + getSuraFileName(
                                chapter.sura
                            )
                        )
                        Log.e(TAG, "doInBackground: download $url")
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connect()
                        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                            "Error Downloading! try again"
                        } else {
                            val file = File(
                                getSelectedQuranDirectoryPath(requireContext()) + "/" + folders[binding.spinnerQuranDownloadType.selectedItemPosition] + "/" +
                                        getSuraFileName(chapter.sura)
                            )
                            length = connection.contentLength
                            itemBinding.progressItemQuranDownload.max = length
                            val input = connection.inputStream
                            val output = FileOutputStream(file)
                            val data = ByteArray(4096)
                            var total = 0
                            var count: Int
                            while (input.read(data).also { count = it } != -1) {
                                total += count
                                if (connection.contentLength > 0)
                                    publishProgress(total)
                                output.write(data, 0, count)
                            }
                            output.close()
                            input.close()
                            connection.disconnect()
                            // download complete and start extracting
                            isDownloading = false
                            publishProgress(total)

                            val zip = ZipFile(file)
                            zip.extractAll(folderPath)
                            file.delete()
                            "Success!"
                        }
                    } catch (ex: Exception) {
                        "Error --> $ex"
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onProgressUpdate(vararg values: Int?) {
                    super.onProgressUpdate(*values)
                    if (isDownloading) {
                        itemBinding.progressItemQuranDownload.isIndeterminate = false
                        for (v in values)
                            v?.let {
                                itemBinding.progressItemQuranDownload.progress = it
                                itemBinding.txtDownloadInfo.text =
                                    "${
                                        String.format(
                                            "%.2f",
                                            (it.toDouble() / 1024) / 1024
                                        )
                                    } mb => ${
                                        String.format(
                                            "%.2f",
                                            (length.toDouble() / 1024) / 1024
                                        )
                                    } mb"
                            }
                    } else {
                        itemBinding.progressItemQuranDownload.isIndeterminate = true
                        itemBinding.txtDownloadInfo.text =
                            getString(R.string.download_completed_unzip)
                    }
                }

                override fun onPostExecute(result: String?) {
                    super.onPostExecute(result)
                    binding.spinnerQuranDownloadType.isEnabled = true
                    itemBinding.progressItemQuranDownload.visibility = View.GONE
                    itemBinding.txtDownloadInfo.visibility = View.GONE
                    itemBinding.btnItemQdDownload.isEnabled = true
                    val transition = ChangeBounds().apply {
                        interpolator = LinearOutSlowInInterpolator()
                    }
                    TransitionManager.beginDelayedTransition(
                        binding.recyclerQuranDownload as ViewGroup,
                        transition
                    )
                    notifyItemChanged(position)
                    if (!result.isNullOrEmpty() && result == "Success!")
                        snackMessage(itemBinding.root, getString(R.string.downloaded))
                    else
                        snackMessage(
                            itemBinding.root,
                            getString(R.string.download_failed_tray_again)
                        )
                }
            }//end of class DownloadTask

        }//end of class QDownloadViewHolder
    }//end of class QDownloadAdapter
}//end of class
package ir.namoo.religiousprayers.ui.preferences

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.AthanDownloadDialogBinding
import ir.namoo.religiousprayers.databinding.ItemAthanBinding
import ir.namoo.religiousprayers.db.Athan
import ir.namoo.religiousprayers.utils.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AthanDownloadDialog(val fragment: NSettingFragment, var athanList: List<Athan>) :
    AppCompatDialogFragment() {
    private lateinit var binding: AthanDownloadDialogBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AthanDownloadDialogBinding.inflate(requireActivity().layoutInflater)
        binding.recyclerAthanDownload.adapter = AthanAdapter()
        return AlertDialog.Builder(requireContext()).apply {
            setView(binding.root)
            setCustomTitle(null)
            isCancelable = false
            setNegativeButton(R.string.close) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
        }.create()
    }

    private inner class AthanAdapter : RecyclerView.Adapter<AthanAdapter.AViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AViewHolder =
            AViewHolder(ItemAthanBinding.inflate(requireActivity().layoutInflater, parent, false))

        override fun getItemCount(): Int = athanList.size

        override fun getItemViewType(position: Int): Int = position

        override fun onBindViewHolder(holder: AViewHolder, position: Int) = holder.bind(position)

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@222
        inner class AViewHolder(var binding: ItemAthanBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.progressAthanDownload.visibility = View.GONE
            }

            @SuppressLint("PrivateResource")
            fun bind(position: Int) {
                binding.txtItemAthanRow.text = formatNumber((position + 1))
                binding.txtItemAthanName.text = athanList[position].name
                binding.btnAthanDownload.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (!isNetworkConnected(requireContext())) {
                        val alert: android.app.AlertDialog.Builder =
                            android.app.AlertDialog.Builder(context)
                        alert.setTitle(resources.getString(R.string.network_error_title))
                        alert.setMessage(resources.getString(R.string.network_error_message))
                        alert.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        alert.create().show()
                    } else if (!isHaveStoragePermission(requireActivity()))
                        askForStoragePermission(requireActivity())
                    else
                        GetSize(position).execute()
                }
                val f = File(
                    getAthansDirectoryPath(requireContext()) + "/" + getFileNameFromLink(athanList[position].link)
                )
                if (f.exists())
                    binding.btnAthanDownload.setImageResource(R.drawable.ic_check)

            }//end of bind

            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            @SuppressLint("StaticFieldLeak")
            inner class DownloadTask(val link: String) : AsyncTask<String, Int, String>() {

                override fun doInBackground(vararg params: String?): String {
                    return try {
                        val url = URL(link)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connect()
                        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                            "Error"
                        } else {
                            binding.progressAthanDownload.max = connection.contentLength
                            val f = File(getAthansDirectoryPath(requireContext()))
                            if (!f.exists())
                                f.mkdirs()
                            val file = File(
                                getAthansDirectoryPath(requireContext()) + "/" + getFileNameFromLink(
                                    link
                                )
                            )
                            val input = connection.inputStream
                            val output = FileOutputStream(file)
                            val data = ByteArray(4096)
                            var total = 0
                            var count: Int
                            while (input.read(data).also { count = it } != -1) {
                                if (isCancelled) {
                                    input.close()
                                    return "Error"
                                }
                                total += count
                                if (connection.contentLength > 0)
                                    publishProgress(total)
                                output.write(data, 0, count)
                            }
                            output.close()
                            input.close()
                            connection.disconnect()
                            "OK"
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "download error : $ex")
                        "Error"
                    }
                }//end of doInBackground

                override fun onPreExecute() {
                    super.onPreExecute()
                    binding.progressAthanDownload.isIndeterminate = false
                    binding.btnAthanDownload.isEnabled = false
                    binding.progressAthanDownload.visibility = View.VISIBLE
                    val transition = ChangeBounds().apply {
                        interpolator = LinearOutSlowInInterpolator()
                    }
                    TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
                }

                override fun onProgressUpdate(vararg values: Int?) {
                    super.onProgressUpdate(*values)
                    values.let {
                        binding.progressAthanDownload.progress = it[0]!!
                    }
                }

                override fun onPostExecute(result: String?) {
                    super.onPostExecute(result)
                    binding.btnAthanDownload.isEnabled = true
                    binding.btnAthanDownload.setImageResource(R.drawable.ic_check)
                    binding.progressAthanDownload.visibility = View.GONE
                    fragment.initAthanSpinner()
                    val transition = ChangeBounds().apply {
                        interpolator = LinearOutSlowInInterpolator()
                    }
                    TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
                }

            }//end of class DownloadTask

            @SuppressLint("StaticFieldLeak")
            inner class GetSize(val position: Int) : AsyncTask<String, Int, String>() {
                override fun doInBackground(vararg params: String?): String {
                    return try {
                        val url = URL(athanList[position].link)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.connect()
                        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                            val res = connection.contentLength.toString()
                            connection.disconnect()
                            res
                        } else
                            "Error"
                    } catch (ex: Exception) {
                        Log.e(TAG, "bind error: $ex")
                        "Error"
                    }
                }

                override fun onPreExecute() {
                    super.onPreExecute()
                    binding.txtItemAthanSize.text = "..."
                    binding.progressAthanDownload.isIndeterminate = true
                    binding.progressAthanDownload.interpolator = LinearOutSlowInInterpolator()
                    binding.progressAthanDownload.visibility = View.VISIBLE
                    binding.btnAthanDownload.isEnabled = false
                }

                @SuppressLint("SetTextI18n")
                override fun onPostExecute(result: String?) {
                    super.onPostExecute(result)
                    binding.progressAthanDownload.visibility = View.GONE
                    binding.btnAthanDownload.isEnabled = true
                    if (!result.isNullOrEmpty() && result != "Error") {
                        binding.txtItemAthanSize.text =
                            "${formatNumber(
                                String.format(
                                    "%.2f", (result.toDouble() / 1024) / 1024
                                )
                            )} mb"
                        DownloadTask(athanList[position].link).execute()
                    }
                }
            }
        }
    }
}//end of AthanDownloadDialog
package ir.namoo.religiousprayers.ui.preferences

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.AthanDownloadDialogBinding
import com.byagowi.persiancalendar.databinding.ItemAthanBinding
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.ktor.client.*
import io.ktor.client.engine.android.*
import ir.namoo.commons.model.Athan
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.ServerAthanModel
import ir.namoo.commons.service.PrayTimesService
import ir.namoo.commons.utils.askForStoragePermission
import ir.namoo.commons.utils.getAthansDirectoryPath
import ir.namoo.commons.utils.isHaveStoragePermission
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.snackMessage
import ir.namoo.religiousprayers.downloader.DownloadResult
import ir.namoo.religiousprayers.downloader.downloadFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AthanDownloadDialog(
    val prayTimesService: PrayTimesService,
    val athanDB: AthanDB,
    val type: Int
) : AppCompatDialogFragment() {
    private lateinit var binding: AthanDownloadDialogBinding
    private val adapter = AthanAdapter()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AthanDownloadDialogBinding.inflate(requireActivity().layoutInflater)
        lifecycleScope.launch {
            val athanList =
                if (type == 1) prayTimesService.getAthans() else prayTimesService.getAlarms()
            for (a in athanList) {
                val ad = athanDB.athanDAO().getAthan(a.name)
                if (ad == null)
                    athanDB.athanDAO()
                        .insert(Athan(a.name, "online/${a.fileTitle}", type, a.fileTitle))
                else {
                    ad.name = a.name
                    ad.link = "online/${a.fileTitle}"
                    ad.type = type
                    ad.fileName = a.fileTitle
                    athanDB.athanDAO().update(ad)
                }
            }
            adapter.setData(athanList)
            withContext(Dispatchers.Main) {
                binding.loading.visibility = View.GONE
                binding.recyclerAthanDownload.visibility = View.VISIBLE
            }
        }
        binding.recyclerAthanDownload.adapter = adapter
        val dialog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            BottomSheetDialog(requireContext()).apply {
                setContentView(binding.root)
                isCancelable = false
                create()
            }
        else MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setCustomTitle(null)
            isCancelable = false
        }.create()

        binding.btnAthanDwonloadClose.setOnClickListener { dialog.dismiss() }

        return dialog
    }

    private inner class AthanAdapter : RecyclerView.Adapter<AthanAdapter.AViewHolder>() {
        private var athanList = mutableListOf<ServerAthanModel>()
        private var mPosition = 0
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AViewHolder =
            AViewHolder(ItemAthanBinding.inflate(requireActivity().layoutInflater, parent, false))

        @SuppressLint("NotifyDataSetChanged")
        fun setData(list: List<ServerAthanModel>) {
            athanList.clear()
            athanList.addAll(list)
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = athanList.size

        override fun getItemViewType(position: Int): Int = position

        override fun onBindViewHolder(holder: AViewHolder, position: Int) = holder.bind(position)

        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@222
        inner class AViewHolder(var itemBinding: ItemAthanBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            init {
                itemBinding.progressAthanDownload.visibility = View.GONE
            }

            @SuppressLint("PrivateResource")
            fun bind(position: Int) {
                mPosition = position
                itemBinding.txtItemAthanRow.text = formatNumber((position + 1))
                itemBinding.txtItemAthanName.text = athanList[position].name
                itemBinding.btnAthanDownload.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (!isNetworkConnected(requireContext())) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(resources.getString(R.string.network_error_title))
                            .setMessage(resources.getString(R.string.network_error_message))
                            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    } else if (!isHaveStoragePermission(requireActivity()))
                        askForStoragePermission(requireActivity())
                    else {
                        download(athanList[position])
                    }
                }
                val f = File(
                    getAthansDirectoryPath(requireContext()) + "/" + athanList[position].fileTitle
                )
                if (f.exists())
                    itemBinding.btnAthanDownload.setImageResource(R.drawable.ic_check)

            }//end of bind

            @SuppressLint("NotifyDataSetChanged")
            fun download(athanModel: ServerAthanModel) {
                val file =
                    File("${getAthansDirectoryPath(requireContext())}/${athanModel.fileTitle}")
                val url = "https://namoodev.ir/api/v1/app/downloadAthan/${athanModel.id}"
                itemBinding.btnAthanDownload.visibility = View.GONE
                itemBinding.progressAthanDownload.visibility = View.VISIBLE
                val ktor = HttpClient(Android)
                lifecycleScope.launch {
                    ktor.downloadFile(file, url).collect {
                        when (it) {
                            is DownloadResult.Success -> {
                                itemBinding.progressAthanDownload.progress = 0
                                notifyDataSetChanged()
                                itemBinding.btnAthanDownload.visibility = View.VISIBLE
                                itemBinding.progressAthanDownload.visibility = View.GONE
                                val transition = ChangeBounds().apply {
                                    interpolator = LinearOutSlowInInterpolator()
                                }
                                TransitionManager.beginDelayedTransition(
                                    itemBinding.root as ViewGroup,
                                    transition
                                )
                            }
                            is DownloadResult.Error -> {
                                snackMessage(
                                    itemBinding.root,
                                    getString(R.string.download_failed_tray_again)
                                )
                                itemBinding.btnAthanDownload.visibility = View.VISIBLE
                                itemBinding.progressAthanDownload.visibility = View.GONE
                                val transition = ChangeBounds().apply {
                                    interpolator = LinearOutSlowInInterpolator()
                                }
                                TransitionManager.beginDelayedTransition(
                                    itemBinding.root as ViewGroup,
                                    transition
                                )
                            }
                            is DownloadResult.Progress -> {
                                ObjectAnimator.ofInt(
                                    itemBinding.progressAthanDownload, "progress",
                                    itemBinding.progressAthanDownload.progress, it.progress
                                ).apply {
                                    interpolator = AccelerateDecelerateInterpolator()
                                    start()
                                }
                            }
                        }
                    }
                }
            }
        }//end of class AViewHolder
    }//end of class AthanAdapter
}//end of AthanDownloadDialog

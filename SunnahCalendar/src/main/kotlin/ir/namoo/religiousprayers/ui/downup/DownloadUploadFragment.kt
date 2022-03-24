package ir.namoo.religiousprayers.ui.downup

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentDownupBinding
import com.byagowi.persiancalendar.databinding.ItemAvailableCityBinding
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.update
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.service.PrayTimesService
import ir.namoo.commons.utils.getDatabasesDirectory
import ir.namoo.commons.utils.hideKeyBoard
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.snackMessage
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import javax.inject.Inject

@AndroidEntryPoint
class DownloadUploadFragment : Fragment() {
    private lateinit var binding: FragmentDownupBinding
    private val viewModel: DownloadUploadViewModel by viewModels()

    @Inject
    lateinit var prayTimesService: PrayTimesService

    @Inject
    lateinit var prayTimesDAO: PrayTimesDAO

    @Inject
    lateinit var locationsDB: LocationsDB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownupBinding.inflate(inflater, container, false)

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.download_upload)
            toolbar.setupMenuNavigation()
            toolbar.menu.add(R.string.update).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_refresh)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.onClick { updateView() }
            }
        }
        binding.appBar.root.hideToolbarBottomShadow()

        binding.recyclerViewAvailableCities.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AAdapter()
        binding.recyclerViewAvailableCities.adapter = adapter
        binding.txtAvailableCitySearch.addTextChangedListener {
            (binding.recyclerViewAvailableCities.adapter as AAdapter).filter.filter(it)
        }
        binding.txtAvailableCitySearch.setOnKeyListener { v, keyCode, _ ->
            return@setOnKeyListener if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideKeyBoard(v)
                true
            } else false
        }
        binding.txtAvailableCitySearch.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyBoard(v)
                true
            } else false
        }

        viewModel.addedCities.observe(viewLifecycleOwner) {
            binding.txtAvailableCitySearchLayout.isEnabled = true
            binding.progressAvailable.visibility = View.GONE
            adapter.setData(it)
            initUpload(it)
        }
        viewModel.downloaded.observe(viewLifecycleOwner) {
            adapter.setDownloaded(it)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateView()
    }

    private fun updateView() {
        if (isNetworkConnected(requireContext()))
            viewModel.loadAddedCities()
        else {
            onPause()
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.network_error_title)
                setMessage(R.string.network_error_message)
                setPositiveButton(R.string.str_retray) { dialog, _ ->
                    dialog.dismiss()
                    onResume()
                }
                show()
            }
        }
    }

    //#########################################
    private fun initUpload(list: List<CityModel>) {
        val existInServer = list.find {
            it.name == requireContext().appPrefs.getString(
                PREF_GEOCODED_CITYNAME,
                ""
            )
        } != null
        when {
            existInServer -> {
                binding.uploadRoot.visibility = View.VISIBLE
                binding.txtUploadTitle.text = getString(R.string.available_exact_times)
                binding.txtUploadTitle.setTextColor(
                    requireContext().resolveColor(R.attr.colorTextHoliday)
                )
                binding.btnUpload.visibility = View.GONE
            }
            viewModel.allEdited.value?.size == 366 -> {
                binding.uploadRoot.visibility = View.VISIBLE
                binding.txtUploadTitle.text = String.format(
                    getString(R.string.you_edited), " ${
                        requireContext().appPrefs.getString(
                            PREF_GEOCODED_CITYNAME, " - "
                        )
                    } "
                )
                binding.btnUpload.visibility = View.VISIBLE
                binding.btnUpload.setOnClickListener { v ->
                    v.startAnimation(
                        AnimationUtils.loadAnimation(
                            context,
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    MaterialAlertDialogBuilder(requireContext())
                        //                        .setTitle(R.string.)
                        .setMessage(getString(R.string.are_sure_send_times))
                        .setPositiveButton(
                            R.string.yes
                        ) { dialog: DialogInterface, _: Int ->
                            runCatching {
                                val city = JSONCity()
                                city.name =
                                    requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, "-")
                                city.lat =
                                    requireContext().appPrefs.getString(PREF_LATITUDE, "0.0")!!
                                        .toDouble()
                                city.lng =
                                    requireContext().appPrefs.getString(PREF_LONGITUDE, "0.0")!!
                                        .toDouble()
                                val allEdited =
                                    viewModel.allEdited.value ?: return@setPositiveButton
                                val times = arrayListOf<JSONPrayTime>()
                                for (t in allEdited) {
                                    val temp = JSONPrayTime()
                                    temp.dayNum = t.dayNumber
                                    temp.fajr = t.fajr
                                    temp.sunrise = t.sunrise
                                    temp.dhuhr = t.dhuhr
                                    temp.asr = t.asr
                                    temp.maghrib = t.maghrib
                                    temp.isha = t.isha
                                    times.add(temp)
                                }
                                val json = toJson(city, times)
                                val file =
                                    File(getDatabasesDirectory(requireContext()) + "${city.name}.json")
                                if (!file.exists())
                                    file.createNewFile()
                                val output = FileOutputStream(file)
                                output.write(
                                    json.toString().toByteArray(charset = Charset.forName("UTF-8"))
                                )
                                output.close()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_STREAM,
                                        FileProvider.getUriForFile(
                                            requireContext(),
                                            "${BuildConfig.APPLICATION_ID}.provider",
                                            file
                                        )
                                    )
                                    type = "text/json"
                                }
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        resources.getText(R.string.send_times)
                                    )
                                )
                            }.onFailure(logException).getOrElse {
                                snackMessage(
                                    binding.btnUpload,
                                    getString(R.string.error_sending_times)
                                )
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton(
                            R.string.no
                        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                        .show()
                }
            }
        }//end of when
    }//end of initUpload

    //    #########################################################
    private inner class AAdapter : RecyclerView.Adapter<AAdapter.AViewHolder>(), Filterable {
        private var filter = ""
        private var availableList = arrayListOf<CityModel>()
        private var filteredList = arrayListOf<CityModel>()
        private var downloadedList = arrayListOf<String>()
        private var prev: MaterialCardView? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AViewHolder {
            return AViewHolder(ItemAvailableCityBinding.inflate(layoutInflater, parent, false))
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setData(available: List<CityModel>) {
            availableList.clear()
            filteredList.clear()

            availableList.addAll(available)
            filteredList.addAll(available)

            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setDownloaded(downloaded: List<Int>) {
            lifecycleScope.launch {
                downloadedList.clear()
                for (d in downloaded)
                    downloadedList.add(locationsDB.cityDAO().getCity(d).name)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return filteredList.size
        }

        override fun onBindViewHolder(holder: AViewHolder, position: Int) {
            holder.bind(position, filteredList[position], filter)
        }

        override fun getItemViewType(position: Int): Int = position

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    if (constraint.toString().isEmpty() || constraint.toString().isBlank()) {
                        filter = ""
                        filteredList.clear()
                        filteredList.addAll(availableList)
                    } else {
                        filter = constraint.toString()
                        filteredList.clear()
                        filteredList.addAll(availableList.filter { it.name.contains(filter) })
                    }
                    val res = FilterResults()
                    res.values = filteredList
                    return res
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredList = results?.values as ArrayList<CityModel>
                    notifyDataSetChanged()
                    val transition = ChangeBounds().apply {
                        interpolator = LinearOutSlowInInterpolator()
                    }
                    TransitionManager.beginDelayedTransition(binding.cardAvailable, transition)
                }

            }
        }

        //#############################################################################
        inner class AViewHolder(val itemBinding: ItemAvailableCityBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            @SuppressLint("PrivateResource")
            fun bind(position: Int, city: CityModel, filter: String) {
                itemBinding.txtAvailableCityName.text = city.name
                if (city.name == requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, "")
                ) {
                    itemBinding.root.setCardBackgroundColor(
                        requireContext().resolveColor(R.attr.colorSelectCard)
                    )
                    prev = itemBinding.root
                }
                itemBinding.txtAvailableCityUpdateDate.text = city.lastUpdate

                if (!downloadedList.find { it == city.name }.isNullOrEmpty())
                    itemBinding.btnDownload.setImageResource(R.drawable.ic_synce)
                itemBinding.btnDownload.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (!isNetworkConnected(requireContext()))
                        updateView()
                    else {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) {
                                itemBinding.progressItemAvailable.visibility = View.VISIBLE
                            }
                            val prayTimes = prayTimesService.getPrayTimesFor(city.id)
                            viewModel.saveToDatabase(prayTimes)
                            val c = locationsDB.cityDAO().getCity(prayTimes.first().cityID)
                            requireContext().appPrefs.edit {
                                putString(PREF_GEOCODED_CITYNAME, c.name)
                                putString(PREF_LATITUDE, c.latitude.toString())
                                putString(PREF_LONGITUDE, c.longitude.toString())
                                putString(PREF_SELECTED_LOCATION, "")
                            }
                            update(requireContext(), true)
                            withContext(Dispatchers.Main) {
                                itemBinding.progressItemAvailable.visibility = View.GONE
                            }
                            notifyItemChanged(position)
                            prev?.setCardBackgroundColor(requireContext().resolveColor(R.attr.colorCard))
                        }
                    }
                }
                if (filter.isNotEmpty()) {
                    runCatching {
                        val fColorSpan = ForegroundColorSpan(
                            requireContext().resolveColor(R.attr.colorTextHoliday)
                        )
                        val spannableStringBuilder =
                            SpannableStringBuilder(itemBinding.txtAvailableCityName.text)
                        spannableStringBuilder.setSpan(
                            fColorSpan,
                            spannableStringBuilder.indexOf(filter),
                            spannableStringBuilder.indexOf(filter) + filter.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        itemBinding.txtAvailableCityName.text = spannableStringBuilder
                    }.onFailure(logException)
                }
            }
        }
    }
}//end of class DownloadUploadFragment

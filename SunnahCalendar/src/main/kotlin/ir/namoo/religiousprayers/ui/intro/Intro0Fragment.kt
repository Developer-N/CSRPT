package ir.namoo.religiousprayers.ui.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.PREF_LATITUDE
import com.byagowi.persiancalendar.PREF_LONGITUDE
import com.byagowi.persiancalendar.PREF_SELECTED_LOCATION
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentIntro0Binding
import com.byagowi.persiancalendar.databinding.ItemAvailableCityBinding
import com.byagowi.persiancalendar.ui.MainActivity
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.commons.PREF_FIRST_START
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.model.LocationsDB
import ir.namoo.commons.service.PrayTimesService
import ir.namoo.commons.utils.hideKeyBoard
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.modelToDBTimes
import ir.namoo.religiousprayers.praytimeprovider.DownloadedPrayTimesDAO
import ir.namoo.religiousprayers.ui.IntroActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class Intro0Fragment : Fragment() {
    private lateinit var binding: FragmentIntro0Binding
    private val adapter = AAdapter()

    @Inject
    lateinit var prayTimesService: PrayTimesService

    @Inject
    lateinit var downloadedPrayTimesDAO: DownloadedPrayTimesDAO

    @Inject
    lateinit var locationsDB: LocationsDB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntro0Binding.inflate(inflater, container, false)

        binding.recyclerViewIntro0Cities.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIntro0Cities.adapter = adapter
        binding.txtIntro0CitySearch.addTextChangedListener {
            (binding.recyclerViewIntro0Cities.adapter as AAdapter).filter.filter(it)
        }
        binding.txtIntro0CitySearch.setOnKeyListener { v, keyCode, _ ->
            return@setOnKeyListener if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideKeyBoard(v)
                true
            } else false
        }
        binding.txtIntro0CitySearch.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyBoard(v)
                true
            } else false
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateView()
    }

    fun updateView() {
        binding.txtIntro0CitySearch.text?.clear()
        if (!isNetworkConnected(requireContext())) {
            onPause()
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(resources.getString(R.string.network_error_title))
                setMessage(resources.getString(R.string.network_error_message))
                setPositiveButton(resources.getString(R.string.str_retray)) { dialog, _ ->
                    dialog.dismiss()
                    onResume()
                }
                setNegativeButton(resources.getString(R.string.next_page)) { dialog, _ ->
                    dialog.dismiss()
                    (requireActivity() as IntroActivity).goTo(1)
                }
                show()
            }
        } else {
            lifecycleScope.launch {
                if (locationsDB.countryDAO().getAllCountries().isNullOrEmpty()) {
                    locationsDB.countryDAO().insert(prayTimesService.getAllCountries())
                }
                if (locationsDB.provinceDAO().getAllProvinces().isNullOrEmpty()) {
                    locationsDB.provinceDAO().insert(prayTimesService.getAllProvinces())
                }
                if (locationsDB.cityDAO().getAllCity().isNullOrEmpty()) {
                    locationsDB.cityDAO().insert(prayTimesService.getAllCities())
                }
                val availableList = prayTimesService.getAddedCities()
                withContext(Dispatchers.Main) {
                    if (availableList.isNullOrEmpty()) {
                        binding.btnIntro0Next.text = resources.getString(R.string.custom_city)
                    } else {
                        adapter.setData(availableList)
                        binding.txtIntro0CitySearchLayout.isEnabled = true
                        binding.btnIntro0Next.text =
                            resources.getString(R.string.my_city_not_in_list)
                        binding.progressIntro0.visibility = View.GONE
                    }
                    binding.btnIntro0Next.setOnClickListener {
                        (requireActivity() as IntroActivity).goTo(1)
                    }
                }
            }
        }
    }


    //#########################################################
    private inner class AAdapter : RecyclerView.Adapter<AAdapter.AViewHolder>(), Filterable {
        private var filter = ""
        private var availableList = arrayListOf<CityModel>()
        private var filteredList = arrayListOf<CityModel>()

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

        override fun getItemCount(): Int {
            return filteredList.size
        }

        override fun onBindViewHolder(holder: AViewHolder, position: Int) {
            holder.bind(filteredList[position], filter)
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
                }

            }
        }

        //#############################################################################
        inner class AViewHolder(val itemBinding: ItemAvailableCityBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            @SuppressLint("PrivateResource")
            fun bind(city: CityModel, filter: String) {
                itemBinding.txtAvailableCityName.text = city.name

                itemBinding.txtAvailableCityUpdateDate.text = city.lastUpdate

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
                        itemBinding.progressItemAvailable.visibility = View.VISIBLE
                        itemBinding.btnDownload.visibility = View.INVISIBLE
                        runCatching {
                            lifecycleScope.launch {
                                val prayTimes = prayTimesService.getPrayTimesFor(city.id)
                                downloadedPrayTimesDAO.clearDownloadFor(prayTimes.first().id)
                                downloadedPrayTimesDAO.insertToDownload(modelToDBTimes(prayTimes))
                                val c = locationsDB.cityDAO().getCity(prayTimes.first().cityID)
                                requireContext().appPrefs.edit {
                                    putString(PREF_GEOCODED_CITYNAME, c.name)
                                    putString(PREF_LATITUDE, c.latitude.toString())
                                    putString(PREF_LONGITUDE, c.longitude.toString())
                                    putString(PREF_SELECTED_LOCATION, "")
                                    putBoolean(PREF_FIRST_START, false)
                                }
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                requireActivity().finish()
                            }
                        }.onFailure {
                            itemBinding.progressItemAvailable.visibility = View.GONE
                            itemBinding.btnDownload.visibility = View.VISIBLE
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
}//end of class Intro0Fragment

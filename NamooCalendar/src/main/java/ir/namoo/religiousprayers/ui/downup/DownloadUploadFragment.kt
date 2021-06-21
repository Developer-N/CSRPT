package ir.namoo.religiousprayers.ui.downup

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.textview.MaterialTextView
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.FragmentDownupBinding
import ir.namoo.religiousprayers.databinding.ItemAvailableCityBinding
import ir.namoo.religiousprayers.praytimes.*
import ir.namoo.religiousprayers.utils.*
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.*

class DownloadUploadFragment : Fragment() {
    private lateinit var binding: FragmentDownupBinding

    private var list = arrayListOf<CityList>()
    private var filteredList = arrayListOf<CityList>()
    private var downloadedList = arrayListOf<String>()
    private var prev: MaterialTextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownupBinding.inflate(inflater, container, false).apply {
            appBar.toolbar.let {
                it.setTitle(R.string.download_upload)
                it.setupUpNavigation()
            }
        }
        binding.recyclerViewAvailableCities.layoutManager = LinearLayoutManager(requireContext())
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
        binding.appBar.let {
            it.toolbar.inflateMenu(R.menu.ud_menu)
            it.toolbar.setOnMenuItemClickListener { clickedMenuItem ->
                when (clickedMenuItem?.itemId) {
                    R.id.mnu_ud_refresh ->
                        updateView()
                }
                true
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateView()
    }

    private fun updateView() {
        if (!isNetworkConnected(requireContext())) {
            onPause()
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(resources.getString(R.string.network_error_title))
            alert.setMessage(resources.getString(R.string.network_error_message))
            alert.setPositiveButton(resources.getString(R.string.str_retray)) { dialog, _ ->
                dialog.dismiss()
                onResume()
            }
            alert.create().show()
        } else
            GetListTask().execute()
    }

    @SuppressLint("PrivateResource")
    private fun initUpload() {
        val existInServer = list.find {
            it.name == requireContext().appPrefs.getString(
                PREF_GEOCODED_CITYNAME,
                ""
            )
        } != null
        when {
            existInServer -> {
                binding.txtUploadTitle.text = getString(R.string.available_exact_times)
                binding.txtUploadTitle.setTextColor(
                    requireContext().resolveColor(R.attr.colorTextHoliday)
                )
                binding.btnUpload.visibility = View.GONE
            }
            PrayTimesDB.getInstance(requireContext()).prayTimes()
                .getAllEdited()?.size == 366 -> { //have custom
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
//                    val ik =
//                        isOk(
//                            PrayTimesDB.getInstance(requireContext()).prayTimes().getAllEdited()
//                        )
//                    if (ik == "OK") {
                    val alert =
                        AlertDialog.Builder(requireContext())
                    //                        alert.setTitle(R.string.)
                    alert.setMessage(getString(R.string.are_sure_send_times))
                    alert.setPositiveButton(
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
                                PrayTimesDB.getInstance(requireContext().applicationContext)
                                    .prayTimes()
                                    .getAllEdited() ?: return@setPositiveButton
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
                            snackMessage(binding.btnUpload, getString(R.string.error_sending_times))
                        }
                        dialog.dismiss()
                    }
                    alert.setNegativeButton(
                        R.string.no
                    ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    alert.create().show()
//                    } else {
//                        binding.txtUploadTitle.text = ik
//                    }

                }
            }
            else -> { // no custom db
                binding.txtUploadTitle.text = getString(R.string.edit_times_first)
                binding.txtUploadTitle.setTextColor(
                    requireContext().resolveColor(R.attr.colorTextHoliday)
                )
                binding.btnUpload.isEnabled = false
            }
        }
        //###################### old edited
        val oldDB = File(getDatabasesDirectory(requireContext().applicationContext) + "cpt.db")
        if (oldDB.exists()) {
            binding.txtUploadTitleOld.visibility = View.VISIBLE
            binding.btnUploadOld.visibility = View.VISIBLE
            binding.btnUploadOld.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        com.google.android.material.R.anim.abc_fade_in
                    )
                )
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            requireContext(),
                            "${BuildConfig.APPLICATION_ID}.provider",
                            oldDB
                        )
                    )
                    type = "application/octet-stream"
                }
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        resources.getText(R.string.send_times)
                    )
                )
            }
        }

    }//end of initUpload

    //###################################################### GetListTask
    @SuppressLint("StaticFieldLeak")
    inner class GetListTask : AsyncTask<String, Int, String>() {
        override fun doInBackground(vararg params: String?): String {
            runCatching {
                val httpclient: HttpClient = DefaultHttpClient()
                val httpGet = HttpGet("http://www.namoo.ir/Home/GetAddedCities")
                val response: HttpResponse = httpclient.execute(httpGet)
                if (response.statusLine.statusCode == 200) {
                    val serverResponse = EntityUtils.toString(response.entity)
                    val parser = JSONParser()
                    val jsonArray: JSONArray = parser.parse(serverResponse) as JSONArray
                    val jsonObjectIterator: MutableIterator<Any?> = jsonArray.iterator()
                    list.clear()
                    while (jsonObjectIterator.hasNext()) {
                        val jt: JSONObject = jsonObjectIterator.next() as JSONObject
                        val t = CityList()
                        t.id = jt["id"].toString().toInt()
                        t.name = jt["cityName"].toString()
                        t.setInsertDate(jt["lastUpdate"].toString())
                        list.add(t)
                    }
                    list.sortBy { it.name }
                    filteredList.clear()
                    filteredList.addAll(list)
                    downloadedList.addAll(
                        DPTDB.getInstance(requireContext().applicationContext).downloadedPrayTimes()
                            .getCities()!!
                    )
                } else return "error"
            }.onFailure(logException)
            return "OK"
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (list.isNotEmpty() && result == "OK") {
                binding.txtAvailableCitySearchLayout.isEnabled = true
                binding.progressAvailable.visibility = View.GONE
                binding.recyclerViewAvailableCities.removeAllViews()
                binding.recyclerViewAvailableCities.adapter = AAdapter()
                initUpload()
            } else
                Log.e(TAG, "onPostExecute: error ")
        }
    }

    //    #########################################################
    private inner class AAdapter : RecyclerView.Adapter<AAdapter.AViewHolder>(), Filterable {
        private var filter = ""

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AViewHolder {
            return AViewHolder(ItemAvailableCityBinding.inflate(layoutInflater, parent, false))
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
                        filteredList.addAll(list)
                    } else {
                        filter = constraint.toString()
                        filteredList.clear()
                        filteredList.addAll(list.filter { it.name.contains(filter) })
                    }
                    val res = FilterResults()
                    res.values = filteredList
                    return res
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredList = results?.values as ArrayList<CityList>
                    notifyDataSetChanged()
                    val transition = ChangeBounds().apply {
                        interpolator = LinearOutSlowInInterpolator()
                    }
                    TransitionManager.beginDelayedTransition(binding.cardAvailable, transition)
                }

            }
        }

        //#############################################################################
        inner class AViewHolder(val binding: ItemAvailableCityBinding) :
            RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("PrivateResource")
            fun bind(position: Int, city: CityList, filter: String) {
                binding.txtAvailableCityName.text = city.name
                if (city.name == requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, "")) {
                    binding.txtAvailableCityName.setTextColor(
                        requireContext().resolveColor(R.attr.colorTextPrimary)
                    )
                    prev = binding.txtAvailableCityName
                }
                binding.txtAvailableCityUpdateDate.text = formatDate(
                    Jdn(Date(city.insertDate.toLong()).toJavaCalendar().toCivilDate()).toCalendar(mainCalendar)
                )

                if (!downloadedList.find { it == city.name }.isNullOrEmpty())
                    binding.btnDownload.setImageResource(R.drawable.ic_synce)
                binding.btnDownload.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (!isHaveStoragePermission(requireActivity())) {
                        askForStoragePermission(requireActivity())
                        return@setOnClickListener
                    }
                    if (!isNetworkConnected(requireContext()))
                        updateView()
                    else
                        GetPrayTimeTask(position, city.id).execute()
                }
                if (filter.isNotEmpty()) {
                    runCatching {
                        val fColorSpan = ForegroundColorSpan(
                            requireContext().resolveColor(R.attr.colorHighlight)
                        )
                        val spannableStringBuilder =
                            SpannableStringBuilder(binding.txtAvailableCityName.text)
                        spannableStringBuilder.setSpan(
                            fColorSpan,
                            spannableStringBuilder.indexOf(filter),
                            spannableStringBuilder.indexOf(filter) + filter.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.txtAvailableCityName.text = spannableStringBuilder
                    }.onFailure(logException)
                }
            }

            @SuppressLint("StaticFieldLeak")
            inner class GetPrayTimeTask(val position: Int, val id: Int) :
                AsyncTask<String, Int, String>() {
                private val url = "http://www.namoo.ir/Home/GetPTime/"

                override fun doInBackground(vararg params: String?): String = runCatching {
                    val httpclient: HttpClient = DefaultHttpClient()
                    val httpGet = HttpGet("$url$id")
                    val response = httpclient.execute(httpGet)
                    if (response.statusLine.statusCode == 200) {
                        val serverResponse = EntityUtils.toString(response.entity)
                        val outFile = File(
                            "${getTimesDirectoryPath(requireContext())}/${
                                getCity(
                                    serverResponse
                                )?.name ?: "_"
                            }"
                        )
                        val outStream = FileOutputStream(outFile)
                        if (!outFile.exists())
                            outFile.createNewFile()
                        outStream.write(serverResponse.toByteArray(charset = Charset.forName("UTF-8")))
                        outStream.flush()
                        outStream.close()
                        val city = getCity(serverResponse)
                        if (city != null) {
                            requireContext().appPrefs.edit {
                                putString(PREF_GEOCODED_CITYNAME, city.name)
                                putString(PREF_LATITUDE, city.lat.toString())
                                putString(PREF_LONGITUDE, city.lng.toString())
                            }
                            val times = getPrayTimes(serverResponse)
                            if (times != null) {
                                val db =
                                    DPTDB.getInstance(requireContext().applicationContext)
                                        .downloadedPrayTimes()
                                if (db.getDownloadFor(city.name!!) != null)
                                    db.clearDownloadFor(city.name!!)
                                db.insertToDownload(getPrayTimes(serverResponse)!!)
                            }
                        }
                        initUtils(requireContext().applicationContext)
                        loadApp(requireContext())
                        update(requireContext(), true)
                        "OK"
                    } else
                        "Error"
                }.onFailure(logException).getOrDefault("Error get time")

                override fun onPreExecute() {
                    super.onPreExecute()
                    binding.progressItemAvailable.visibility = View.VISIBLE
                }

                override fun onPostExecute(result: String?) {
                    super.onPostExecute(result)
                    binding.progressItemAvailable.visibility = View.GONE
                    downloadedList.clear()
                    downloadedList.addAll(
                        DPTDB.getInstance(requireContext()).downloadedPrayTimes().getCities()!!
                    )
                    notifyItemChanged(position)
                    prev?.setTextColor(requireContext().resolveColor(R.attr.colorTextNormal))
                }
            }
        }
    }
}//end of DownloadUploadFragment

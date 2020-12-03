package ir.namoo.religiousprayers.ui.intro

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.FragmentIntro0Binding
import ir.namoo.religiousprayers.databinding.ItemAvailableCityBinding
import ir.namoo.religiousprayers.praytimes.DPTDB
import ir.namoo.religiousprayers.praytimes.PrayTimesDB
import ir.namoo.religiousprayers.praytimes.getCity
import ir.namoo.religiousprayers.praytimes.getPrayTimes
import ir.namoo.religiousprayers.ui.IntroActivity
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.ui.downup.CityList
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

class Intro0Fragment : Fragment() {
    private lateinit var binding: FragmentIntro0Binding
    private var list = arrayListOf<CityList>()
    private var filteredList = arrayListOf<CityList>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentIntro0Binding.inflate(inflater, container, false)

        binding.recyclerViewIntro0Cities.layoutManager = LinearLayoutManager(requireContext())
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
            val alert: AlertDialog.Builder = AlertDialog.Builder(context)
            alert.setTitle(resources.getString(R.string.network_error_title))
            alert.setMessage(resources.getString(R.string.network_error_message))
            alert.setPositiveButton(resources.getString(R.string.str_retray)) { dialog, _ ->
                dialog.dismiss()
                onResume()
            }
            alert.setNegativeButton(resources.getString(R.string.next_page)) { dialog, _ ->
                dialog.dismiss()
                (requireActivity() as IntroActivity).goTo(1)
            }
            alert.create().show()
        } else
            GetListTask().execute()
    }

    //###################################################### GetListTask
    @SuppressLint("StaticFieldLeak")
    inner class GetListTask : AsyncTask<String, Int, String>() {
        override fun doInBackground(vararg params: String?): String {
            try {
                val httpclient: HttpClient = DefaultHttpClient()
                val httpGet =
                    HttpGet("http://www.namoo.ir/Home/GetAddedCities")
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
                } else return "error"
            } catch (ex: Exception) {
                Log.d(TAG, "Error get available cities!$ex")
            }
            return "OK"
        }

        override fun onPreExecute() {
            super.onPreExecute()
            binding.progressIntro0.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (list.isNotEmpty() && result == "OK") {
                binding.txtIntro0CitySearchLayout.isEnabled = true
                binding.recyclerViewIntro0Cities.removeAllViews()
                binding.recyclerViewIntro0Cities.adapter = AAdapter()
                binding.btnIntro0Next.text = resources.getString(R.string.my_city_not_in_list)
                binding.progressIntro0.visibility = View.GONE
            } else {
                Log.e(TAG, "onPostExecute: error ")
                binding.btnIntro0Next.text = resources.getString(R.string.custom_city)
            }
            binding.btnIntro0Next.setOnClickListener {
                (requireActivity() as IntroActivity).goTo(1)
            }
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
            holder.bind(filteredList[position], filter)
        }

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
                }

            }
        }

        //#############################################################################
        inner class AViewHolder(val ItemBinding: ItemAvailableCityBinding) :
            RecyclerView.ViewHolder(ItemBinding.root) {
            init {
                ItemBinding.txtAvailableCityName.setTextIsSelectable(false)
                ItemBinding.txtAvailableCityName.isClickable = false
                ItemBinding.txtAvailableCityUpdateDate.visibility = View.GONE
            }

            @SuppressLint("PrivateResource")
            fun bind(city: CityList, filter: String) {
                ItemBinding.btnDownload.setImageResource(R.drawable.ic_check)
                ItemBinding.txtAvailableCityName.text = city.name
                ItemBinding.txtAvailableCityUpdateDate.text = formatDate(
                    getDateFromJdnOfCalendar(
                        mainCalendar,
                        calendarToCivilDate(makeCalendarFromDate(Date(city.insertDate.toLong()))).toJdn()
                    )
                )
                ItemBinding.btnDownload.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    download(city.id)
                }
                ItemBinding.root.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    download(city.id)
                }
                if (filter.isNotEmpty()) {
                    try {
                        val fColorSpan = ForegroundColorSpan(
                            getColorFromAttr(
                                itemView.context,
                                R.attr.colorHighlight
                            )
                        )
                        val spannableStringBuilder =
                            SpannableStringBuilder(ItemBinding.txtAvailableCityName.text)
                        spannableStringBuilder.setSpan(
                            fColorSpan,
                            spannableStringBuilder.indexOf(filter),
                            spannableStringBuilder.indexOf(filter) + filter.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        ItemBinding.txtAvailableCityName.text = spannableStringBuilder
                    } catch (ex: Exception) {

                    }

                }
            }

            @SuppressLint("StaticFieldLeak")
            inner class GetPrayTimeTask(val id: Int) :
                AsyncTask<String, Int, String>() {
                private val url = "http://www.namoo.ir/Home/GetPTime/"

                override fun doInBackground(vararg params: String?): String {
                    return try {
                        val httpclient: HttpClient = DefaultHttpClient()
                        val httpGet = HttpGet("$url$id")
                        val response = httpclient.execute(httpGet)
                        if (response.statusLine.statusCode == 200) {
                            val serverResponse = EntityUtils.toString(response.entity)
                            val outFile = File(
                                "${getTimesDirectoryPath(requireContext())}/${getCity(
                                    serverResponse
                                )?.name ?: "_"}"
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
                                    putBoolean(PREF_FIRST_START, false)
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
                            "OK"
                        } else
                            "Error"
                    } catch (ex: Exception) {
                        "Error get time : ${ex.message}"
                    }
                }

                override fun onPreExecute() {
                    super.onPreExecute()
                    ItemBinding.progressItemAvailable.visibility = View.VISIBLE
                    binding.btnIntro0Next.isEnabled = false
                    binding.txtIntro0CitySearchLayout.isEnabled = false
                    binding.recyclerViewIntro0Cities.isEnabled = false
                }

                override fun onPostExecute(result: String?) {
                    super.onPostExecute(result)
                    if (result == "OK") {
                        requireActivity().startActivity(
                            Intent(
                                requireActivity(),
                                MainActivity::class.java
                            )
                        )
                        requireActivity().finish()
                    } else {
                        ItemBinding.progressItemAvailable.visibility = View.GONE
                        binding.btnIntro0Next.isEnabled = true
                        binding.txtIntro0CitySearchLayout.isEnabled = true
                        binding.recyclerViewIntro0Cities.isEnabled = true
                    }
                }
            }

            fun download(id: Int) {
                if (!isHaveStoragePermission(requireActivity()))
                    askForStoragePermission(requireActivity())
                else if (!isNetworkConnected(requireContext()))
                    updateView()
                else
                    GetPrayTimeTask(id).execute()
            }
        }
    }
}//end of class Intro0Fragment
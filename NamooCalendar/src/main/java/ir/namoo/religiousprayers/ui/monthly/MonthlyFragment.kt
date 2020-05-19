package ir.namoo.religiousprayers.ui.monthly

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.PREF_GEOCODED_CITYNAME
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.appLink
import ir.namoo.religiousprayers.databinding.FragmentMonthlyBinding
import ir.namoo.religiousprayers.databinding.ItemMonthlyBinding
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.ui.edit.ShapedAdapter
import ir.namoo.religiousprayers.utils.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MonthlyFragment : Fragment() {
    private lateinit var binding: FragmentMonthlyBinding
    private val mAdapter = MonthlyAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMonthlyBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).setTitleAndSubtitle(
            getString(R.string.monthly_times),
            ""
        )
        setHasOptionsMenu(true)

        binding.spinnerMonthly.adapter = ShapedAdapter(
            requireContext(), R.layout.select_dialog_item,
            persianMonths.toTypedArray()
        )
        binding.spinnerMonthly.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updateRecycler(position)
                }

            }
        binding.recyclerViewMonthly.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMonthly.adapter = mAdapter
        return binding.root
    }//end of onCreateView

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.monthly_menu, menu)
    }

    @SuppressLint("WrongThread")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_monthly_share -> {
                binding.recyclerViewMonthly.setBackgroundColor(
                    getColorFromAttr(
                        requireContext(),
                        R.attr.colorBackground
                    )
                )
                try {
                    MediaPlayer().apply {
                        setDataSource(
                            requireContext(),
                            (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                                    resources.getResourcePackageName(R.raw.camera_shutter_click) + "/" +
                                    resources.getResourceTypeName(R.raw.camera_shutter_click) + "/" +
                                    resources.getResourceEntryName(R.raw.camera_shutter_click)).toUri()
                        )
                        setVolume(6f, 6f)
                        prepare()
                    }.start()
                } catch (ex: Exception) {
                }
                val photo = createBitmapFromView3(binding.recyclerViewMonthly)
                val path = requireContext().getExternalFilesDir("pic")?.absolutePath
                    ?: ""
                val f = File(path)
                if (!f.exists()) f.mkdirs()

                val file = File("$path/share.png")
                try {
                    val out = FileOutputStream(file)
                    photo.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                } catch (ex: Exception) {
                    Log.e(TAG, "on share error ", ex)
                }
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(
                        Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            requireContext(),
                            "ir.namoo.religiousprayers.fileprovider",
                            file
                        )
                    )
                    val text =
                        "${getString(R.string.owghat)} ${getString(R.string.in_city_time)} " +
                                "${requireContext().appPrefs.getString(
                                    PREF_GEOCODED_CITYNAME,
                                    ""
                                )}" +
                                "\n\n" +
                                "${getString(R.string.app_name)}\n$appLink"
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "*/*"
                }
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                true
            }

            else ->
                false
        }
    }


    private fun updateRecycler(month: Int) {
        RecyclerUpdater(month + 1).execute()
    }

    inner class MonthlyAdapter :
        RecyclerView.Adapter<MonthlyAdapter.MViewHolder>() {

        private val list = arrayListOf<PrayTimes>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder =
            MViewHolder(
                ItemMonthlyBinding.inflate(layoutInflater, parent, false)
            )

        override fun getItemCount(): Int = list.size

        override fun onBindViewHolder(holder: MViewHolder, position: Int) =
            holder.bind(
                list[position],
                position + 1,
                binding.spinnerMonthly.selectedItem.toString()
            )

        fun add(time: PrayTimes) {
            list.add(time)
            notifyItemInserted(if (list.size - 1 >= 0) list.size - 1 else 0)
        }

        fun clear() {
            list.clear()
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int = position


        inner class MViewHolder(val binding: ItemMonthlyBinding) :
            RecyclerView.ViewHolder(binding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(time: PrayTimes, day: Int, month: String) {
                binding.txtItemMonthlyDaySummary.text = formatNumber("$day $month")
                val dayLength = Clock.fromInt(time.maghribClock.toInt() - time.fajrClock.toInt())
                binding.txtItemMonthlyDayLength.text = formatNumber(
                    String.format(
                        getString(R.string.length_of_day),
                        dayLength.hour,
                        dayLength.minute
                    )
                )

                binding.txtItemMonthlyFajr.text = formatNumber(
                    "${getString(R.string.fajr)}\n${time.fajrClock.toFormattedString()}"
                )
                binding.txtItemMonthlySunrise.text = formatNumber(
                    "${getString(R.string.sunrise)}\n${time.sunriseClock.toFormattedString()}"
                )
                binding.txtItemMonthlyDhuhr.text = formatNumber(
                    "${getString(R.string.dhuhr)}\n${time.dhuhrClock.toFormattedString()}"
                )
                binding.txtItemMonthlyAsr.text = formatNumber(
                    "${getString(R.string.asr)}\n${time.asrClock.toFormattedString()}"
                )
                binding.txtItemMonthlyMaghrib.text = formatNumber(
                    "${getString(R.string.maghrib)}\n${time.maghribClock.toFormattedString()}"
                )
                binding.txtItemMonthlyIsha.text = formatNumber(
                    "${getString(R.string.isha)}\n${time.ishaClock.toFormattedString()}"
                )
            }
        }//end of MViewHolder

    }//end of MonthlyAdapter

    @SuppressLint("StaticFieldLeak")
    private inner class RecyclerUpdater(val month: Int) : AsyncTask<String, PrayTimes, String>() {
        override fun doInBackground(vararg params: String?): String {
            val monthDays = if (month in 1..6) 31 else 30
            val civilDate = calendarToCivilDate(makeCalendarFromDate(Date()))
            for (day in 1..monthDays) {
                val persianDate = PersianDate(PersianDate(civilDate.toJdn()).year, month, day)
                val date = CivilDate(persianDate.toJdn()).toCalendar().time
                publishProgress(
                    PrayTimeProvider.calculate(
                        calculationMethod,
                        date,
                        coordinate!!,
                        requireContext()
                    )
                )
            }
            return "OK"
        }

        override fun onProgressUpdate(vararg values: PrayTimes?) {
            super.onProgressUpdate(*values)
            if (values.isNotEmpty())
                for (p in values)
                    if (p != null)
                        mAdapter.add(p)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            mAdapter.clear()
        }
    }
}//end of class
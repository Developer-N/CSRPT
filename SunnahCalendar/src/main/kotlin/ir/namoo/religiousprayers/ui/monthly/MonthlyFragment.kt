package ir.namoo.religiousprayers.ui.monthly

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthlyBinding
import com.byagowi.persiancalendar.databinding.ItemMonthlyBinding
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.persianMonths
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.ui.utils.setupUpNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.logException
import dagger.hilt.android.AndroidEntryPoint
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.appLink
import ir.namoo.commons.utils.createBitmapFromView3
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import ir.namoo.religiousprayers.ui.shared.ShapedAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

@AndroidEntryPoint
class MonthlyFragment : Fragment() {
    private lateinit var binding: FragmentMonthlyBinding
    private val mAdapter = MonthlyAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthlyBinding.inflate(inflater, container, false)

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.monthly_times)
            toolbar.subtitle = requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, "")
            toolbar.setupMenuNavigation()
            toolbar.menu.add(R.string.share).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_camera)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.onClick {
                    binding.recyclerViewMonthly.setBackgroundColor(
                        requireContext().resolveColor(R.attr.colorBackground)
                    )
                    runCatching {
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
                    }.onFailure(logException)
                    val photo = createBitmapFromView3(binding.recyclerViewMonthly)
                    val path = requireContext().getExternalFilesDir("pic")?.absolutePath
                        ?: ""
                    val f = File(path)
                    if (!f.exists()) f.mkdirs()

                    val file = File("$path/share.png")
                    runCatching {
                        val out = FileOutputStream(file)
                        photo.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()
                    }.onFailure(logException)
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(
                            Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                requireContext(),
                                "${BuildConfig.APPLICATION_ID}.provider",
                                file
                            )
                        )
                        val text =
                            "${getString(R.string.owghat)} ${
                                String.format(
                                    getString(R.string.in_city_time),
                                    requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, "")
                                )
                            }\n\n" +
                                    "${getString(R.string.app_name)}\n$appLink"
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "*/*"
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                }
            }
        }
        binding.appBar.root.hideToolbarBottomShadow()

        binding.spinnerMonthly.adapter = ShapedAdapter(
            requireContext(), R.layout.select_dialog_item, R.id.text1,
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
                    updateRecycler(position + 1)
                }

            }
        binding.recyclerViewMonthly.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMonthly.adapter = mAdapter

        return binding.root
    }//end of onCreateView

    fun updateRecycler(month: Int) {
        lifecycleScope.launchWhenStarted {
            mAdapter.clear()
            val monthDays = if (month in 1..6) 31 else 30
            val civilDate = Jdn.today().toGregorianCalendar()
            for (day in 1..monthDays) {
                val persianDate = PersianDate(PersianDate(civilDate.toJdn()).year, month, day)
                val date = CivilDate(persianDate.toJdn())
                var prayTimes: PrayTimes? =
                    coordinates?.calculatePrayTimes(Jdn(date).toJavaCalendar())
                prayTimes = PrayTimeProvider(requireContext()).nReplace(prayTimes, Jdn(date))
                if (prayTimes == null) return@launchWhenStarted
                withContext(Dispatchers.Main) {
                    mAdapter.add(prayTimes)
                }
            }
        }
    }

    inner class MonthlyAdapter : RecyclerView.Adapter<MonthlyAdapter.MViewHolder>() {

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

        @SuppressLint("NotifyDataSetChanged")
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
                val fajr = Clock.fromHoursFraction(time.fajr).toMinutes().toFloat()
                val maghrib = Clock.fromHoursFraction(time.maghrib).toMinutes().toFloat()
                val dayLength = Clock.fromMinutesCount((maghrib - fajr).toInt())
                binding.txtItemMonthlyDayLength.text =
                    getString(R.string.length_of_day) + spacedColon +
                            dayLength.asRemainingTime(resources, short = false)

                binding.txtItemMonthlyFajr.text = formatNumber(
                    "${getString(R.string.fajr)}\n${
                        time.getFromStringId(R.string.fajr).toFormattedString()
                    }"
                )
                binding.txtItemMonthlySunrise.text = formatNumber(
                    "${getString(R.string.sunrise)}\n${
                        time.getFromStringId(R.string.sunrise).toFormattedString()
                    }"
                )
                binding.txtItemMonthlyDhuhr.text = formatNumber(
                    "${getString(R.string.dhuhr)}\n${
                        time.getFromStringId(R.string.dhuhr).toFormattedString()
                    }"
                )
                binding.txtItemMonthlyAsr.text = formatNumber(
                    "${getString(R.string.asr)}\n${
                        time.getFromStringId(R.string.asr).toFormattedString()
                    }"
                )
                binding.txtItemMonthlyMaghrib.text = formatNumber(
                    "${getString(R.string.maghrib)}\n${
                        time.getFromStringId(R.string.maghrib).toFormattedString()
                    }"
                )
                binding.txtItemMonthlyIsha.text = formatNumber(
                    "${getString(R.string.isha)}\n${
                        time.getFromStringId(R.string.isha).toFormattedString()
                    }"
                )
            }
        }//end of MViewHolder
    }//end of MonthlyAdapter
}//end of EditFragment

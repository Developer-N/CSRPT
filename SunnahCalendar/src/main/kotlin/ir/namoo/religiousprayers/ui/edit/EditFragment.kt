package ir.namoo.religiousprayers.ui.edit

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentEditBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.getFromStringId
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.github.persiancalendar.calendar.CivilDate
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.PREF_ENABLE_EDIT
import ir.namoo.commons.PREF_SUMMER_TIME
import ir.namoo.commons.utils.animateVisibility
import ir.namoo.commons.utils.fixSummerTimes
import ir.namoo.commons.utils.fixTime
import ir.namoo.commons.utils.getDayMonthForDayOfYear
import ir.namoo.commons.utils.getDayNum
import ir.namoo.religiousprayers.praytimeprovider.EditedPrayTimesEntity
import ir.namoo.religiousprayers.praytimeprovider.PrayTimesDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private val viewModel: EditViewModel by viewModels()
    private lateinit var times: MutableList<EditedPrayTimesEntity>
    private lateinit var originalTimes: MutableList<EditedPrayTimesEntity>

    @Inject
    lateinit var prayTimesDB: PrayTimesDB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.edit_times)
            toolbar.setupMenuNavigation()
            toolbar.menu.add(R.string.apply).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_check)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.onClick {
                    if (!binding.switchEnableEdit.isChecked) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.edit_not_enabled),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(getString(R.string.str_dialog_save))
                            setMessage(getString(R.string.str_dialog_save_message))
                            setPositiveButton(R.string.yes) { _, _ ->
                                updateDB()
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.saved),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                            setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                            show()
                        }
                    }
                }
            }
            toolbar.menu.add(R.string.group_change).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                it.onClick {
                    if (!binding.switchEnableEdit.isChecked) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.edit_not_enabled),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        val dialog = GEditDialog()
                        dialog.show(childFragmentManager, GEditDialog::class.java.name)
                    }
                }
            }
            toolbar.menu.add(R.string.clear_change).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                it.onClick {
                    if (!binding.switchEnableEdit.isChecked) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.edit_not_enabled),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(getString(R.string.str_dialog_clear_edited_title))
                            setMessage(getString(R.string.str_dialog_clear_edited_message))
                            setPositiveButton(R.string.yes) { _, _ ->
                                lifecycleScope.launch(Dispatchers.Main) {
                                    prayTimesDB.prayTimes().cleanEditedPrayTimes()
                                    binding.switchEnableEdit.isChecked = false
                                    requireActivity().onBackPressed()
                                }
                            }
                            setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                            show()
                        }
                    }
                }
            }

        }
        binding.appBar.root.hideToolbarBottomShadow()
        binding.progressEdit.max = 366
        binding.progressEdit.progress = 0
        val enabled = requireContext().appPrefs.getBoolean(PREF_ENABLE_EDIT, false)
        binding.switchEnableEdit.isChecked = enabled
        binding.switchEnableEdit.setOnCheckedChangeListener { _, isChecked ->
            run {
                requireContext().appPrefs.edit {
                    putBoolean(PREF_ENABLE_EDIT, isChecked)
                }
                if (isChecked) {
                    updateView()
                    enableDisableAllPickers(true)
                } else enableDisableAllPickers(false)
            }
        }

        if (enabled) {
            updateView()
        } else {
            enableDisableAllPickers(true)
        }
        return binding.root
    }

    private fun enableDisableAllPickers(enable: Boolean) {
        binding.pickerIshaMinute.isEnabled = enable
        binding.pickerIshaHour.isEnabled = enable
        binding.pickerMaghribMinute.isEnabled = enable
        binding.pickerMaghribHour.isEnabled = enable
        binding.pickerAsrMinute.isEnabled = enable
        binding.pickerAsrHour.isEnabled = enable
        binding.pickerDhuhrMinute.isEnabled = enable
        binding.pickerDhuhrHour.isEnabled = enable
        binding.pickerFajrMinute.isEnabled = enable
        binding.pickerFajrHour.isEnabled = enable
        binding.pickerSunriseMinute.isEnabled = enable
        binding.pickerSunriseHour.isEnabled = enable
        binding.pickerDay.isEnabled = enable
        binding.pickerMonth.isEnabled = enable
    }

    private fun updateView() {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                binding.txtEditInfo.text = resources.getString(R.string.please_wait)
                animateVisibility(binding.progressEdit, true)
                binding.switchEnableEdit.isEnabled = false
            }
            if (!(prayTimesDB.prayTimes().getAllEdited() != null &&
                        prayTimesDB.prayTimes().getAllEdited()?.isNullOrEmpty() == false)
            ) {
                val times = calAll()
                val forEdits = arrayListOf<EditedPrayTimesEntity>()
                var id = 1
                for (t in times) {
                    if (t != null) {
                        val temp =
                            EditedPrayTimesEntity(
                                id, id,
                                t.getFromStringId(R.string.fajr).toFormattedString(),
                                t.getFromStringId(R.string.sunrise).toFormattedString(),
                                t.getFromStringId(R.string.dhuhr).toFormattedString(),
                                t.getFromStringId(R.string.asr).toFormattedString(),
                                t.getFromStringId(R.string.maghrib).toFormattedString(),
                                t.getFromStringId(R.string.isha).toFormattedString()
                            )
                        id++
                        forEdits.add(temp)
                    }
                }

                prayTimesDB.prayTimes().insertEdited(forEdits)
            }
            originalTimes = mutableListOf()
            prayTimesDB.prayTimes().getAllEdited()?.let {
                originalTimes.addAll(it)
            }

            withContext(Dispatchers.Main) {
                binding.txtEditInfo.text =
                    formatNumber(resources.getString(R.string.use_summer_time))
                initPickers()
                viewModel.timeList.observe(requireActivity()) {
                    times = arrayListOf()
                    times.addAll(it)

                    viewModel.dayNum.observe(requireActivity()) { dayNum ->
                        run {
                            val time = times.find { t -> t.dayNumber == dayNum }
                            val originalTime = originalTimes.find { t -> t.dayNumber == dayNum }
                            if (time != null) {
                                binding.pickerMonth.value =
                                    getDayMonthForDayOfYear(time.dayNumber).split("/")[0].toInt()
                                binding.pickerDay.value =
                                    getDayMonthForDayOfYear(time.dayNumber).split("/")[1].toInt()

                                binding.pickerFajrHour.value = time.fajr.split(":")[0].toInt()
                                binding.pickerFajrMinute.value = time.fajr.split(":")[1].toInt()

                                binding.pickerSunriseHour.value = time.sunrise.split(":")[0].toInt()
                                binding.pickerSunriseMinute.value =
                                    time.sunrise.split(":")[1].toInt()

                                binding.pickerDhuhrHour.value = time.dhuhr.split(":")[0].toInt()
                                binding.pickerDhuhrMinute.value = time.dhuhr.split(":")[1].toInt()

                                binding.pickerAsrHour.value = time.asr.split(":")[0].toInt()
                                binding.pickerAsrMinute.value = time.asr.split(":")[1].toInt()

                                binding.pickerMaghribHour.value = time.maghrib.split(":")[0].toInt()
                                binding.pickerMaghribMinute.value =
                                    time.maghrib.split(":")[1].toInt()

                                binding.pickerIshaHour.value = time.isha.split(":")[0].toInt()
                                binding.pickerIshaMinute.value = time.isha.split(":")[1].toInt()
                            }
                            if (originalTime != null && time != null) {
                                if (originalTime.fajr != time.fajr)
                                    changeCardBackgroundColor(binding.cardFajr, R.attr.colorWarning)
                                else
                                    changeCardBackgroundColor(binding.cardFajr, R.attr.colorCard)

                                if (originalTime.sunrise != time.sunrise)
                                    changeCardBackgroundColor(
                                        binding.cardSunrise,
                                        R.attr.colorWarning
                                    )
                                else
                                    changeCardBackgroundColor(binding.cardSunrise, R.attr.colorCard)

                                if (originalTime.dhuhr != time.dhuhr)
                                    changeCardBackgroundColor(
                                        binding.cardDhuhr,
                                        R.attr.colorWarning
                                    )
                                else
                                    changeCardBackgroundColor(binding.cardDhuhr, R.attr.colorCard)

                                if (originalTime.asr != time.asr)
                                    changeCardBackgroundColor(binding.cardAsr, R.attr.colorWarning)
                                else
                                    changeCardBackgroundColor(binding.cardAsr, R.attr.colorCard)

                                if (originalTime.maghrib != time.maghrib)
                                    changeCardBackgroundColor(
                                        binding.cardMaghrib,
                                        R.attr.colorWarning
                                    )
                                else
                                    changeCardBackgroundColor(binding.cardMaghrib, R.attr.colorCard)

                                if (originalTime.isha != time.isha)
                                    changeCardBackgroundColor(binding.cardIsha, R.attr.colorWarning)
                                else
                                    changeCardBackgroundColor(binding.cardIsha, R.attr.colorCard)
                            }
                        }
                    }
                }
                animateVisibility(binding.progressEdit, false)
                binding.switchEnableEdit.isEnabled = true
            }

        }
    }

    private fun calAll(): List<PrayTimes?> {
        val res = arrayListOf<PrayTimes?>()
        val civilDate = Jdn.today().toGregorianCalendar()
        for (i in 1..366) {
            val str = getDayMonthForDayOfYear(i)
            val month = str.split("/")[0].toInt()
            val day = str.split("/")[1].toInt()
            val persianDate = PersianDate(PersianDate(civilDate.toJdn()).year, month, day)
            val date = CivilDate(persianDate.toJdn())
            var time = coordinates?.calculatePrayTimes(Jdn(date).toJavaCalendar())
            if (!requireContext().appPrefs.getBoolean(PREF_SUMMER_TIME, true) && i in 2..185)
                time = fixSummerTimes(time, true)
            res.add(time)
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    binding.progressEdit.isIndeterminate = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        binding.progressEdit.setProgress(i, true)
                    else binding.progressEdit.progress = i
                }
            }
        }
        return res
    }

    private fun initPickers() {
        binding.pickerDay.minValue = 1
        binding.pickerDay.maxValue = 31

        binding.pickerMonth.minValue = 1
        binding.pickerMonth.maxValue = 12

        binding.pickerMonth.value = 1
        binding.pickerDay.value = 1

        binding.pickerFajrHour.minValue = 0
        binding.pickerFajrHour.maxValue = 23

        binding.pickerFajrMinute.minValue = 0
        binding.pickerFajrMinute.maxValue = 59

        binding.pickerSunriseHour.minValue = 0
        binding.pickerSunriseHour.maxValue = 23

        binding.pickerSunriseMinute.minValue = 0
        binding.pickerSunriseMinute.maxValue = 59

        binding.pickerDhuhrHour.minValue = 0
        binding.pickerDhuhrHour.maxValue = 23

        binding.pickerDhuhrMinute.minValue = 0
        binding.pickerDhuhrMinute.maxValue = 59

        binding.pickerAsrHour.minValue = 0
        binding.pickerAsrHour.maxValue = 23

        binding.pickerAsrMinute.minValue = 0
        binding.pickerAsrMinute.maxValue = 59

        binding.pickerMaghribHour.minValue = 0
        binding.pickerMaghribHour.maxValue = 23

        binding.pickerMaghribMinute.minValue = 0
        binding.pickerMaghribMinute.maxValue = 59

        binding.pickerIshaHour.minValue = 0
        binding.pickerIshaHour.maxValue = 23

        binding.pickerIshaMinute.minValue = 0
        binding.pickerIshaMinute.maxValue = 59

        binding.pickerMonth.setOnValueChangedListener { _, _, newVal ->
            run {
                when (newVal) {
                    in 1..6 -> binding.pickerDay.maxValue = 31
                    else -> binding.pickerDay.maxValue = 30
                }
                binding.pickerDay.value = 1
                updateDay()
            }
        }
        binding.pickerDay.setOnValueChangedListener { _, _, _ -> updateDay() }

        binding.pickerFajrMinute.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.fajr = "${binding.pickerFajrHour.value}:$newVal"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.fajr,
                    originalTimes.find { it.dayNumber == day }?.fajr
                )
            )
                changeCardBackgroundColor(binding.cardFajr, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardFajr, R.attr.colorCard)

        }
        binding.pickerFajrHour.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.fajr = "$newVal:${binding.pickerFajrMinute.value}"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.fajr,
                    originalTimes.find { it.dayNumber == day }?.fajr
                )
            )
                changeCardBackgroundColor(binding.cardFajr, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardFajr, R.attr.colorCard)
        }

        binding.pickerSunriseMinute.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.sunrise =
                "${binding.pickerSunriseHour.value}:$newVal"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.sunrise,
                    originalTimes.find { it.dayNumber == day }?.sunrise
                )
            )
                changeCardBackgroundColor(binding.cardSunrise, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardSunrise, R.attr.colorCard)
        }
        binding.pickerSunriseHour.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.sunrise =
                "$newVal:${binding.pickerSunriseMinute.value}"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.sunrise,
                    originalTimes.find { it.dayNumber == day }?.sunrise
                )
            )
                changeCardBackgroundColor(binding.cardSunrise, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardSunrise, R.attr.colorCard)
        }

        binding.pickerDhuhrMinute.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.dhuhr = "${binding.pickerDhuhrHour.value}:$newVal"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.dhuhr,
                    originalTimes.find { it.dayNumber == day }?.dhuhr
                )
            )
                changeCardBackgroundColor(binding.cardDhuhr, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardDhuhr, R.attr.colorCard)
        }
        binding.pickerDhuhrHour.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.dhuhr = "$newVal:${binding.pickerDhuhrMinute.value}"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.dhuhr,
                    originalTimes.find { it.dayNumber == day }?.dhuhr
                )
            )
                changeCardBackgroundColor(binding.cardDhuhr, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardDhuhr, R.attr.colorCard)
        }

        binding.pickerAsrMinute.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.asr = "${binding.pickerAsrHour.value}:$newVal"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.asr,
                    originalTimes.find { it.dayNumber == day }?.asr
                )
            )
                changeCardBackgroundColor(binding.cardAsr, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardAsr, R.attr.colorCard)
        }
        binding.pickerAsrHour.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.asr = "$newVal:${binding.pickerAsrMinute.value}"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.asr,
                    originalTimes.find { it.dayNumber == day }?.asr
                )
            )
                changeCardBackgroundColor(binding.cardAsr, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardAsr, R.attr.colorCard)

        }

        binding.pickerMaghribMinute.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.maghrib =
                "${binding.pickerMaghribHour.value}:$newVal"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.maghrib,
                    originalTimes.find { it.dayNumber == day }?.maghrib
                )
            )
                changeCardBackgroundColor(binding.cardMaghrib, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardMaghrib, R.attr.colorCard)
        }
        binding.pickerMaghribHour.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.maghrib =
                "$newVal:${binding.pickerMaghribMinute.value}"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.maghrib,
                    originalTimes.find { it.dayNumber == day }?.maghrib
                )
            )
                changeCardBackgroundColor(binding.cardMaghrib, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardMaghrib, R.attr.colorCard)
        }

        binding.pickerIshaMinute.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.isha = "${binding.pickerIshaHour.value}:$newVal"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.isha,
                    originalTimes.find { it.dayNumber == day }?.isha
                )
            )
                changeCardBackgroundColor(binding.cardIsha, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardIsha, R.attr.colorCard)
        }
        binding.pickerIshaHour.setOnValueChangedListener { _, _, newVal ->
            val day = viewModel.dayNum.value
            times.find { it.dayNumber == day }?.isha = "$newVal:${binding.pickerIshaMinute.value}"
            if (!isEquals(
                    times.find { it.dayNumber == day }?.isha,
                    originalTimes.find { it.dayNumber == day }?.isha
                )
            )
                changeCardBackgroundColor(binding.cardIsha, R.attr.colorWarning)
            else
                changeCardBackgroundColor(binding.cardIsha, R.attr.colorCard)
        }

    }//end of initPickers

    private fun updateDay() {
        viewModel.dayNum.value = getDayNum(binding.pickerMonth.value, binding.pickerDay.value)
    }

    fun groupChange(athan: Int, fromDay: Int, toDay: Int, m: Int, forward: Boolean) {
        var min = m
        if (!forward) min *= -1
        for (i in fromDay..toDay) {
            val t = times.find { it.dayNumber == i }
            when (athan) {
                0 -> {
                    times[times.indexOf(t)].fajr = fixTime(t!!.fajr, min)
                }
                1 -> {
                    times[times.indexOf(t)].sunrise = fixTime(t!!.sunrise, min)
                }
                2 -> {
                    times[times.indexOf(t)].dhuhr = fixTime(t!!.dhuhr, min)
                }
                3 -> {
                    times[times.indexOf(t)].asr = fixTime(t!!.asr, min)
                }
                4 -> {
                    times[times.indexOf(t)].maghrib = fixTime(t!!.maghrib, min)
                }
                5 -> {
                    times[times.indexOf(t)].isha = fixTime(t!!.isha, min)
                }
            }
        }
        viewModel.dayNum.value = getDayNum(binding.pickerMonth.value, binding.pickerDay.value)
    }//end of groupChange

    private fun changeCardBackgroundColor(card: MaterialCardView, @AttrRes color: Int) {
        ObjectAnimator.ofObject(
            card, "cardBackgroundColor", ArgbEvaluator(),
            card.cardBackgroundColor.defaultColor, requireContext().resolveColor(color)
        ).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun isEquals(c1: String?, c2: String?): Boolean {
        c1 ?: return false
        c2 ?: return false
        val a = c1.split(":")
        val b = c2.split(":")
        return a[0].toInt() == b[0].toInt() && a[1].toInt() == b[1].toInt()
    }

    private fun updateDB() {
        viewModel.timeList.value?.let { times ->
            lifecycleScope.launch {
                prayTimesDB.prayTimes().updateEdited(times)
                updateView()
            }
        }
    }


}//end of EditFragment

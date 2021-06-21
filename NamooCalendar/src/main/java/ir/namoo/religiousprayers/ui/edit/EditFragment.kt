package ir.namoo.religiousprayers.ui.edit

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.github.persiancalendar.calendar.PersianDate
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.PREF_ENABLE_EDIT
import ir.namoo.religiousprayers.PREF_SUMMER_TIME
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentEditBinding
import ir.namoo.religiousprayers.praytimes.EditedPrayTimesEntity
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import ir.namoo.religiousprayers.praytimes.PrayTimesDB
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.addSummerTimes
import ir.namoo.religiousprayers.utils.animateVisibility
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.calculationMethod
import ir.namoo.religiousprayers.utils.fixTime
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.getCoordinate
import ir.namoo.religiousprayers.utils.getDayMonthForDayOfYear
import ir.namoo.religiousprayers.utils.getDayNum
import ir.namoo.religiousprayers.utils.logException
import ir.namoo.religiousprayers.utils.resolveColor
import ir.namoo.religiousprayers.utils.setupUpNavigation

class EditFragment : Fragment() {

    private lateinit var binding: FragmentEditBinding
    private val model: EditViewModel by viewModels()
    private lateinit var times: MutableList<EditedPrayTimesEntity>
    private lateinit var originalTimes: MutableList<EditedPrayTimesEntity>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false).apply {
            appBar.toolbar.let {
                it.setTitle(R.string.edit_times)
                it.setupUpNavigation()
            }
        }
        binding.progressEdit.max = 366
        binding.progressEdit.progress = 0
        val enabled = requireActivity().appPrefs.getBoolean(PREF_ENABLE_EDIT, false)
        binding.switchEnableEdit.isChecked = enabled
        binding.switchEnableEdit.setOnCheckedChangeListener { _, isChecked ->
            run {
                requireActivity().appPrefs.edit {
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
        binding.appBar.let {
            it.toolbar.inflateMenu(R.menu.edit_menu)
            it.toolbar.setOnMenuItemClickListener { clickedMenuItem ->
                when (clickedMenuItem?.itemId) {
                    R.id.mnu_edit_apply -> {
                        if (!binding.switchEnableEdit.isChecked) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.edit_not_enabled),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
                            AlertDialog.Builder(requireContext()).apply {
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
                    R.id.mnu_edit_clear -> {
                        if (!binding.switchEnableEdit.isChecked) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.edit_not_enabled),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle(getString(R.string.str_dialog_clear_edited_title))
                                setMessage(getString(R.string.str_dialog_clear_edited_message))
                                setPositiveButton(R.string.yes) { _, _ ->
                                    PrayTimesDB.getInstance(requireContext().applicationContext)
                                        .prayTimes()
                                        .cleanEditedPrayTimes()
                                    binding.switchEnableEdit.isChecked = false
                                    requireActivity().onBackPressed()
                                }
                                setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                                show()
                            }
                        }
                    }
                    R.id.mnu_edit_group_change -> {
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
                true
            }
        }
        return binding.root
    }// end of onCreateView


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
        DBCheckTask().execute()
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
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.fajr = "${binding.pickerFajrHour.value}:$newVal"
            if (times.find { it.dayNumber == day }?.fajr != originalTimes.find { it.dayNumber == day }?.fajr)
                ObjectAnimator.ofObject(
                    binding.cardFajr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardFajr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardFajr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardFajr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }
        binding.pickerFajrHour.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.fajr = "$newVal:${binding.pickerFajrMinute.value}"
            if (times.find { it.dayNumber == day }?.fajr != originalTimes.find { it.dayNumber == day }?.fajr)
                ObjectAnimator.ofObject(
                    binding.cardFajr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardFajr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardFajr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardFajr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }

        binding.pickerSunriseMinute.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.sunrise =
                "${binding.pickerSunriseHour.value}:$newVal"
            if (times.find { it.dayNumber == day }?.sunrise != originalTimes.find { it.dayNumber == day }?.sunrise)
                ObjectAnimator.ofObject(
                    binding.cardSunrise,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardSunrise.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardSunrise,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardSunrise.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }
        binding.pickerSunriseHour.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.sunrise =
                "$newVal:${binding.pickerSunriseMinute.value}"
            if (times.find { it.dayNumber == day }?.sunrise != originalTimes.find { it.dayNumber == day }?.sunrise)
                ObjectAnimator.ofObject(
                    binding.cardSunrise,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardSunrise.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardSunrise,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardSunrise.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }

        binding.pickerDhuhrMinute.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.dhuhr = "${binding.pickerDhuhrHour.value}:$newVal"
            if (times.find { it.dayNumber == day }?.dhuhr != originalTimes.find { it.dayNumber == day }?.dhuhr)
                ObjectAnimator.ofObject(
                    binding.cardDhuhr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardDhuhr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardDhuhr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardDhuhr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }
        binding.pickerDhuhrHour.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.dhuhr = "$newVal:${binding.pickerDhuhrMinute.value}"
            if (times.find { it.dayNumber == day }?.dhuhr != originalTimes.find { it.dayNumber == day }?.dhuhr)
                ObjectAnimator.ofObject(
                    binding.cardDhuhr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardDhuhr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardDhuhr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardDhuhr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }

        binding.pickerAsrMinute.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.asr = "${binding.pickerAsrHour.value}:$newVal"
            if (times.find { it.dayNumber == day }?.asr != originalTimes.find { it.dayNumber == day }?.asr)
                ObjectAnimator.ofObject(
                    binding.cardAsr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardAsr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardAsr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardAsr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }
        binding.pickerAsrHour.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.asr = "$newVal:${binding.pickerAsrMinute.value}"
            if (times.find { it.dayNumber == day }?.asr != originalTimes.find { it.dayNumber == day }?.asr)
                ObjectAnimator.ofObject(
                    binding.cardAsr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardAsr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardAsr,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardAsr.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }

        binding.pickerMaghribMinute.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.maghrib =
                "${binding.pickerMaghribHour.value}:$newVal"
            if (times.find { it.dayNumber == day }?.maghrib != originalTimes.find { it.dayNumber == day }?.maghrib)
                ObjectAnimator.ofObject(
                    binding.cardMaghrib,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardMaghrib.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardMaghrib,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardMaghrib.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }
        binding.pickerMaghribHour.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.maghrib =
                "$newVal:${binding.pickerMaghribMinute.value}"
            if (times.find { it.dayNumber == day }?.maghrib != originalTimes.find { it.dayNumber == day }?.maghrib)
                ObjectAnimator.ofObject(
                    binding.cardMaghrib,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardMaghrib.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardMaghrib,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardMaghrib.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }

        binding.pickerIshaMinute.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.isha = "${binding.pickerIshaHour.value}:$newVal"
            if (times.find { it.dayNumber == day }?.isha != originalTimes.find { it.dayNumber == day }?.isha)
                ObjectAnimator.ofObject(
                    binding.cardIsha,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardIsha.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardIsha,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardIsha.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }
        binding.pickerIshaHour.setOnValueChangedListener { _, _, newVal ->
            val day = model.getDay().value
            times.find { it.dayNumber == day }?.isha = "$newVal:${binding.pickerIshaMinute.value}"
            if (times.find { it.dayNumber == day }?.isha != originalTimes.find { it.dayNumber == day }?.isha)
                ObjectAnimator.ofObject(
                    binding.cardIsha,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardIsha.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorWarning)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            else
                ObjectAnimator.ofObject(
                    binding.cardIsha,
                    "cardBackgroundColor",
                    ArgbEvaluator(),
                    binding.cardIsha.cardBackgroundColor.defaultColor,
                    requireContext().resolveColor(R.attr.colorCard)
                ).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
        }

    }//end of initPickers

    private fun updateDay() {
        model.getDay().value = getDayNum(binding.pickerMonth.value, binding.pickerDay.value)
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
        model.getDay().value = getDayNum(binding.pickerMonth.value, binding.pickerDay.value)
    }//end of groupChange


    private fun updateDB() {
        val db = PrayTimesDB.getInstance(requireContext().applicationContext)
        if (model.getTimes().value != null)
            db.prayTimes().updateEdited(model.getTimes().value!!)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class DBCheckTask : AsyncTask<Unit, Int, Unit>() {

        override fun doInBackground(vararg params: Unit?) {
            firstDBCheck()
        }

        override fun onPreExecute() {
            super.onPreExecute()
            binding.txtEditInfo.text = resources.getString(R.string.please_wait)
            animateVisibility(binding.progressEdit, true)
            binding.switchEnableEdit.isEnabled = false
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            binding.txtEditInfo.text = formatNumber(resources.getString(R.string.use_summer_time))
            initPickers()
            model.getTimes().observe(requireActivity(), Observer {
                times = arrayListOf()
                times.addAll(it)
            })
            model.getDay().observe(requireActivity(), Observer { dayNum ->
                run {
                    val time = times.find { it.dayNumber == dayNum }
                    val originalTime = originalTimes.find { it.dayNumber == dayNum }
                    if (time != null) {
                        binding.pickerMonth.value =
                            getDayMonthForDayOfYear(time.dayNumber).split("/")[0].toInt()
                        binding.pickerDay.value =
                            getDayMonthForDayOfYear(time.dayNumber).split("/")[1].toInt()

                        binding.pickerFajrHour.value = time.fajr.split(":")[0].toInt()
                        binding.pickerFajrMinute.value = time.fajr.split(":")[1].toInt()

                        binding.pickerSunriseHour.value = time.sunrise.split(":")[0].toInt()
                        binding.pickerSunriseMinute.value = time.sunrise.split(":")[1].toInt()

                        binding.pickerDhuhrHour.value = time.dhuhr.split(":")[0].toInt()
                        binding.pickerDhuhrMinute.value = time.dhuhr.split(":")[1].toInt()

                        binding.pickerAsrHour.value = time.asr.split(":")[0].toInt()
                        binding.pickerAsrMinute.value = time.asr.split(":")[1].toInt()

                        binding.pickerMaghribHour.value = time.maghrib.split(":")[0].toInt()
                        binding.pickerMaghribMinute.value = time.maghrib.split(":")[1].toInt()

                        binding.pickerIshaHour.value = time.isha.split(":")[0].toInt()
                        binding.pickerIshaMinute.value = time.isha.split(":")[1].toInt()
                    }
                    if (originalTime != null && time != null) {
                        if (originalTime.fajr != time.fajr)
                            ObjectAnimator.ofObject(
                                binding.cardFajr,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardFajr.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorWarning)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        else
                            ObjectAnimator.ofObject(
                                binding.cardFajr,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardFajr.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorCard)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        if (originalTime.sunrise != time.sunrise)
                            ObjectAnimator.ofObject(
                                binding.cardSunrise,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardSunrise.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorWarning)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        else
                            ObjectAnimator.ofObject(
                                binding.cardSunrise,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardSunrise.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorCard)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        if (originalTime.dhuhr != time.dhuhr)
                            ObjectAnimator.ofObject(
                                binding.cardDhuhr,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardDhuhr.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorWarning)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        else
                            ObjectAnimator.ofObject(
                                binding.cardDhuhr,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardDhuhr.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorCard)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        if (originalTime.asr != time.asr)
                            ObjectAnimator.ofObject(
                                binding.cardAsr,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardAsr.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorWarning)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        else
                            ObjectAnimator.ofObject(
                                binding.cardAsr,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardAsr.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorCard)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        if (originalTime.maghrib != time.maghrib)
                            ObjectAnimator.ofObject(
                                binding.cardMaghrib,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardMaghrib.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorWarning)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        else
                            ObjectAnimator.ofObject(
                                binding.cardMaghrib,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardMaghrib.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorCard)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        if (originalTime.isha != time.isha)
                            ObjectAnimator.ofObject(
                                binding.cardIsha,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardIsha.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorWarning)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }
                        else
                            ObjectAnimator.ofObject(
                                binding.cardIsha,
                                "cardBackgroundColor",
                                ArgbEvaluator(),
                                binding.cardIsha.cardBackgroundColor.defaultColor,
                                requireContext().resolveColor(R.attr.colorCard)
                            ).apply {
                                duration = 300
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }

                    }
                }
            })

            animateVisibility(binding.progressEdit, false)
            binding.switchEnableEdit.isEnabled = true
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            binding.progressEdit.isIndeterminate = false
            for (i in values)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    binding.progressEdit.setProgress(i!!, true)
                else binding.progressEdit.progress = i!!
        }

        private fun firstDBCheck() {
            val db = PrayTimesDB.getInstance(requireContext().applicationContext)
            runCatching {
                if (db.prayTimes().getAllEdited() == null || db.prayTimes().getAllEdited()!!
                        .isEmpty()
                ) {
                    val times = calAll()
                    val forEdits = arrayListOf<EditedPrayTimesEntity>()
                    var id = 1
                    for (t in times) {
                        val temp =
                            EditedPrayTimesEntity(
                                id, id,
                                "${t.fajrClock.hour}:${t.fajrClock.minute}",
                                "${t.sunriseClock.hour}:${t.sunriseClock.minute}",
                                "${t.dhuhrClock.hour}:${t.dhuhrClock.minute}",
                                "${t.asrClock.hour}:${t.asrClock.minute}",
                                "${t.maghribClock.hour}:${t.maghribClock.minute}",
                                "${t.ishaClock.hour}:${t.ishaClock.minute}"
                            )
                        id++
                        forEdits.add(temp)
                    }

                    db.prayTimes().insertEdited(forEdits)
                }
            }.onFailure(logException)
            originalTimes = mutableListOf()
            db.prayTimes().getAllEdited()?.let {
                originalTimes.addAll(it)
            }
        }

        private fun calAll(): List<PrayTimes> {
            val res = arrayListOf<PrayTimes>()
            val temp = PersianDate(Jdn.today.value)
            for (i in 1..366) {
                val str = getDayMonthForDayOfYear(i)
                val date =
                    PersianDate(temp.year, str.split("/")[0].toInt(), str.split("/")[1].toInt())
                var time = PrayTimeProvider.calculate(
                    calculationMethod,
                    Jdn(date.toJdn()),
                    getCoordinate(requireContext())!!,
                    requireContext()
                )
                if (!requireContext().appPrefs.getBoolean(PREF_SUMMER_TIME, true) && i in 2..185)
                    time = addSummerTimes(time)
                res.add(time)

                publishProgress(i)
            }
            return res
        }
    }

}//end of EditFragment

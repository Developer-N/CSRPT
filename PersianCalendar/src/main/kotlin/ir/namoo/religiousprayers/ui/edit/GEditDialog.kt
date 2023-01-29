package ir.namoo.religiousprayers.ui.edit

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.GeditDialogBinding
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.getDayNum
import ir.namoo.religiousprayers.ui.shared.ShapedAdapter

class GEditDialog : AppCompatDialogFragment() {
    private lateinit var binding: GeditDialogBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = GeditDialogBinding.inflate(requireActivity().layoutInflater)
        binding.pickerFromDay.minValue = 1
        binding.pickerFromDay.maxValue = 31

        binding.pickerToDay.minValue = 1
        binding.pickerToDay.maxValue = 31

        binding.pickerMinuteChange.minValue = 1
        binding.pickerMinuteChange.maxValue = 30

        binding.pickerFromMonth.minValue = 1
        binding.pickerFromMonth.maxValue = 12

        binding.pickerToMonth.minValue = 1
        binding.pickerToMonth.maxValue = 12

        binding.pickerFromMonth.setOnValueChangedListener { _, _, newVal ->
            if (newVal in 1..6)
                binding.pickerFromDay.maxValue = 31
            else
                binding.pickerFromDay.maxValue = 30
        }
        binding.pickerToMonth.setOnValueChangedListener { _, _, newVal ->
            if (newVal in 1..6)
                binding.pickerToDay.maxValue = 31
            else
                binding.pickerToDay.maxValue = 30
        }
        val athans = listOf(
            getString(R.string.fajr),
            getString(R.string.sunrise),
            getString(R.string.dhuhr),
            getString(R.string.asr),
            getString(R.string.maghrib),
            getString(R.string.isha)
        ).toTypedArray()

        binding.spinnerAthanSE.adapter =
            ShapedAdapter<String>(requireContext(), R.layout.select_dialog_item, R.id.text1, athans)
        binding.spinnerAthanSE.setSelection(0)
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setView(binding.root)
            setCustomTitle(null)
            setPositiveButton(R.string.apply) { _, _ ->
                if (allThingIsOk()) {
                    val fromDay =
                        getDayNum(binding.pickerFromMonth.value, binding.pickerFromDay.value)
                    val toDay = getDayNum(binding.pickerToMonth.value, binding.pickerToDay.value)
                    val editPrayTimesFragment = parentFragment as EditFragment?
                    runCatching {
                        editPrayTimesFragment!!.groupChange(
                            binding.spinnerAthanSE.selectedItemPosition,
                            fromDay,
                            toDay,
                            binding.pickerMinuteChange.value,
                            binding.radioBtnForward.isChecked
                        )
                    }.onFailure(logException)
                } else {
                    Toast.makeText(context, getString(R.string.error_gedit_data), Toast.LENGTH_LONG)
                        .show()
                }
            }
            setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
        }.create()
    }

    private fun allThingIsOk(): Boolean {
        return if (binding.pickerFromMonth.value > binding.pickerToMonth.value) false
        else if (binding.pickerFromMonth.value == binding.pickerToMonth.value &&
            binding.pickerToDay.value < binding.pickerFromDay.value
        ) false
        else binding.pickerMinuteChange.value != 0
    }

}//end of class GEditDialog

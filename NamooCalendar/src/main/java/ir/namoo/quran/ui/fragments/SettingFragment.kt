package ir.namoo.quran.ui.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import ir.namoo.quran.ui.QuranActivity
import ir.namoo.quran.utils.*
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentQuranSettingBinding
import ir.namoo.religiousprayers.ui.edit.ShapedAdapter
import ir.namoo.religiousprayers.utils.animateVisibility
import ir.namoo.religiousprayers.utils.appPrefsLite


class SettingFragment : Fragment() {

    private lateinit var binding: FragmentQuranSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)
        (requireActivity() as QuranActivity).setTitleAndSubtitle(
            resources.getString(R.string.settings),
            ""
        )
        binding = FragmentQuranSettingBinding.inflate(inflater)
        setHasOptionsMenu(true)

        initCheckBoxes()
        initQari()
        initArabic()
        initEnglish()
        initKurdish()
        initFarsi()

        return binding.root
    }//end of onCreateView

    private fun initCheckBoxes() {
        listOf(
            binding.checkboxEnglish,
            binding.checkboxEnglishTransliteration,
            binding.checkboxKurdish,
            binding.checkboxFarsi,
            binding.checkboxFarsiFull
        ).zip(
            listOf(
                PREF_ENGLISH_TRANSLATE,
                PREF_ENGLISH_TRANSLITERATION,
                PREF_KURDISH_TRANSLATE,
                PREF_FARSI_TRANSLATE,
                PREF_FARSI_FULL_TRANSLATE
            )
        ) { checkBox, pref ->
            checkBox.isChecked = requireContext().appPrefsLite.getBoolean(pref, false)
            checkBox.setOnClickListener {
                requireContext().appPrefsLite.edit {
                    putBoolean(pref, checkBox.isChecked)
                }
                if (checkBox.id == R.id.checkbox_farsi) {
                    binding.checkboxFarsiFull.isEnabled = requireContext().appPrefsLite.getBoolean(
                        PREF_FARSI_TRANSLATE, false
                    )
                }
            }
        }
        binding.checkboxFarsiFull.isEnabled = requireContext().appPrefsLite.getBoolean(
            PREF_FARSI_TRANSLATE, false
        )

    }//end of initCheckBoxes

    private fun initQari() {
        val names = resources.getStringArray(R.array.quran_names).filter {
            it != resources.getStringArray(R.array.quran_names)[0] && it != resources.getStringArray(
                R.array.quran_names
            )[1]
        }.toTypedArray()
        val values = resources.getStringArray(R.array.quran_folders).filter {
            it != resources.getStringArray(R.array.quran_folders)[0] && it != resources.getStringArray(
                R.array.quran_folders
            )[1]
        }
        binding.spinnerSelectQari.apply {
            adapter = ShapedAdapter<String>(requireContext(), R.layout.select_dialog_item, names)
            setSelection(
                values.indexOf(
                    requireContext().appPrefsLite.getString(
                        PREF_SELECTED_QARI,
                        DEFAULT_SELECTED_QARI
                    )
                )
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    requireContext().appPrefsLite.edit {
                        putString(
                            PREF_SELECTED_QARI,
                            values[names.indexOf(binding.spinnerSelectQari.selectedItem.toString())]
                        )
                    }
                }
            }
        }

        binding.checkboxPlayTranslate.apply {
            isChecked = requireContext().appPrefsLite.getBoolean(PREF_PLAY_TRANSLATION, false)
            setOnClickListener {
                requireContext().appPrefsLite.edit {
                    putBoolean(PREF_PLAY_TRANSLATION, isChecked)
                }
                animateVisibility(
                    binding.spinnerSelectTranslateToPlay,
                    binding.checkboxPlayTranslate.isChecked
                )
                val transition = ChangeBounds().apply {
                    interpolator = LinearOutSlowInInterpolator()
                }
                TransitionManager.beginDelayedTransition(
                    binding.cardTranslates as ViewGroup,
                    transition
                )
            }
        }

        binding.spinnerSelectTranslateToPlay.visibility =
            if (binding.checkboxPlayTranslate.isChecked) View.VISIBLE else View.GONE

        val tNames = resources.getStringArray(R.array.quran_names).filter {
            it == resources.getStringArray(R.array.quran_names)[0] || it == resources.getStringArray(
                R.array.quran_names
            )[1]
        }.toTypedArray()
        val tValues = resources.getStringArray(R.array.quran_folders).filter {
            it == resources.getStringArray(R.array.quran_folders)[0] || it == resources.getStringArray(
                R.array.quran_folders
            )[1]
        }
        binding.spinnerSelectTranslateToPlay.apply {
            adapter = ShapedAdapter<String>(requireContext(), R.layout.select_dialog_item, tNames)
            setSelection(
                tValues.indexOf(
                    requireContext().appPrefsLite.getString(
                        PREF_TRANSLATE_TO_PLAY,
                        DEFAULT_TRANSLATE_TO_PLAY
                    )
                )
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    requireContext().appPrefsLite.edit {
                        putString(
                            PREF_TRANSLATE_TO_PLAY,
                            tValues[tNames.indexOf(binding.spinnerSelectTranslateToPlay.selectedItem.toString())]
                        )
                    }
                }
            }
        }
    }//end of initQari

    private fun initArabic() {
        val fontNames = resources.getStringArray(R.array.quran_fonts)
        val fontValues = resources.getStringArray(R.array.quran_fonts_values)
        updateArabicPrev()
        binding.spinnerArabicFont.apply {
            adapter =
                ShapedAdapter<String>(requireContext(), R.layout.select_dialog_item, fontNames)
            setSelection(
                fontValues.indexOf(
                    requireContext().appPrefsLite.getString(
                        PREF_QURAN_FONT, DEFAULT_QURAN_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    requireContext().appPrefsLite.edit {
                        putString(
                            PREF_QURAN_FONT,
                            fontValues[fontNames.indexOf(binding.spinnerArabicFont.selectedItem.toString())]
                        )
                    }
                    updateArabicPrev()
                }
            }
        }


        binding.seekbarArabicFontSize.apply {
            progress = requireContext().appPrefsLite.getFloat(
                PREF_QURAN_FONT_SIZE,
                DEFAULT_QURAN_FONT_SIZE
            ).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    binding.txtArabicPreview.textSize = progress.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    requireContext().appPrefsLite.edit {
                        putFloat(PREF_QURAN_FONT_SIZE, progress.toFloat())
                    }
                    updateArabicPrev()
                }
            })
        }


    }//end of initArabic

    private fun initEnglish() {
        val fontNames = resources.getStringArray(R.array.english_fonts)
        val fontValues = resources.getStringArray(R.array.english_fonts_values)
        binding.spinnerEnglishFont.apply {
            adapter =
                ShapedAdapter<String>(requireContext(), R.layout.select_dialog_item, fontNames)
            setSelection(
                fontValues.indexOf(
                    requireContext().appPrefsLite.getString(
                        PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    requireContext().appPrefsLite.edit {
                        putString(
                            PREF_ENGLISH_FONT,
                            fontValues[fontNames.indexOf(binding.spinnerEnglishFont.selectedItem.toString())]
                        )
                    }
                    updateEnglishPrev()
                }
            }
        }


        binding.seekbarEnglishFontSize.apply {
            progress = requireContext().appPrefsLite.getFloat(
                PREF_ENGLISH_FONT_SIZE,
                DEFAULT_ENGLISH_FONT_SIZE
            ).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    binding.txtEnglishPreview.textSize = progress.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    requireContext().appPrefsLite.edit {
                        putFloat(PREF_ENGLISH_FONT_SIZE, progress.toFloat())
                    }
                    updateEnglishPrev()
                }
            })
        }


    }//end of initEnglish

    private fun initKurdish() {
        val fontNames = resources.getStringArray(R.array.kurdish_fonts)
        val fontValues = resources.getStringArray(R.array.kurdish_fonts_values)
        binding.spinnerKurdishFont.apply {
            adapter =
                ShapedAdapter<String>(requireContext(), R.layout.select_dialog_item, fontNames)
            setSelection(
                fontValues.indexOf(
                    requireContext().appPrefsLite.getString(
                        PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    requireContext().appPrefsLite.edit {
                        putString(
                            PREF_KURDISH_FONT,
                            fontValues[fontNames.indexOf(binding.spinnerKurdishFont.selectedItem.toString())]
                        )
                    }
                    updateKurdishPrev()
                }
            }
        }


        binding.seekbarKurdishFontSize.apply {
            progress = requireContext().appPrefsLite.getFloat(
                PREF_KURDISH_FONT_SIZE,
                DEFAULT_KURDISH_FONT_SIZE
            ).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    binding.txtKurdishPreview.textSize = progress.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    requireContext().appPrefsLite.edit {
                        putFloat(PREF_KURDISH_FONT_SIZE, progress.toFloat())
                    }
                    updateKurdishPrev()
                }
            })
        }


    }//end of initKurdish

    private fun initFarsi() {
        val fontNames = resources.getStringArray(R.array.farsi_fonts)
        val fontValues = resources.getStringArray(R.array.farsi_fonts_values)
        binding.spinnerFarsiFont.apply {
            adapter =
                ShapedAdapter<String>(requireContext(), R.layout.select_dialog_item, fontNames)
            setSelection(
                fontValues.indexOf(
                    requireContext().appPrefsLite.getString(
                        PREF_FARSI_FONT, DEFAULT_FARSI_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    requireContext().appPrefsLite.edit {
                        putString(
                            PREF_FARSI_FONT,
                            fontValues[fontNames.indexOf(binding.spinnerFarsiFont.selectedItem.toString())]
                        )
                    }
                    updateFarsiPrev()
                }
            }
        }


        binding.seekbarFarsiFontSize.apply {
            progress = requireContext().appPrefsLite.getFloat(
                PREF_FARSI_FONT_SIZE,
                DEFAULT_FARSI_FONT_SIZE
            ).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    binding.txtFarsiPreview.textSize = progress.toFloat()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    requireContext().appPrefsLite.edit {
                        putFloat(PREF_FARSI_FONT_SIZE, progress.toFloat())
                    }
                    updateFarsiPrev()
                }
            })
        }


    }//end of initFarsi

    private fun updateArabicPrev() {
        binding.txtArabicPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, requireContext().appPrefsLite.getString(
                PREF_QURAN_FONT, DEFAULT_QURAN_FONT
            )
        )
        binding.txtArabicPreview.textSize = requireContext().appPrefsLite.getFloat(
            PREF_QURAN_FONT_SIZE,
            DEFAULT_QURAN_FONT_SIZE
        )
    }

    private fun updateEnglishPrev() {
        binding.txtEnglishPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, requireContext().appPrefsLite.getString(
                PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT
            )
        )
        binding.txtEnglishPreview.textSize = requireContext().appPrefsLite.getFloat(
            PREF_ENGLISH_FONT_SIZE,
            DEFAULT_ENGLISH_FONT_SIZE
        )
    }

    private fun updateKurdishPrev() {
        binding.txtKurdishPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, requireContext().appPrefsLite.getString(
                PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT
            )
        )
        binding.txtKurdishPreview.textSize = requireContext().appPrefsLite.getFloat(
            PREF_KURDISH_FONT_SIZE,
            DEFAULT_KURDISH_FONT_SIZE
        )
    }

    private fun updateFarsiPrev() {
        binding.txtFarsiPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, requireContext().appPrefsLite.getString(
                PREF_FARSI_FONT, DEFAULT_FARSI_FONT
            )
        )
        binding.txtFarsiPreview.textSize = requireContext().appPrefsLite.getFloat(
            PREF_FARSI_FONT_SIZE,
            DEFAULT_FARSI_FONT_SIZE
        )
    }
}//end of class
package ir.namoo.quran.ui.fragments

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentQuranSettingBinding
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.quran.utils.DEFAULT_ENGLISH_FONT
import ir.namoo.quran.utils.DEFAULT_ENGLISH_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_FARSI_FONT
import ir.namoo.quran.utils.DEFAULT_FARSI_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_KURDISH_FONT
import ir.namoo.quran.utils.DEFAULT_KURDISH_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_PLAY_TYPE
import ir.namoo.quran.utils.DEFAULT_QURAN_FONT
import ir.namoo.quran.utils.DEFAULT_QURAN_FONT_SIZE
import ir.namoo.quran.utils.DEFAULT_SELECTED_QARI
import ir.namoo.quran.utils.DEFAULT_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.PREF_ENGLISH_FONT
import ir.namoo.quran.utils.PREF_ENGLISH_FONT_SIZE
import ir.namoo.quran.utils.PREF_ENGLISH_TRANSLATE
import ir.namoo.quran.utils.PREF_ENGLISH_TRANSLITERATION
import ir.namoo.quran.utils.PREF_FARSI_FONT
import ir.namoo.quran.utils.PREF_FARSI_FONT_SIZE
import ir.namoo.quran.utils.PREF_FARSI_FULL_TRANSLATE
import ir.namoo.quran.utils.PREF_FARSI_TRANSLATE
import ir.namoo.quran.utils.PREF_KURDISH_FONT
import ir.namoo.quran.utils.PREF_KURDISH_FONT_SIZE
import ir.namoo.quran.utils.PREF_KURDISH_TRANSLATE
import ir.namoo.quran.utils.PREF_PLAY_TYPE
import ir.namoo.quran.utils.PREF_QURAN_FONT
import ir.namoo.quran.utils.PREF_QURAN_FONT_SIZE
import ir.namoo.quran.utils.PREF_SELECTED_QARI
import ir.namoo.quran.utils.PREF_STORAGE_PATH
import ir.namoo.quran.utils.PREF_TRANSLATE_TO_PLAY
import ir.namoo.quran.utils.getRootDirs
import ir.namoo.quran.utils.getSelectedQuranDirectoryPath
import ir.namoo.religiousprayers.ui.shared.ShapedAdapter
import javax.inject.Inject


@AndroidEntryPoint
class SettingFragment : Fragment() {

    private lateinit var binding: FragmentQuranSettingBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuranSettingBinding.inflate(inflater)
        binding.appBar.toolbar.let {
            it.setTitle(R.string.settings)
            it.setupMenuNavigation()
        }
        binding.appBar.root.hideToolbarBottomShadow()
        initCheckBoxes()
        initQari()
        initArabic()
        initEnglish()
        initKurdish()
        initFarsi()


        binding.spinnerStorage.apply {
            val dirs = getRootDirs(requireContext())
            val names = arrayListOf<String>()
            for (d in dirs)
                names.add(d?.absolutePath ?: "-")
            adapter = ShapedAdapter(
                requireContext(),
                R.layout.select_dialog_item,
                R.id.text1,
                names.toTypedArray()
            )
            val storage = prefs.getString(
                PREF_STORAGE_PATH,
                getSelectedQuranDirectoryPath(requireContext())
            )
            setSelection(if (names.contains(storage)) names.indexOf(storage) else 0)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    prefs.edit {
                        putString(PREF_STORAGE_PATH, selectedItem.toString())
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}

            }
        }
        return binding.root
    }//end of onCreateView

    private fun initCheckBoxes() {
        listOf(
            Triple(binding.checkboxEnglish, PREF_ENGLISH_TRANSLATE, false),
            Triple(binding.checkboxEnglishTransliteration, PREF_ENGLISH_TRANSLITERATION, false),
            Triple(binding.checkboxKurdish, PREF_KURDISH_TRANSLATE, true),
            Triple(binding.checkboxFarsi, PREF_FARSI_TRANSLATE, true),
            Triple(binding.checkboxFarsiFull, PREF_FARSI_FULL_TRANSLATE, false)
        ).forEach { (checkBox, pref, default) ->
            checkBox.isChecked = prefs.getBoolean(pref, default)
            checkBox.setOnClickListener {
                prefs.edit {
                    putBoolean(pref, checkBox.isChecked)
                }
                if (checkBox.id == R.id.checkbox_farsi) {
                    binding.checkboxFarsiFull.isVisible = prefs.getBoolean(
                        PREF_FARSI_TRANSLATE, default
                    )
                }
            }
        }
        binding.checkboxFarsiFull.isVisible = prefs.getBoolean(
            PREF_FARSI_TRANSLATE, true
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
            adapter = ShapedAdapter<String>(
                requireContext(),
                R.layout.select_dialog_item,
                R.id.text1,
                names
            )
            setSelection(
                values.indexOf(
                    prefs.getString(
                        PREF_SELECTED_QARI,
                        DEFAULT_SELECTED_QARI
                    )
                )
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    prefs.edit {
                        putString(
                            PREF_SELECTED_QARI,
                            values[names.indexOf(binding.spinnerSelectQari.selectedItem.toString())]
                        )
                    }
                }
            }
        }

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
            adapter = ShapedAdapter<String>(
                requireContext(),
                R.layout.select_dialog_item,
                R.id.text1,
                tNames
            )
            setSelection(
                tValues.indexOf(
                    prefs.getString(
                        PREF_TRANSLATE_TO_PLAY,
                        DEFAULT_TRANSLATE_TO_PLAY
                    )
                )
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    prefs.edit {
                        putString(
                            PREF_TRANSLATE_TO_PLAY,
                            tValues[tNames.indexOf(binding.spinnerSelectTranslateToPlay.selectedItem.toString())]
                        )
                    }
                }
            }
        }

        binding.playType.apply {
            check(
                when (prefs.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE)) {
                    1 -> R.id.radio_arabic_translate
                    2 -> R.id.radio_arabic
                    else -> R.id.radio_translate
                }
            )
            setOnCheckedChangeListener { group, checkedId ->
                prefs.edit {
                    putInt(
                        PREF_PLAY_TYPE, when (checkedId) {
                            R.id.radio_arabic_translate -> 1
                            R.id.radio_arabic -> 2
                            else -> 3
                        }
                    )
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
                ShapedAdapter<String>(
                    requireContext(),
                    R.layout.select_dialog_item,
                    R.id.text1,
                    fontNames
                )
            setSelection(
                fontValues.indexOf(
                    prefs.getString(
                        PREF_QURAN_FONT, DEFAULT_QURAN_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    prefs.edit {
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
            progress = prefs.getFloat(
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
                    prefs.edit {
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
                ShapedAdapter<String>(
                    requireContext(),
                    R.layout.select_dialog_item,
                    R.id.text1,
                    fontNames
                )
            setSelection(
                fontValues.indexOf(
                    prefs.getString(
                        PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    prefs.edit {
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
            progress = prefs.getFloat(
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
                    prefs.edit {
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
                ShapedAdapter<String>(
                    requireContext(),
                    R.layout.select_dialog_item,
                    R.id.text1,
                    fontNames
                )
            setSelection(
                fontValues.indexOf(
                    prefs.getString(
                        PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    prefs.edit {
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
            progress = prefs.getFloat(
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
                    prefs.edit {
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
                ShapedAdapter<String>(
                    requireContext(),
                    R.layout.select_dialog_item,
                    R.id.text1,
                    fontNames
                )
            setSelection(
                fontValues.indexOf(
                    prefs.getString(
                        PREF_FARSI_FONT, DEFAULT_FARSI_FONT
                    )
                )
            )
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    prefs.edit {
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
            progress = prefs.getFloat(
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
                    prefs.edit {
                        putFloat(PREF_FARSI_FONT_SIZE, progress.toFloat())
                    }
                    updateFarsiPrev()
                }
            })
        }


    }//end of initFarsi

    private fun updateArabicPrev() {
        binding.txtArabicPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, prefs.getString(
                PREF_QURAN_FONT, DEFAULT_QURAN_FONT
            )
        )
        binding.txtArabicPreview.textSize = prefs.getFloat(
            PREF_QURAN_FONT_SIZE,
            DEFAULT_QURAN_FONT_SIZE
        )
    }

    private fun updateEnglishPrev() {
        binding.txtEnglishPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, prefs.getString(
                PREF_ENGLISH_FONT, DEFAULT_ENGLISH_FONT
            )
        )
        binding.txtEnglishPreview.textSize = prefs.getFloat(
            PREF_ENGLISH_FONT_SIZE,
            DEFAULT_ENGLISH_FONT_SIZE
        )
    }

    private fun updateKurdishPrev() {
        binding.txtKurdishPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, prefs.getString(
                PREF_KURDISH_FONT, DEFAULT_KURDISH_FONT
            )
        )
        binding.txtKurdishPreview.textSize = prefs.getFloat(
            PREF_KURDISH_FONT_SIZE,
            DEFAULT_KURDISH_FONT_SIZE
        )
    }

    private fun updateFarsiPrev() {
        binding.txtFarsiPreview.typeface = Typeface.createFromAsset(
            requireContext().assets, prefs.getString(
                PREF_FARSI_FONT, DEFAULT_FARSI_FONT
            )
        )
        binding.txtFarsiPreview.textSize = prefs.getFloat(
            PREF_FARSI_FONT_SIZE,
            DEFAULT_FARSI_FONT_SIZE
        )
    }
}//end of class

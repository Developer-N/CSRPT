package ir.namoo.quran.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import ir.namoo.quran.utils.englishFontNames
import ir.namoo.quran.utils.englishFonts
import ir.namoo.quran.utils.farsiFontNames
import ir.namoo.quran.utils.farsiFonts
import ir.namoo.quran.utils.kurdishFontNames
import ir.namoo.quran.utils.kurdishFonts
import ir.namoo.quran.utils.quranFontNames
import ir.namoo.quran.utils.quranFonts
import org.koin.androidx.compose.koinViewModel

@Composable
fun FontSettingItems(viewModel: SettingViewModel = koinViewModel()) {
    val quranFontName by viewModel.quranFontName.collectAsState()
    val quranFontSize by viewModel.quranFontSize.collectAsState()

    val kurdishFontName by viewModel.kurdishFontName.collectAsState()
    val kurdishFontSize by viewModel.kurdishFontSize.collectAsState()

    val farsiFontName by viewModel.farsiFontName.collectAsState()
    val farsiFontSize by viewModel.farsiFontSize.collectAsState()

    val englishFontName by viewModel.englishFontName.collectAsState()
    val englishFontSize by viewModel.englishFontSize.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        // -------------------------------------------------------------------- Quran
        MyFontSelector(modifier = Modifier.padding(2.dp),
            title = stringResource(id = R.string.quran_font_setting),
            preview = stringResource(id = R.string.str_bismillah),
            fontList = quranFonts,
            fontNames = quranFontNames,
            selectedFont = if (quranFonts.contains(quranFontName)) quranFontName else quranFonts.first(),
            fontSize = quranFontSize,
            onFontFamilyChanged = { viewModel.updateQuranFontName(it) },
            onFontSizeChanged = { viewModel.updateQuranFontSize(it) })
        // -------------------------------------------------------------------- Kurdish
        MyFontSelector(modifier = Modifier.padding(2.dp),
            title = stringResource(id = R.string.kurdish_font_setting),
            preview = stringResource(id = R.string.str_kurdish_prev),
            fontList = kurdishFonts,
            fontNames = kurdishFontNames,
            selectedFont = if (kurdishFonts.contains(kurdishFontName)) kurdishFontName else kurdishFonts.first(),
            fontSize = kurdishFontSize,
            onFontFamilyChanged = { viewModel.updateKurdishFontName(it) },
            onFontSizeChanged = { viewModel.updateKurdishFontSize(it) })
        // -------------------------------------------------------------------- Farsi
        MyFontSelector(modifier = Modifier.padding(2.dp),
            title = stringResource(id = R.string.farsi_font_setting),
            preview = stringResource(id = R.string.str_farsi_font),
            fontList = farsiFonts,
            fontNames = farsiFontNames,
            selectedFont = if (farsiFonts.contains(farsiFontName)) farsiFontName else farsiFonts.first(),
            fontSize = farsiFontSize,
            onFontFamilyChanged = { viewModel.updateFarsiFontName(it) },
            onFontSizeChanged = { viewModel.updateFarsiFontSize(it) })
        // -------------------------------------------------------------------- English
        MyFontSelector(modifier = Modifier.padding(2.dp),
            title = stringResource(id = R.string.english_font_setting),
            preview = stringResource(id = R.string.english_font_preview),
            fontList = englishFonts,
            fontNames = englishFontNames,
            selectedFont = if (englishFonts.contains(englishFontName)) englishFontName else englishFonts.first(),
            fontSize = englishFontSize,
            onFontFamilyChanged = { viewModel.updateEnglishFontName(it) },
            onFontSizeChanged = { viewModel.updateEnglishFontSize(it) })

    }
}

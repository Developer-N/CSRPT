package ir.namoo.quran.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import ir.namoo.quran.utils.QCF2BISMLFont

@Composable
fun MushafPageComponent(
    pageState: PageState,
    playingSura: Int,
    playingVerse: Int,
    getSuraName: (Int) -> String,
    fontFamily: FontFamily? = null,
    fontSizeSp: Float = 24f,
    selectedVerseId: Int? = null,
    onVerseClick: (Verse) -> Unit,
    onSurahPositionsCalculated: ((List<Pair<Float, Int>>) -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val numeral by numeral.collectAsState()
    val font = fontFamily ?: FontFamily(pageState.getFont(context))
    val lineHeightSp = (fontSizeSp * 1.7f).sp
    val suraMarkers = remember { mutableListOf<Pair<Int, Int>>() }
    val pageText = buildAnnotatedString {
        suraMarkers.clear()
        pageState.verses.forEach { verse ->
            val isSelected =
                verse.id == selectedVerseId || (verse.sura == playingSura && verse.verseNumber == playingVerse)
            val color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
            val backgroundColor =
                MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 0.1f else 0f)
            if (verse.verseNumber == 1) {
                suraMarkers.add(this.length to verse.sura)
                withStyle(style = ParagraphStyle(textAlign = TextAlign.Center)) {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily(QCF2BISMLFont), fontSize = 32.sp
                        )
                    ) {
                        append(getSuraName(verse.sura))
                    }
                }
                if (verse.sura != 1 && verse.sura != 9) {
                    withStyle(style = ParagraphStyle(textAlign = TextAlign.Center)) {
                        withStyle(
                            style = SpanStyle(
                                fontFamily = FontFamily(Font(R.font.qcf2001)),
                                fontSize = fontSizeSp.sp
                            )
                        ) {
                            append("ﱁ ﱂ ﱃ ﱄ")
                        }
                    }
                }
            }
            withLink(
                LinkAnnotation.Clickable(
                    tag = verse.id.toString(),
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            fontFamily = font,
                            fontSize = fontSizeSp.sp,
                            color = color,
                            background = backgroundColor
                        )
                    ),
                    linkInteractionListener = {
                        onVerseClick(verse)
                    },
                )
            ) {
                append(verse.verseQCFText)
            }

            append(" ")
        }
    }
    val verticalPaddingPx = with(density) { 8.dp.toPx() }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            text = pageText,
            lineHeight = lineHeightSp,
            textAlign = TextAlign.Justify,
            onTextLayout = { layoutResult ->
                val positions = suraMarkers.map { (charIndex, suraId) ->
                    val line = layoutResult.getLineForOffset(charIndex)
                    val topY = layoutResult.getLineTop(line) + verticalPaddingPx
                    topY to suraId
                }
                onSurahPositionsCalculated?.invoke(positions)
            })

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            text = numeral.format("${pageState.page}"),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(locale = "fa")
@Composable
private fun PagePreview() {
    MaterialTheme {
        MushafPageComponent(
            pageState = PageState(
                page = 1, verses = listOf(
                    Verse(1, 1, 1, "ﱁ ﱂ ﱃ ﱄ ﱅ", "NormalText", "CleanText", "Note", 0, emptyList()),
                    Verse(2, 1, 2, "ﱆ ﱇ ﱈ ﱉ ﱊ", "NormalText", "CleanText", "Note", 0, emptyList()),
                    Verse(3, 1, 3, "ﱋ ﱌ ﱍ", "NormalText", "CleanText", "Note", 0, emptyList()),
                    Verse(4, 1, 4, "ﱎ ﱏ ﱐ ﱑ", "NormalText", "CleanText", "Note", 0, emptyList()),
                    Verse(5, 1, 5, "ﱒ ﱓ ﱔ ﱕ ﱖ", "NormalText", "CleanText", "Note", 0, emptyList()),
                    Verse(6, 1, 6, "ﱗ ﱘ ﱙ ﱚ", "NormalText", "CleanText", "Note", 0, emptyList()),
                    Verse(
                        7,
                        1,
                        7,
                        "ﱛ ﱜ ﱝ ﱞ ﱟ ﱠ ﱡ ﱢ ﱣ ﱤ",
                        "NormalText",
                        "CleanText",
                        "Note",
                        0,
                        emptyList()
                    )
                )
            ),
            playingSura = 1,
            playingVerse = 2,
            getSuraName = { "-" },
            fontFamily = FontFamily(Font(R.font.qcf2001)),
            selectedVerseId = null,
            onVerseClick = {},
        )
    }
}

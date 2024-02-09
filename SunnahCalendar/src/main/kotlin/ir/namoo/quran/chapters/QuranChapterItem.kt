package ir.namoo.quran.chapters

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.quran.chapters.data.ChapterEntity
import kotlinx.coroutines.launch

@Composable
fun QuranChapterItem(
    modifier: Modifier = Modifier,
    chapter: ChapterEntity,
    rowID: Int,
    query: String,
    onFavClick: () -> Unit,
    cardClick: () -> Unit
) {
    val scale = remember { Animatable(1.3f) }
    val coroutineScope = rememberCoroutineScope()
    ElevatedCard(modifier = modifier
        .fillMaxWidth()
        .padding(4.dp, 2.dp),
        onClick = { cardClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            val suraName = buildAnnotatedString {
                append(formatNumber(rowID) + " : ")
                if (query.isNotEmpty() && chapter.nameArabic.contains(query)) {
                    val index = chapter.nameArabic.indexOf(query)
                    for (i in 0..<index) append("${chapter.nameArabic[i]}")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                        )
                    ) {
                        append(query)
                    }
                    for (i in (index + query.length)..<chapter.nameArabic.length) append("${chapter.nameArabic[i]}")
                } else append(chapter.nameArabic)
            }
            Text(
                modifier = Modifier
                    .weight(3f)
                    .padding(12.dp, 2.dp),
                text = suraName,
                fontSize = 20.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(16.dp, 2.dp)
            ) {
                Text(
                    modifier = Modifier.padding(0.dp),
                    text = if (chapter.type == "Meccan") stringResource(id = R.string.meccan) else stringResource(
                        id = R.string.medinan
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier.padding(0.dp), text = formatNumber(
                        String.format(
                            stringResource(id = R.string.aya_count), chapter.ayaCount
                        )
                    ), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier.padding(0.dp), text = formatNumber(
                        String.format(
                            stringResource(id = R.string.revelation_order), chapter.revelationOrder
                        )
                    ), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(modifier = Modifier
                .weight(1f)
                .scale(scale.value), onClick = {
                onFavClick()
                coroutineScope.launch {
                    scale.animateTo(0.3f)
                    scale.animateTo(1.3f)
                }
            }) {
                Icon(
                    imageVector = if (chapter.fav == 1) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.favorite),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

package ir.namoo.quran.chapters

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.global.numeral
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.utils.QCF2BISMLFont

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuranChapterItem(
    modifier: Modifier = Modifier,
    chapter: ChapterEntity,
    rowID: Int,
    query: String,
    qcfText: String,
    isInSearch: Boolean,
    onFavClick: () -> Unit,
    cardClick: () -> Unit
) {
    val numeral by numeral.collectAsState()
    val suraName = buildAnnotatedString {
        append(numeral.format(rowID) + ": ")
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
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp, 2.dp),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = cardClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(targetState = isInSearch, modifier = Modifier.weight(3f)) {
                if (it) {
                    Text(
                        modifier = Modifier.padding(12.dp, 2.dp),
                        text = suraName,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(12.dp, 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = numeral.format("$rowID: "),
                            fontSize = 20.sp
                        )
                        Text(
                            text = qcfText,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Start,
                            fontFamily = FontFamily(QCF2BISMLFont)
                        )
                    }
                }
            }
            FlowRow(
                modifier = Modifier
                    .weight(4f)
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier.padding(0.dp),
                    text = if (chapter.type == "Meccan") stringResource(id = R.string.meccan)
                    else stringResource(id = R.string.medinan),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier.padding(0.dp), text = numeral.format(
                        String.format(
                            stringResource(id = R.string.aya_count), chapter.ayaCount
                        )
                    ), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier.padding(0.dp), text = numeral.format(
                        String.format(
                            stringResource(id = R.string.revelation_order), chapter.revelationOrder
                        )
                    ), fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                modifier = Modifier.weight(1f), onClick = onFavClick,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                AnimatedContent(
                    targetState = chapter.fav, label = "fav",
                    transitionSpec = {
                        if (chapter.fav == 1)
                            slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) { -it } togetherWith slideOutHorizontally { it }
                        else
                            slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) { it } togetherWith slideOutHorizontally { -it }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = if (it == 1) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.favorite)
                    )
                }
            }
        }
    }
}

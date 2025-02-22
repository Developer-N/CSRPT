package ir.namoo.religiousprayers.ui.azkar

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.religiousprayers.ui.azkar.data.AzkarChapter

@Composable
fun AzkarChapterUI(
    azkar: AzkarChapter,
    lang: String,
    searchText: String = "",
    onFavClick: (zkr: AzkarChapter) -> Unit,
    onCardClick: (id: Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clickable { onCardClick(azkar.id) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val chapterText = when (lang) {
                Language.FA.code -> azkar.persian
                Language.CKB.code -> azkar.kurdish
                else -> azkar.arabic
            }
            val txt = buildAnnotatedString {
                withStyle(style = SpanStyle(fontSize = MaterialTheme.typography.titleLarge.fontSize)) {
                    append(formatNumber(azkar.id))
                }
                append(" : ")
                if (searchText.isNotEmpty() && chapterText?.contains(searchText) == true) {
                    val index = chapterText.indexOf(searchText)
                    for (i in 0..<index) append("${chapterText[i]}")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error
                        )
                    ) {
                        append(searchText)
                    }
                    for (i in (index + searchText.length)..<chapterText.length) append("${chapterText[i]}")
                } else append(chapterText)
            }
            if (searchText.isEmpty())
                AnimatedContent(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .weight(1f),
                    targetState = txt,
                    label = "title"
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = it,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            else
                Text(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .weight(1f),
                    text = txt,
                    fontWeight = FontWeight.SemiBold
                )
            AnimatedContent(targetState = azkar.fav, label = "fav") {
                IconButton(onClick = {
                    onFavClick(azkar)
                }) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = if (it == 1) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.favorite),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

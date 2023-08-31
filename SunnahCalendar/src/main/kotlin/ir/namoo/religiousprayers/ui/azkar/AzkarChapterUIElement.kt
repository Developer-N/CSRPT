package ir.namoo.religiousprayers.ui.azkar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.iconColor
import ir.namoo.religiousprayers.ui.azkar.data.AzkarChapter
import kotlinx.coroutines.launch

@Composable
fun ZikrChapterUI(
    zikr: AzkarChapter,
    lang: String,
    searchText: String = "",
    onFavClick: (zkr: AzkarChapter) -> Unit,
    onCardClick: (id: Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable { onCardClick(zikr.id) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = CardDefaults.elevatedShape,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            val coroutineScope = rememberCoroutineScope()
            val scale = remember { Animatable(1f) }
            val zikrText = when (lang) {
                Language.FA.code -> zikr.persian
                Language.CKB.code -> zikr.kurdish
                else -> zikr.arabic
            }
            val txt = buildAnnotatedString {
                append(formatNumber(zikr.id))
                append(" : ")
                if (searchText.isNotEmpty() && zikrText?.contains(searchText) == true) {
                    val index = zikrText.indexOf(searchText)
                    for (i in 0..<index) append("${zikrText[i]}")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    ) {
                        append(searchText)
                    }
                    for (i in (index + searchText.length)..<zikrText.length) append("${zikrText[i]}")
                } else append(zikrText)
            }
            Text(
                modifier = Modifier
                    .weight(8f)
                    .padding(4.dp),
                text = txt,
                fontFamily = FontFamily(appFont)
            )

            IconButton(onClick = {
                onFavClick(zikr)
                coroutineScope.launch {
                    scale.animateTo(0.5f, animationSpec = tween(50))
                    scale.animateTo(1f, animationSpec = tween(100))
                }
            }) {
                Icon(
                    modifier = Modifier
                        .scale(scale = scale.value)
                        .size(32.dp)
                        .weight(2f),
                    imageVector = if (zikr.fav == 1) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.favorite),
                    tint = iconColor
                )
            }


        }
    }


}

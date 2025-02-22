package ir.namoo.religiousprayers.ui.downloadtimes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun CityItemUIElement(
    city: CityItemState, searchText: String, download: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (city.isSelected) MaterialTheme.colorScheme.primaryContainer else CardDefaults.elevatedCardColors().containerColor,
        label = "containerColor",
        animationSpec = tween()
    )
    val contentColor by animateColorAsState(
        targetValue = if (city.isSelected) MaterialTheme.colorScheme.onPrimaryContainer else CardDefaults.elevatedCardColors().contentColor,
        label = "contentColor",
        animationSpec = tween()
    )
    ElevatedCard(
        modifier = Modifier
            .padding(8.dp, 2.dp)
            .fillMaxWidth()
            .clickable { download() },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val cityName = buildAnnotatedString {
                if (searchText.isNotEmpty() && city.name.contains(searchText)) {
                    val index = city.name.indexOf(searchText)
                    for (i in 0..<index) append("${city.name[i]}")
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = if (city.isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.error
                        )
                    ) {
                        append(searchText)
                    }
                    for (i in (index + searchText.length)..<city.name.length) append("${city.name[i]}")
                } else append(city.name)
            }
            Text(
                text = cityName, modifier = Modifier
                    .weight(4f)
                    .padding(horizontal = 4.dp),
                fontWeight = FontWeight.SemiBold,
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = city.lastUpdate, modifier = Modifier.weight(4f),
                fontWeight = FontWeight.SemiBold,
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
            Spacer(modifier = Modifier.width(4.dp))
            AnimatedVisibility(visible = city.isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .size(26.dp), strokeCap = StrokeCap.Round
                )
            }
            AnimatedVisibility(visible = !city.isDownloading) {
                IconButton(modifier = Modifier
                    .weight(1f)
                    .height(32.dp), onClick = { download() }) {
                    Icon(
                        imageVector = if (city.isDownloaded) Icons.Filled.DownloadDone else Icons.Filled.CloudDownload,
                        contentDescription = "Selected",
                        tint = if (city.isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

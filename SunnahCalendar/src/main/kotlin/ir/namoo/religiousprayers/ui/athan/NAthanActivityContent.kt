package ir.namoo.religiousprayers.ui.athan

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.ui.utils.isLight

@Composable
fun NAthanActivityContent(
    title: String,
    subtitle: String,
    applyEdgeToEdge: (Boolean, Boolean) -> Unit,
    stop: () -> Unit
) {
    AppTheme {
        val isBackgroundColorLight = MaterialTheme.colorScheme.background.isLight
        val isSurfaceColorLight = MaterialTheme.colorScheme.surface.isLight
        LaunchedEffect(isBackgroundColorLight, isSurfaceColorLight) {
            applyEdgeToEdge(isBackgroundColorLight, isSurfaceColorLight)
        }
        Scaffold { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.adhan_background),
                        contentDescription = "",
                        contentScale = ContentScale.FillBounds
                    )
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                            .alpha(0.98f), shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(7f)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    modifier = Modifier.alpha(AppBlendAlpha),
                                    text = subtitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(modifier = Modifier.weight(1f), onClick = { stop() }) {
                                Icon(
                                    modifier = Modifier.size(100.dp),
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.close),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

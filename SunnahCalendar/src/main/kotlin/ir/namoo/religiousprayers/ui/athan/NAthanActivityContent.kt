package ir.namoo.religiousprayers.ui.athan

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NAthanActivityContent(
    title: String,
    subtitle: String,
    background: String?,
    stop: () -> Unit
) {
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .padding(4.dp),
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        modifier = Modifier
                            .alpha(AppBlendAlpha)
                            .padding(4.dp)
                            .basicMarquee(),
                        text = subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            actions = {
                IconButton(onClick = stop) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            },
            colors = appTopAppBarColors()
        )
    }) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(context).data(background)
                    .crossfade(true).build(),
                placeholder = painterResource(R.drawable.adhan_background),
                error = painterResource(R.drawable.adhan_background),
                contentDescription = stringResource(R.string.athan_background),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

package ir.namoo.religiousprayers.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun IntroWelcomeScreen(goToDownloadLocation: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    text = stringResource(id = R.string.app_name),
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    text = stringResource(id = R.string.str_welcome),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Justify
                )
                Button(modifier = Modifier.padding(8.dp), onClick = { goToDownloadLocation() }) {
                    Text(
                        text = stringResource(id = R.string.select_city),
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize
                    )

                    Icon(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = stringResource(id = R.string.city)
                    )
                }
            }
        }
    }
}

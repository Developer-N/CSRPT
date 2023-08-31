package ir.namoo.religiousprayers.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import ir.namoo.commons.utils.appFont

@Composable
fun IntroWelcomeScreen(goToDownloadLocation: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp),
            text = stringResource(id = R.string.app_name),
            fontFamily = FontFamily(appFont),
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            text = stringResource(id = R.string.str_welcome),
            fontFamily = FontFamily(appFont),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
        Button(modifier = Modifier.padding(8.dp), onClick = { goToDownloadLocation() }) {
            Text(
                text = stringResource(id = R.string.select_city),
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                fontFamily = FontFamily(appFont)
            )

            Icon(
                modifier = Modifier.padding(horizontal = 8.dp),
                imageVector = Icons.Filled.LocationCity,
                contentDescription = stringResource(id = R.string.city)
            )
        }
    }
}

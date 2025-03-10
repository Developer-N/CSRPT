package ir.namoo.religiousprayers.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun ContactUIElement(namooClick: () -> Unit, developerNClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        ContactsButtons(namooClick = namooClick, developerNClick = developerNClick)
    }
}

@Composable
fun ContactsButtons(
    namooClick: () -> Unit, developerNClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            ElevatedButton(onClick = namooClick) {
                Text(text = stringResource(R.string.app_channel), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.padding(4.dp))
                Icon(
                    painterResource(id = R.drawable.ic_tg),
                    contentDescription = stringResource(R.string.app_channel),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            ElevatedButton(onClick = developerNClick) {
                Text(text = stringResource(R.string.support), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.padding(4.dp))
                Icon(
                    painterResource(id = R.drawable.ic_tg),
                    contentDescription = stringResource(R.string.support),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


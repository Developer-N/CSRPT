package ir.namoo.religiousprayers.ui.donate

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.utils.isPackageInstalled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DonateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = "Scroll") {
        delay(1000)
        scrollState.animateScrollTo(1000)
        delay(1000)
        scrollState.animateScrollTo(0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.close), fontWeight = FontWeight.SemiBold)
            }
        },
        icon = { Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = "") },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.donate),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Text(
                    text = formatNumber(stringResource(id = R.string.donate_msg)),
                    textAlign = TextAlign.Justify
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ElevatedButton(onClick = {
                        scope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "CardNumber", "5859831016333741"
                                    )
                                )
                            )
                        }
                        Toast.makeText(
                            context, "شماره کارت کپی شد! 😃", Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = stringResource(id = R.string.copy)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "5859-8310-1633-3741")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(id = R.string.copy)
                        )
                    }
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(
                        "https://zarinp.al/371439",
                        "https://zarinp.al/371438",
                        "https://zarinp.al/371440",
                        "https://zarinp.al/371441"
                    ).zip(
                        listOf(
                            stringResource(id = R.string._5),
                            stringResource(id = R.string._10),
                            stringResource(id = R.string._20),
                            stringResource(id = R.string._50)
                        )
                    ).forEach { pair ->
                        ElevatedButton(
                            onClick = { openDonateUrl(pair.first, context) }) {
                            Text(
                                text = formatNumber(pair.second),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = pair.second
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val url = "https://zarinp.al/namoo"
                    ElevatedButton(onClick = { openDonateUrl(url, context) }) {
                        Text(
                            text = stringResource(id = R.string.custom_donate),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = stringResource(id = R.string.custom_donate)
                        )
                    }
                }
            }
        })
}

private fun openDonateUrl(url: String?, context: Context) {
    url?.let { url ->
        CustomTabsIntent.Builder().build().apply {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (isPackageInstalled(
                    "com.android.chrome", context.packageManager
                )
            ) intent.setPackage("com.android.chrome")
        }.launchUrl(context, url.toUri())
    }
}

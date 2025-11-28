package ir.namoo.religiousprayers.ui.donate

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.byagowi.persiancalendar.R
import ir.namoo.commons.PREF_LAST_SHOW_DONATE_DIALOG
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.isPackageInstalled
import ir.namoo.commons.utils.toastMessage
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun DonateDialog(onDismiss: () -> Unit, copyCardNumber: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var cardCopied by remember { mutableStateOf(false) }
//    val numeral by numeral.collectAsState()

    LaunchedEffect(key1 = Unit) {
        context.appPrefsLite.edit {
            putInt(PREF_LAST_SHOW_DONATE_DIALOG, Calendar.getInstance()[Calendar.DAY_OF_YEAR])
        }
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
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.donate_msg),
                    textAlign = TextAlign.Justify
                )
                ElevatedButton(onClick = {
                    scope.launch {
                        clipboardManager.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    "CardNumber", "5859831016333741"
                                )
                            )
                        )
                        if (!cardCopied) {
                            copyCardNumber()
                            cardCopied = true
                        }
                    }
                    context.toastMessage(context.getString(R.string.card_copied))
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

                TextButton(onClick = {
                    openDonateUrl("https://namoodev.ir/donate", context)
                }) {
                    Text(
                        text = stringResource(R.string.donate_page_in_site),
                        fontWeight = FontWeight.Bold
                    )
                }
                /*
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
                                text = numeral.format(pair.second),
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
                }*/
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

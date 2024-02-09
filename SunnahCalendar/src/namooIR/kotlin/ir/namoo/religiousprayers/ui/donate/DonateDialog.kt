package ir.namoo.religiousprayers.ui.donate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.utils.isPackageInstalled
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = "Scroll") {
        delay(1000)
        scrollState.animateScrollTo(1000)
        delay(1000)
        scrollState.animateScrollTo(0)
    }

    AlertDialog(onDismissRequest = { onDismiss() }, confirmButton = {
        TextButton(onClick = { onDismiss() }) {
            Text(text = stringResource(id = R.string.close))
        }
    }, title = {
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
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ElevatedAssistChip(modifier = Modifier.padding(8.dp),
                        onClick = {
                            clipboardManager.setText(AnnotatedString("5859831016333741"))
                            Toast.makeText(
                                context, "شماره کارت کپی شد! 😃", Toast.LENGTH_SHORT
                            ).show()
                        },
                        label = {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = stringResource(id = R.string.bank_number),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }, trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = stringResource(id = R.string.copy)
                            )
                        })
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalArrangement = Arrangement.SpaceEvenly
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
                        ElevatedAssistChip(
                            onClick = { openDonateUrl(pair.first, context) },
                            label = {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = formatNumber(pair.second),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = pair.second
                                )
                            })
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
                    ElevatedAssistChip(
                        onClick = { openDonateUrl(url, context) },
                        label = {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = stringResource(id = R.string.custom_donate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = stringResource(id = R.string.custom_donate)
                            )
                        })
                }
            }
        })
}

private fun openDonateUrl(url: String?, context: Context) {
    CustomTabsIntent.Builder().build().apply {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (isPackageInstalled(
                "com.android.chrome", context.packageManager
            )
        ) intent.setPackage("com.android.chrome")
    }.launchUrl(context, Uri.parse(url))
}

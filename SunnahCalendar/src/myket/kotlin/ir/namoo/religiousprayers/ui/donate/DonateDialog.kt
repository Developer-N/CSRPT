package ir.namoo.religiousprayers.ui.donate

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.utils.formatNumber
import ir.myket.billingclient.IabHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isBtnsEnabled by remember { mutableStateOf(false) }
    val mHelper = IabHelper(context, BuildConfig.IAB_PUBLIC_KEY)
    mHelper.startSetup {
        if (it.isSuccess) isBtnsEnabled = true
        else {
            isBtnsEnabled = false
            Toast.makeText(context, "مشکلی در ارتباط با مایکت به وجود آمد!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    val mPurchaseFinishedListener =
        IabHelper.OnIabPurchaseFinishedListener { result, _ ->
            if (result.isFailure) {
                Toast.makeText(
                    context, "خطایی رخ داد \uD83D\uDE15" +
                            "\n" +
                            "ان شاءالله دفعه ی بعدی کمک کنید 🙂", Toast.LENGTH_SHORT
                ).show()

            }
            if (result.isSuccess) {
                Toast.makeText(context, "جزاکم الله الخیرا ❤️", Toast.LENGTH_SHORT).show()
            }
        }

    DisposableEffect(key1 = mHelper) {
        onDispose {
            mHelper.dispose()
        }
    }

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
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "donate5",
                        "donate10",
                        "donate20",
                        "donate50"
                    ).zip(
                        listOf(
                            stringResource(id = R.string._5),
                            stringResource(id = R.string._10),
                            stringResource(id = R.string._20),
                            stringResource(id = R.string._50)
                        )
                    ).forEach { pair ->
                        ElevatedAssistChip(
                            onClick = {
                                mHelper.launchPurchaseFlow(
                                    context.getActivity(),
                                    pair.first,
                                    mPurchaseFinishedListener
                                )
                            },
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
                            }, enabled = isBtnsEnabled
                        )
                    }
                }
            }
        })
}

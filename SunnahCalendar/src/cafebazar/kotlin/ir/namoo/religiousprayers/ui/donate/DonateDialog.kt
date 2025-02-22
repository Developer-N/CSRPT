package ir.namoo.religiousprayers.ui.donate

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.getActivity
import com.byagowi.persiancalendar.utils.formatNumber
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlinx.coroutines.delay

@Composable
fun DonateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val localSecurityCheck = SecurityCheck.Enable(
        rsaPublicKey = "your-key"
    )
    val paymentConfiguration = PaymentConfiguration(
        localSecurityCheck = localSecurityCheck
    )
    val payment = Payment(context = context, config = paymentConfiguration)
    val paymentConnection = payment.connect {
        connectionSucceed {
            Toast.makeText(
                context, "اتصال به کافه بازار برقرار شد.", Toast.LENGTH_SHORT
            ).show()

        }
        connectionFailed { throwable ->
            Toast.makeText(context, "خطایی رخ داد. $throwable", Toast.LENGTH_SHORT)
                .show()
        }
        disconnected {
            Toast.makeText(context, "اتصال به کافه بازار قطع شد.", Toast.LENGTH_SHORT)
                .show()
        }
    }
    LaunchedEffect(key1 = "Scroll") {
        delay(1000)
        scrollState.animateScrollTo(1000)
        delay(1000)
        scrollState.animateScrollTo(0)
    }

    DisposableEffect(key1 = paymentConnection) {
        onDispose {
            paymentConnection.disconnect()
        }
    }

    AlertDialog(onDismissRequest = onDismiss,
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
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Justify
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
                        ElevatedButton(
                            onClick = { purchase(context, pair.first, payment) }) {
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
            }
        })
}

private fun purchase(context: Context, productID: String, payment: Payment) {
    val purchaseRequest = PurchaseRequest(
        productId = productID,
        payload = "PAYLOAD"
    )
    val activity = context.getActivity() ?: return
    payment.purchaseProduct(
        registry = activity.activityResultRegistry,
        request = purchaseRequest
    ) {
        purchaseFlowBegan {
            Toast.makeText(context, "رفتیم برای کمک 😃", Toast.LENGTH_SHORT).show()
        }
        failedToBeginFlow { throwable ->
            Toast.makeText(context, "خطایی رخ داد 😕 $throwable", Toast.LENGTH_SHORT)
                .show()
        }
        purchaseSucceed {
            Toast.makeText(context, "جزاکم الله الخیرا ❤️", Toast.LENGTH_SHORT).show()
        }
        purchaseCanceled {
            Toast.makeText(
                context,
                "ان شاءالله دفعه ی بعدی کمک کنید 🙂",
                Toast.LENGTH_SHORT
            ).show()

        }
        purchaseFailed { throwable ->
            Toast.makeText(context, "خطایی رخ داد 😕 $throwable", Toast.LENGTH_SHORT)
                .show()
        }
    }
}

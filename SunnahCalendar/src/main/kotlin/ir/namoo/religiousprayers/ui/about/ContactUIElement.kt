package ir.namoo.religiousprayers.ui.about

import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import kotlinx.coroutines.launch

@Composable
fun ContactUIElement(
    typeface: Typeface,
    iconColor: Color,
    cardColor: Color,
    namooClick: () -> Unit,
    developerNClick: () -> Unit,
    mailTo: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column {
            ContactsButtons(
                cardColor = cardColor,
                iconColor = iconColor,
                namooClick = namooClick,
                developerNClick = developerNClick,
                mailTo = mailTo
            )
        }
    }
}

@Composable
fun ContactsButtons(
    cardColor: Color,
    iconColor: Color,
    namooClick: () -> Unit,
    developerNClick: () -> Unit,
    mailTo: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Row {
                ElevatedAssistChip(
                    onClick = { namooClick() },
                    label = {
                        Text(text = "@Namoo_IR", fontWeight = FontWeight.SemiBold)
                    },
                    trailingIcon = {
                        Icon(
                            painterResource(id = R.drawable.ic_tg),
                            contentDescription = "namoo_ir",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 2.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = cardColor,
                        trailingIconContentColor = iconColor
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                ElevatedAssistChip(
                    onClick = { developerNClick() },
                    label = {
                        Text(text = "@Developer_N", fontWeight = FontWeight.SemiBold)
                    },
                    trailingIcon = {
                        Icon(
                            painterResource(id = R.drawable.ic_tg),
                            contentDescription = "Developer_N",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 2.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = cardColor,
                        trailingIconContentColor = iconColor
                    )
                )
            }
            ElevatedAssistChip(
                onClick = { mailTo() },
                label = {
                    Text(text = "namoodev@gmail.com", fontWeight = FontWeight.SemiBold)
                },
                trailingIcon = {
                    Icon(
                        painterResource(id = R.drawable.ic_gmail),
                        contentDescription = "namoo_ir",
                        modifier = Modifier.size(24.dp)
                    )
                },
                elevation = AssistChipDefaults.elevatedAssistChipElevation(elevation = 2.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = cardColor,
                    trailingIconContentColor = iconColor
                )
            )
        }
    }
}


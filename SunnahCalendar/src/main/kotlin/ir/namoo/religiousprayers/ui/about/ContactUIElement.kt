package ir.namoo.religiousprayers.ui.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.iconColor

@Composable
fun ContactUIElement(
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
                namooClick = namooClick,
                developerNClick = developerNClick,
                mailTo = mailTo
            )
        }
    }
}

@Composable
fun ContactsButtons(
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


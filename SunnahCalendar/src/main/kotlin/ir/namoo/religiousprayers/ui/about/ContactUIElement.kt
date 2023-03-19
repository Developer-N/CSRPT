package ir.namoo.religiousprayers.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R

@Composable
fun ContactUIElement(
    normalTextColor: Color,
    iconColor: Color,
    cardColor: Color,
    namooClick: () -> Unit,
    developerNClick: () -> Unit,
    mailTo: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = CardDefaults.elevatedShape,
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column {
            ContactsButtons(
                cardColor = cardColor,
                iconColor = iconColor,
                textColor = normalTextColor,
                namooClick = namooClick,
                developerNClick = developerNClick,
                mailTo = mailTo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsButtons(
    cardColor: Color,
    iconColor: Color,
    textColor: Color,
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
                        labelColor = textColor,
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
                        labelColor = textColor,
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
                    labelColor = textColor,
                    trailingIconContentColor = iconColor
                )
            )
        }
    }
}


package ir.namoo.religiousprayers.ui.edit

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.AppDropdownMenu
import com.byagowi.persiancalendar.ui.common.AppDropdownMenuItem
import com.byagowi.persiancalendar.ui.common.NumberPicker
import com.byagowi.persiancalendar.ui.theme.AppTheme

@Composable
fun EditDateComponent(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.date),
    month: Int,
    day: Int,
    onMonthChanged: (Int) -> Unit,
    onDayChanged: (Int) -> Unit
) {
    val dayRange = if (month < 7) 1..31 else 1..30
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    ElevatedCard(modifier = modifier.padding(2.dp)) {
        Row(
            modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            NumberPicker(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                range = dayRange,
                value = day,
                onValueChange = { onDayChanged(it) },
                pendingConfirms = pendingConfirms
            )
            NumberPicker(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                range = 1..12,
                value = month,
                onValueChange = { onMonthChanged(it) },
                pendingConfirms = pendingConfirms
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun EditAthanComponent(
    modifier: Modifier = Modifier,
    title: String,
    editedTime: String?,
    originalTime: String?,
    onTimeChanged: (String) -> Unit
) {
    originalTime ?: return
    editedTime ?: return
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    var hour by remember { mutableIntStateOf(editedTime.split(":")[0].toInt()) }
    var minute by remember { mutableIntStateOf(editedTime.split(":")[1].toInt()) }
    var isEquals by remember { mutableStateOf(false) }

    SideEffect {
        isEquals = isEditTimesEquals(editedTime, originalTime)
    }

    val cardBackground by animateColorAsState(
        targetValue = if (isEquals) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer,
        label = "color",
        animationSpec = tween()
    )
    ElevatedCard(
        modifier, colors = CardDefaults.elevatedCardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1.5f),
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            NumberPicker(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                range = 1..59,
                value = minute,
                onValueChange = {
                    minute = it
                    onTimeChanged("$hour:$minute")
                },
                pendingConfirms = pendingConfirms
            )
            NumberPicker(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                range = 0..23,
                value = hour,
                onValueChange = {
                    hour = it
                    onTimeChanged("$hour:$minute")
                },
                pendingConfirms = pendingConfirms
            )
        }
    }
}

fun isEditTimesEquals(t1: String?, t2: String?): Boolean {
    t1 ?: return false
    t2 ?: return false
    val a = t1.split(":")
    val b = t2.split(":")
    return a[0].toInt() == b[0].toInt() && a[1].toInt() == b[1].toInt()
}

@Preview(locale = "fa")
@Composable
fun PreviewComponents() {
    AppTheme {
        Column {
            EditDateComponent(day = 14, month = 6, onDayChanged = {}, onMonthChanged = {})
            Row(modifier = Modifier.fillMaxWidth()) {
                EditAthanComponent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    title = stringResource(id = R.string.fajr),
                    editedTime = "6:10",
                    originalTime = "6:10",
                    onTimeChanged = {})
                EditAthanComponent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    title = stringResource(id = R.string.sunrise),
                    editedTime = "12:50",
                    originalTime = "12:51",
                    onTimeChanged = {})
            }
        }
    }
}

@Composable
fun GroupEditDialog(
    onDismiss: () -> Unit, onGroupEdit: (Int, Int, Int, Int, Int, Int, Boolean) -> Unit
) {
    val pendingConfirms = remember { mutableStateListOf<() -> Unit>() }
    val athans = listOf(
        stringResource(id = R.string.fajr),
        stringResource(id = R.string.sunrise),
        stringResource(id = R.string.dhuhr),
        stringResource(id = R.string.asr),
        stringResource(id = R.string.maghrib),
        stringResource(id = R.string.isha)
    )
    val types = listOf(
        stringResource(id = R.string.str_forward), stringResource(id = R.string.str_backward)
    )


    var selectedAthan by remember { mutableStateOf(athans.first()) }
    var showAthanSelector by remember { mutableStateOf(false) }

    var selectedFromDay by remember { mutableIntStateOf(1) }
    var selectedFromMonth by remember { mutableIntStateOf(1) }

    var selectedToDay by remember { mutableIntStateOf(1) }
    var selectedToMonth by remember { mutableIntStateOf(1) }

    var selectedMinute by remember { mutableIntStateOf(1) }

    var selectedEditType by remember { mutableIntStateOf(0) }
    val rotate by animateFloatAsState(
        targetValue = if (showAthanSelector) 180f else 0f, label = "rotate", animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        )
    )
    AlertDialog(onDismissRequest = { onDismiss() }, confirmButton = {
        TextButton(onClick = {
            onDismiss()
            onGroupEdit(
                athans.indexOf(selectedAthan),
                selectedFromMonth,
                selectedToMonth,
                selectedFromDay,
                selectedToDay,
                selectedMinute,
                selectedEditType == 0
            )
        }) {
            Text(text = stringResource(id = R.string.apply))
        }
    }, dismissButton = {
        TextButton(onClick = {
            onDismiss()
        }) {
            Text(text = stringResource(id = R.string.no))
        }
    }, icon = {
        Icon(
            imageVector = Icons.Default.EditNote,
            contentDescription = stringResource(id = R.string.group_edit)
        )
    }, title = { Text(text = stringResource(id = R.string.group_edit)) }, text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.select_athan_name),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                ElevatedAssistChip(onClick = { showAthanSelector = true }, label = {
                    Text(text = selectedAthan, fontWeight = FontWeight.SemiBold)
                    AppDropdownMenu(
                        expanded = showAthanSelector,
                        onDismissRequest = { showAthanSelector = false }) { closeMenu ->
                        athans.forEach { athan ->
                            AppDropdownMenuItem(text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    AnimatedVisibility(visible = selectedAthan == athan) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Check",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        modifier = Modifier.padding(4.dp),
                                        text = athan,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }, onClick = {
                                selectedAthan = athan
                                closeMenu()
                            })
                        }
                    }
                }, trailingIcon = {
                    Icon(
                        modifier = Modifier.rotate(rotate),
                        imageVector = Icons.Default.ExpandCircleDown,
                        contentDescription = "Expand"
                    )
                })
            }
            HorizontalDivider(modifier = Modifier.padding(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                EditDateComponent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    title = stringResource(id = R.string.str_from),
                    month = selectedFromMonth,
                    day = selectedFromDay,
                    onMonthChanged = { selectedFromMonth = it },
                    onDayChanged = { selectedFromDay = it })
                EditDateComponent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    title = stringResource(id = R.string.str_to),
                    month = selectedToMonth,
                    day = selectedToDay,
                    onMonthChanged = { selectedToMonth = it },
                    onDayChanged = { selectedToDay = it })
            }
            HorizontalDivider(modifier = Modifier.padding(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.str_minute),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                NumberPicker(
                    modifier = Modifier.weight(1f),
                    range = 1..30,
                    value = selectedMinute,
                    onValueChange = { selectedMinute = it },
                    pendingConfirms = pendingConfirms
                )
                Spacer(modifier = Modifier.weight(0.5f))
                Column(modifier = Modifier.weight(2f)) {
                    types.forEach { item ->
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraLarge)
                                .padding(8.dp)
                                .selectable(
                                    selected = types.indexOf(item) == selectedEditType, onClick = {
                                        selectedEditType = types.indexOf(item)
                                    }, role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            RadioButton(
                                selected = types.indexOf(item) == selectedEditType, onClick = null
                            )
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    })
}

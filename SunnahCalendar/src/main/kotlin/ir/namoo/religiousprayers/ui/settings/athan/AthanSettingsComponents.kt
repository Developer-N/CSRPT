package ir.namoo.religiousprayers.ui.settings.athan

import android.media.AudioManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.core.text.isDigitsOnly
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.NumberPicker
import com.byagowi.persiancalendar.ui.theme.AppTheme
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop

@Composable
fun SettingSwitch(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    title: String,
    onClick: () -> Unit,
    checkedIcon: ImageVector? = null,
    unCheckedIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .weight(3f)
                .padding(4.dp),
            text = title,
            fontWeight = FontWeight.SemiBold
        )
        Switch(
            modifier = Modifier.weight(1f),
            checked = isChecked,
            onCheckedChange = { onClick() },
            thumbContent = {
                if (checkedIcon != null) AnimatedVisibility(
                    visible = isChecked, enter = slideInHorizontally(), exit = shrinkHorizontally()
                ) {
                    Icon(
                        imageVector = checkedIcon,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (unCheckedIcon != null) AnimatedVisibility(visible = !isChecked) {
                    Icon(
                        imageVector = unCheckedIcon,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.scrim
                    )
                }
            })
    }
}

@Composable
fun PlayTypeComponent(
    playType: Int, onTypeChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val types = listOf(
            stringResource(id = R.string.full_screen),
            stringResource(id = R.string.notification),
            stringResource(id = R.string.just_notification)
        )

        types.forEach { item ->
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .padding(8.dp)
                    .selectable(
                        selected = types.indexOf(item) == playType, onClick = {
                            onTypeChange(types.indexOf(item))
                        }, role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(selected = types.indexOf(item) == playType, onClick = null)
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AlertComponent(
    title: String,
    togleTitle: String,
    isChecked: Boolean,
    onCheck: () -> Unit,
    range: IntRange,
    minute: Int,
    onMinute: (Int) -> Unit
) {
    Column {
        Text(
            modifier = Modifier
                .padding(vertical = 2.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SettingSwitch(
                modifier = Modifier.weight(2f),
                isChecked = isChecked,
                title = togleTitle,
                onClick = { onCheck() },
                checkedIcon = Icons.Default.AlarmOn,
                unCheckedIcon = Icons.Default.AlarmOff
            )
            NumberPicker(
                modifier = Modifier.weight(1f),
                range = range,
                value = minute,
                onValueChange = { onMinute(it) })

        }
    }
}

@Composable
fun VolumeComponent(enable: Boolean = true, volume: Int, updateVolume: (Int) -> Unit) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService<AudioManager>() } ?: return
    val originalAlarmVolume = remember { audioManager.getStreamVolume(AudioManager.STREAM_ALARM) }
    remember { audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0) }
    val maxValue = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) }
    DisposableEffect(Unit) {
        onDispose {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0)
        }
    }
    var innerVolume by remember { mutableIntStateOf(volume) }
    Slider(
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp),
        value = innerVolume.toFloat(),
        steps = maxValue,
        valueRange = 0f..maxValue.toFloat(),
        enabled = enable,
        onValueChange = { value ->
            innerVolume = value.toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, innerVolume, 0)
        },
        onValueChangeFinished = { updateVolume(innerVolume) })
}

@Composable
fun AthanSelector(
    title: String,
    items: List<String>,
    selectedItem: String,
    onSelectChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotate = animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "", animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium
        )
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
        ElevatedButton(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            onClick = { expanded = true }) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedContent(targetState = selectedItem) {
                    Text(text = it, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    modifier = Modifier.rotate(rotate.value),
                    imageVector = Icons.Default.ExpandCircleDown,
                    contentDescription = ""
                )
            }
            DropdownMenu(
                shape = MaterialTheme.shapes.extraLarge,
                expanded = expanded,
                onDismissRequest = { expanded = false }) {
                items.forEachIndexed { index, item ->
                    val name = if (item.split(".").first()
                            .isDigitsOnly()
                    ) stringResource(R.string.added_file, item) else item
                    DropdownMenuItem(text = {
                        Text(
                            text = name,
                            fontWeight = if (item == selectedItem) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }, onClick = {
                        onSelectChanged(item)
                        expanded = false
                    }, trailingIcon = {
                        if (item == selectedItem) Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    })
                }
            }
        }
    }
}

@Preview(locale = "fa")
@Composable
fun PrevAthanSelector() {
    AppTheme {
        Surface {
            AthanSelector(
                title = stringResource(id = R.string.str_selectAthan),
                items = listOf("athan1", "athan2", "athan3", "athan4", "athan5", "athan6"),
                selectedItem = "athan3",
                onSelectChanged = {})
        }
    }
}

@Preview(locale = "fa")
@Composable
fun ComponentsPrev() {
    AppTheme {
        Scaffold { paddingValues ->
            Surface(
                shape = materialCornerExtraLargeTop(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SettingSwitch(
                        isChecked = true,
                        title = stringResource(id = R.string.enable_athan),
                        onClick = { },
                        checkedIcon = Icons.AutoMirrored.Default.VolumeUp,
                        unCheckedIcon = Icons.AutoMirrored.Default.VolumeOff
                    )
                    HorizontalDivider()
                    PlayTypeComponent(playType = 1, onTypeChange = {})
                    HorizontalDivider()
                    SettingSwitch(
                        modifier = Modifier.fillMaxWidth(),
                        isChecked = true,
                        title = stringResource(id = R.string.broadcast_prayer_after_adhan),
                        onClick = { },
                        checkedIcon = Icons.AutoMirrored.Default.VolumeUp,
                        unCheckedIcon = Icons.AutoMirrored.Default.VolumeOff
                    )
                    HorizontalDivider()
                    AlertComponent(
                        title = stringResource(id = R.string.alarm_before),
                        togleTitle = stringResource(id = R.string.enable_alarm),
                        isChecked = true,
                        onCheck = {},
                        range = 10..60,
                        minute = 15,
                        onMinute = {})
                    HorizontalDivider()
                    AlertComponent(
                        title = stringResource(id = R.string.alarm_after),
                        togleTitle = stringResource(id = R.string.enable_alarm),
                        isChecked = true,
                        onCheck = {},
                        range = 10..60,
                        minute = 15,
                        onMinute = {})
                    HorizontalDivider()
                    AlertComponent(
                        title = stringResource(id = R.string.silent_after),
                        togleTitle = stringResource(id = R.string.enable_silent),
                        isChecked = true,
                        onCheck = {},
                        range = 10..60,
                        minute = 15,
                        onMinute = {})
                    HorizontalDivider()
                    SettingSwitch(
                        isChecked = true,
                        title = stringResource(id = R.string.ascending_athan_volume),
                        onClick = { },
                        checkedIcon = Icons.Default.CheckCircleOutline,
                        unCheckedIcon = Icons.Default.Close
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.enable_ascending_athan_volume),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    VolumeComponent(volume = 3, updateVolume = {})
                    HorizontalDivider()
                }
            }
        }
    }
}

package ir.namoo.religiousprayers.ui.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.update
import ir.namoo.commons.DEFAULT_FULL_SCREEN_METHOD
import ir.namoo.commons.DEFAULT_NOTIFICATION_METHOD
import ir.namoo.commons.PREF_FULL_SCREEN_METHOD
import ir.namoo.commons.PREF_NOTIFICATION_METHOD
import ir.namoo.commons.utils.appPrefsLite
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MyLocationSelector(
    modifier: Modifier = Modifier,
    locationList: List<String>,
    selectedLocation: String,
    onSelectedLocationChange: (String) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val rotate = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    ElevatedAssistChip(modifier = modifier.padding(4.dp), onClick = {
        expanded.value = !expanded.value
        coroutineScope.launch {
            rotate.animateTo(
                if (expanded.value) 180f else 0f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }, label = { Text(text = selectedLocation) }, leadingIcon = {
        Icon(
            modifier = Modifier.rotate(rotate.value),
            imageVector = Icons.Filled.ExpandCircleDown,
            contentDescription = "DropDown"
        )
        DropdownMenu(expanded = expanded.value, onDismissRequest = {
            expanded.value = false
            coroutineScope.launch {
                rotate.animateTo(
                    if (expanded.value) 180f else 0f, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }) {
            locationList.forEach { location ->
                DropdownMenuItem(text = { Text(text = location) }, onClick = {
                    onSelectedLocationChange(location)
                    expanded.value = false
                    coroutineScope.launch {
                        rotate.animateTo(
                            if (expanded.value) 180f else 0f, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                }, trailingIcon = {
                    if (selectedLocation == location) Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Check",
                        tint = MaterialTheme.colorScheme.primary
                    )
                })
            }
        }
    })
}

@Composable
fun SettingsAthanSwitch(
    title: String,
    summary: String? = null,
    isActive: Boolean,
    onBoxClick: () -> Unit,
    onSwitchClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = { onBoxClick() })
            .padding(horizontal = 8.dp),
    ) {
        Column(
            Modifier
                .align(alignment = Alignment.CenterStart)
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = (16 + 68).dp)
        ) {
            Text(
                title, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (summary != null) Text(
                summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(AppBlendAlpha)
            )
        }
        Switch(
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .padding(end = 16.dp),
            checked = isActive,
            thumbContent = {
                AnimatedVisibility(
                    visible = isActive,
                    enter = slideInHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.VolumeUp,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                AnimatedVisibility(
                    visible = !isActive,
                    enter = slideInHorizontally(),
                    exit = shrinkHorizontally()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.VolumeOff,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.scrim
                    )
                }
            },
            onCheckedChange = { onSwitchClick() },
        )
    }
}

@Composable
fun MethodsModeToggle(context: Context, type: Int) {//1:Notification 2:FullScreen
    val prefs = context.appPrefsLite
    val strPREF = if (type == 1) PREF_NOTIFICATION_METHOD else PREF_FULL_SCREEN_METHOD
    val defPref = if (type == 1) DEFAULT_NOTIFICATION_METHOD else DEFAULT_FULL_SCREEN_METHOD
    val options = listOf(
        stringResource(id = R.string.first_method), stringResource(id = R.string.second_method)
    )
    val (selectedOption, onOptionSelected) = remember {
        mutableStateOf(options[(prefs.getInt(strPREF, defPref) - 1)])
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 24.dp)
            .selectableGroup()
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .selectable(
                        selected = option == selectedOption, onClick = {
                            onOptionSelected(option)
                            prefs.edit {
                                putInt(strPREF, options.indexOf(option) + 1)
                            }
                            update(context, true)
                        }, role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(selected = option == selectedOption, onClick = null)
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

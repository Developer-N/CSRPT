package ir.namoo.religiousprayers.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.utils.AppBlendAlpha

@Composable
fun MyLocationSelector(
    modifier: Modifier = Modifier,
    locationList: List<String>,
    selectedLocation: String,
    onSelectedLocationChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotate by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotate",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    ElevatedButton(modifier = modifier.padding(4.dp),
        onClick = { expanded = !expanded }) {
        Text(
            text = selectedLocation,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Icon(
            modifier = Modifier.rotate(rotate),
            imageVector = Icons.Filled.ExpandCircleDown,
            contentDescription = "DropDown"
        )
        DropdownMenu(expanded = expanded,
            shape = MaterialTheme.shapes.extraLarge,
            onDismissRequest = { expanded = false }) {
            locationList.forEach { location ->
                DropdownMenuItem(text = { Text(text = location) }, onClick = {
                    onSelectedLocationChange(location)
                    expanded = false
                }, trailingIcon = {
                    if (selectedLocation == location) Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Check",
                        tint = MaterialTheme.colorScheme.primary
                    )
                })
            }
        }
    }
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

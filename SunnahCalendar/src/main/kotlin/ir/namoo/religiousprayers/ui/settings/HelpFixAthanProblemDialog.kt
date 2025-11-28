package ir.namoo.religiousprayers.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LiveHelp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.ScrollShadow
import com.byagowi.persiancalendar.utils.logException
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@SuppressLint("BatteryLife")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HelpFixAthanProblemDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val phoneStatePermission =
        rememberPermissionState(permission = Manifest.permission.READ_PHONE_STATE)


    var isPhoneStateGranted by remember { mutableStateOf(false) }
    var isShowOverlayGranted by remember { mutableStateOf(false) }
    var isPostNotificationGranted by remember { mutableStateOf(false) }
    var isIgnoringBatteryOptimizations by remember { mutableStateOf(false) }
    var showAlarmManagerDialog by remember { mutableStateOf(false) }

    val postNotificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPostNotificationGranted = isGranted
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {}
                Lifecycle.Event.ON_START -> {}
                Lifecycle.Event.ON_RESUME -> {
                    isPhoneStateGranted = phoneStatePermission.status.isGranted

                    isShowOverlayGranted = Settings.canDrawOverlays(context)

                    isPostNotificationGranted =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        else true

                    isIgnoringBatteryOptimizations = runCatching {
                        context.getSystemService<PowerManager>()
                            ?.isIgnoringBatteryOptimizations(context.applicationContext.packageName)
                    }.onFailure(logException).getOrNull() == true

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService<AlarmManager>()
                        alarmManager?.let {
                            showAlarmManagerDialog = !it.canScheduleExactAlarms()
                        }
                    } else showAlarmManagerDialog = false
                }

                Lifecycle.Event.ON_PAUSE -> {}
                Lifecycle.Event.ON_STOP -> {}
                Lifecycle.Event.ON_DESTROY -> {}
                Lifecycle.Event.ON_ANY -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = onDismiss) {
            Text(
                text = stringResource(id = R.string.close),
                fontWeight = FontWeight.SemiBold
            )
        }
    }, icon = {
        Icon(
            imageVector = Icons.AutoMirrored.Default.LiveHelp, contentDescription = "Help"
        )
    }, text = {
        val scrollState = rememberScrollState()
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(state = scrollState)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    text = stringResource(id = R.string.help_fix_athan_problems_msg),
                    style = TextStyle.Default.copy(hyphens = Hyphens.Auto),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                FixAthanItem(
                    icon = Icons.Default.BatteryStd,
                    title = stringResource(id = R.string.battery_optimization_title),
                    subtitle = stringResource(id = R.string.battery_optimization_msg),
                    isOk = isIgnoringBatteryOptimizations,
                    onClick = {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                data = "package:${context.packageName}".toUri()
                            })
                    })

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) FixAthanItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(id = R.string.post_notification_permission_title),
                    subtitle = stringResource(id = R.string.post_notification_permission_msg),
                    isOk = isPostNotificationGranted,
                    onClick = {
                        postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    })
                FixAthanItem(
                    icon = Icons.Default.Layers,
                    title = stringResource(id = R.string.show_overly_permission_title),
                    subtitle = stringResource(id = R.string.show_overly_permission_msg),
                    isOk = isShowOverlayGranted,
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            })

                    })
                FixAthanItem(
                    icon = Icons.Default.Phone,
                    title = stringResource(id = R.string.phone_state_permission_title),
                    subtitle = stringResource(id = R.string.phone_state_permission_msg),
                    isOk = isPhoneStateGranted,
                    onClick = { phoneStatePermission.launchPermissionRequest() })
                FixAthanItem(
                    icon = Icons.Default.Alarm,
                    title = stringResource(id = R.string.alarm_reminders),
                    subtitle = stringResource(id = R.string.schedule_permission_message),
                    isOk = !showAlarmManagerDialog,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            })
                    })

                OutlinedCard(modifier = Modifier.padding(4.dp), onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            ("package:" + context.applicationContext.packageName).toUri()
                        )
                    )
                }) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = stringResource(id = R.string.other_checks),
                        style = TextStyle.Default.copy(hyphens = Hyphens.Auto)
                    )
                }

            }//end of column
            ScrollShadow(scrollState = scrollState)
        }
    })//end of AlertDialog
}

@Composable
fun FixAthanItem(
    icon: ImageVector, title: String, subtitle: String, isOk: Boolean, onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotate = animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "Rotation", animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        )
    )
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .clickable(onClick = onClick)
            .border(
                border = if (isOk) BorderStroke(
                    width = 1.dp, color = MaterialTheme.colorScheme.primary
                )
                else BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.error),
                shape = MaterialTheme.shapes.large
            )
            .background(
                color = if (isOk) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.large
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (!isOk) {
                Icon(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .align(Alignment.CenterEnd),
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Icon(
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.Center),
                imageVector = icon,
                contentDescription = title,
                tint = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Text(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            text = title,
            fontWeight = FontWeight.SemiBold
        )
        TextButton(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = stringResource(id = R.string.more), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.padding(4.dp))
            Icon(
                modifier = Modifier.rotate(rotate.value),
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand"
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                text = subtitle,
                textAlign = TextAlign.Justify
            )
        }
    }
}

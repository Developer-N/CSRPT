package ir.namoo.religiousprayers.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LiveHelp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.AppTheme
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

                    isShowOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        Settings.canDrawOverlays(context)
                    else true

                    isPostNotificationGranted =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        else true

                    isIgnoringBatteryOptimizations = runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            context.getSystemService<PowerManager>()
                                ?.isIgnoringBatteryOptimizations(
                                    context.applicationContext.packageName
                                )
                        } else true
                    }.onFailure(logException).getOrNull() == true
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

    AlertDialog(onDismissRequest = { onDismiss() }, confirmButton = {
        TextButton(onClick = { onDismiss() }) {
            Text(text = stringResource(id = R.string.close))
        }
    }, icon = {
        Icon(
            imageVector = Icons.AutoMirrored.Default.LiveHelp, contentDescription = "Help"
        )
    }, text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                text = stringResource(id = R.string.help_fix_athan_problems_msg),
                style = TextStyle.Default.copy(hyphens = Hyphens.Auto),
                fontWeight = FontWeight.SemiBold
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FixAthanItem(
                    title = stringResource(id = R.string.battery_optimization_title),
                    subtitle = stringResource(id = R.string.battery_optimization_msg),
                    isOk = isIgnoringBatteryOptimizations,
                    onClick = {
                        context.startActivity(
                            Intent().apply {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                FixAthanItem(
                    title = stringResource(id = R.string.post_notification_permission_title),
                    subtitle = stringResource(id = R.string.post_notification_permission_msg),
                    isOk = isPostNotificationGranted,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            FixAthanItem(
                title = stringResource(id = R.string.show_overly_permission_title),
                subtitle = stringResource(id = R.string.show_overly_permission_msg),
                isOk = isShowOverlayGranted,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + context.applicationContext.packageName)
                            )
                        )
                    }
                }
            )
            FixAthanItem(
                title = stringResource(id = R.string.phone_state_permission_title),
                subtitle = stringResource(id = R.string.phone_state_permission_msg),
                isOk = isPhoneStateGranted,
                onClick = { phoneStatePermission.launchPermissionRequest() }
            )
            OutlinedCard(modifier = Modifier.padding(4.dp), onClick = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + context.applicationContext.packageName)
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
    })//end of AlertDialog
}

@Composable
fun FixAthanItem(title: String, subtitle: String, isOk: Boolean, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val rotate = animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "Rotation",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    OutlinedCard(modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth()
        .animateContentSize(animationSpec = spring()),
        onClick = { onClick() }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(modifier = Modifier.padding(4.dp), text = title, fontWeight = FontWeight.SemiBold)
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = "isOk",
                tint = if (isOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ElevatedButton(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                onClick = { expanded = !expanded }) {
                Text(text = stringResource(id = R.string.more))
                Spacer(modifier = Modifier.padding(4.dp))
                Icon(
                    modifier = Modifier.rotate(rotate.value),
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand"
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                text = subtitle,
                style = TextStyle.Default.copy(hyphens = Hyphens.Auto)
            )
        }
    }
}

@Preview(name = "light", locale = "fa", wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
@Preview(
    name = "night",
    locale = "fa",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
@Composable
private fun PreviewFixAthanItem() {
    AppTheme {
        FixAthanItem(
            title = stringResource(id = R.string.show_overly_permission_title),
            subtitle = stringResource(id = R.string.show_overly_permission_msg),
            isOk = true
        ) {

        }
    }
}

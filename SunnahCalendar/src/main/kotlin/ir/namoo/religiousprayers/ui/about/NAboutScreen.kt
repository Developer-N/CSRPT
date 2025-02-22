package ir.namoo.religiousprayers.ui.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.common.NavigationOpenDrawerIcon
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import ir.namoo.commons.APP_LINK

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.NAboutScreen(
    navigateToPersianCalendar: () -> Unit,
    openDrawer: () -> Unit,
    animatedContentScope: AnimatedContentScope
) {
    val context = LocalContext.current
    val version = programVersion(context.packageManager, context.packageName)
    val versionDescription = formatNumber(
        "${context.getString(R.string.app_name)}: ${
            context.getString(R.string.version, version)
        }\nنسخه سال 1403-1404"
    )
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                colors = appTopAppBarColors(),
                navigationIcon = { NavigationOpenDrawerIcon(animatedContentScope, openDrawer) },
                actions = {
                    IconButton(onClick = { navigateToPersianCalendar() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.calendar)
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            "نرم افزار تقویم + اوقات شرعی اهل سنت را از لینک زیر دانلود کنید. \n$APP_LINK"
                        )
                        context.startActivity(
                            Intent.createChooser(
                                intent, context.getString(R.string.share)
                            )
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share)
                        )
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = stringResource(R.string.str_changes_title)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
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
                    .verticalScroll(rememberScrollState())
            ) {
                InfoUIElement(versionDescription)
                HorizontalDivider(modifier = Modifier.padding(8.dp))
                ContactUIElement(
                    namooClick = { openTG(context) },
                    developerNClick = { openTGDeveloper(context) }
                )
            }

            if (showDialog) AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(text = stringResource(R.string.ok))
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.str_changes_title))
                },
                text = {
                    val textScrollState = rememberScrollState()
                    Text(
                        modifier = Modifier.verticalScroll(textScrollState),
                        text = formatNumber(stringResource(id = R.string.changes))
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = stringResource(
                            id = R.string.str_changes_title
                        )
                    )
                })
        }
    }
}

private fun programVersion(packageManager: PackageManager, packageName: String): String? =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName, PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }.versionName
    }.onFailure(logException).getOrDefault("")

private fun openTG(context: Context) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=namoo_ir"))
        context.startActivity(intent)
    }.onFailure(logException).getOrElse {
        Toast.makeText(
            context, context.getString(R.string.telegram_not_installed), Toast.LENGTH_SHORT
        ).show()
    }
}

private fun openTGDeveloper(context: Context) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=developer_n"))
        context.startActivity(intent)
    }.onFailure(logException).getOrElse {
        Toast.makeText(
            context, context.getString(R.string.telegram_not_installed), Toast.LENGTH_SHORT
        ).show()
    }
}

package com.byagowi.persiancalendar.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.ui.utils.SettingsHorizontalPaddingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseAppDialog(
    title: (@Composable () -> Unit)?,
    onDismissRequest: () -> Unit,
    neutralButton: (@Composable () -> Unit)?,
    confirmButton: (@Composable () -> Unit)?,
    dismissButton: (@Composable () -> Unit)?,
    content: @Composable BoxScope.() -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        DialogSurface {
            Column {
                title?.also { title ->
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.headlineSmall
                    ) {
                        Box(
                            Modifier.padding(
                                top = SettingsHorizontalPaddingItem.dp,
                                start = SettingsHorizontalPaddingItem.dp,
                                bottom = 16.dp,
                                end = SettingsHorizontalPaddingItem.dp,
                            )
                        ) { title() }
                    }
                }

                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(weight = 1f, fill = false)
                    ) { content() }
                }

                if (neutralButton != null || dismissButton != null || confirmButton != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(Modifier.padding(bottom = 16.dp, start = 24.dp, end = 24.dp)) {
                        neutralButton?.invoke()
                        Spacer(Modifier.weight(1f))
                        dismissButton?.invoke()
                        if (dismissButton != null && confirmButton != null)
                            Spacer(Modifier.width(8.dp))
                        confirmButton?.invoke()
                    }
                }
            }
        }
    }
}

@Composable
fun AppDialog(
    title: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    neutralButton: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    BaseAppDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        neutralButton = neutralButton,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            content = content
        )
        ScrollShadow(scrollState, top = true)
        ScrollShadow(scrollState, top = false)
    }
}

@Composable
fun AppDialogWithLazyColumn(
    title: (@Composable () -> Unit)? = null,
    onDismissRequest: () -> Unit,
    neutralButton: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    BaseAppDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        neutralButton = neutralButton,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
    ) {
        val lazyState = rememberLazyListState()
        LazyColumn(state = lazyState, content = content)
        ScrollShadow(lazyState, top = true)
        ScrollShadow(lazyState, top = false)
    }
}

package com.byagowi.persiancalendar.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appFabElevation

@Composable
fun StopButton(isStopped: Boolean, setStop: (Boolean) -> Unit) {
    FloatingActionButton(onClick = { setStop(!isStopped) }, elevation = appFabElevation()) {
        Icon(
            if (isStopped) Icons.Default.PlayArrow else Icons.Default.Stop,
            contentDescription = stringResource(
                if (isStopped) R.string.resume else R.string.stop
            )
        )
    }
}

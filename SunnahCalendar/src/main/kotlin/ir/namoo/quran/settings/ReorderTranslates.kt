package ir.namoo.quran.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderTranslates(
    onDismiss: () -> Unit, viewModel: ReorderTranslatesViewModel = koinViewModel()
) {
    viewModel.loadData()

    val settings by viewModel.settings.collectAsState()

    AlertDialog(onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = R.string.close))
            }
        },
        icon = { Icon(imageVector = Icons.AutoMirrored.Default.Sort, contentDescription = "") },
        text = {
            LazyColumn {
                if (settings.isNotEmpty()) items(items = settings, key = { it.id }) { setting ->
                    ElevatedCard(
                        modifier = Modifier
                            .padding(4.dp)
                            .animateItemPlacement()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(4f),
                                text = formatNumber("${setting.priority}: ${setting.name}"),
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (setting.priority > 1) 1f else 0.5f),
                                onClick = { viewModel.moveUp(setting) },
                                enabled = setting.priority > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Up",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (setting.priority < 12) 1f else 0.5f),
                                onClick = { viewModel.moveDown(setting) },
                                enabled = setting.priority < 12
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Down",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        })
}

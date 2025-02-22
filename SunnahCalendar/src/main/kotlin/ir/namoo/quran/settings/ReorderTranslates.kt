package ir.namoo.quran.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.formatNumber
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReorderTranslates(
    onDismiss: () -> Unit, viewModel: ReorderTranslatesViewModel = koinViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.loadData()
    }

    val settings = viewModel.settings

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
                            .animateItem(
                                fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                    visibilityThreshold = IntOffset.VisibilityThreshold
                                )
                            ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                                    .weight(4f),
                                text = formatNumber("${setting.priority}: ${setting.name}"),
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.moveUp(setting) },
                                enabled = setting.priority > 1,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Up"
                                )
                            }
                            IconButton(
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.moveDown(setting) },
                                enabled = setting.priority < 12,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Down"
                                )
                            }
                        }
                    }
                }
            }
        })
}

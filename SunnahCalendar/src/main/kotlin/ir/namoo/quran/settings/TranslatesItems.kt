package ir.namoo.quran.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranslateItems(viewModel: SettingViewModel = koinViewModel()) {
    val translates = viewModel.translates
    val fullFarsi by viewModel.isFullFarsiEnabled.collectAsState()
    val isKurdishEnabled by viewModel.isKurdishEnabled.collectAsState()
    var showReorderDialog by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(4f),
                    text = stringResource(id = R.string.select_translates), fontSize = 16.sp
                )
                IconButton(modifier = Modifier.weight(1f), onClick = { showReorderDialog = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            //Farsi Translate
            translates.find { it.id == 1 }?.let {
                Row {
                    MySwitchBox(
                        checked = it.isActive,
                        label = it.name,
                        onCheckChanged = { isActive ->
                            viewModel.updateSetting(it, isActive)
                        })
                    AnimatedVisibility(
                        visible = it.isActive,
                        enter = expandHorizontally(),
                        exit = shrinkHorizontally()
                    ) {
                        MySwitchBox(checked = fullFarsi,
                            label = stringResource(id = R.string.farsi_tafsir),
                            onCheckChanged = { active -> viewModel.updateFullFarsi(active) })
                    }
                }
            }
            //English translate
            translates.find { it.id == 2 }?.let {
                MySwitchBox(checked = it.isActive, label = it.name, onCheckChanged = { isActive ->
                    viewModel.updateSetting(it, isActive)
                })
            }
            //Kurdish translate

            MySwitchBox(checked = isKurdishEnabled,
                label = stringResource(id = R.string.kurdish_translate),
                onCheckChanged = { viewModel.updateKurdishEnable(it) })
            AnimatedVisibility(visible = isKurdishEnabled) {
                FlowRow(
                    modifier = Modifier
                        .padding(16.dp, 2.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.Center
                ) {
                    translates.filter { it.id > 2 }.forEach { item ->
                        MySwitchBox(
                            checked = item.isActive,
                            label = item.name,
                            onCheckChanged = { isActive ->
                                viewModel.updateSetting(item, isActive)
                            })
                    }
                }
            }
            AnimatedVisibility(visible = showReorderDialog) {
                ReorderTranslates(onDismiss = { showReorderDialog = false })
            }
        }
    }
}

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranslateItems(viewModel: SettingViewModel = koinViewModel()) {
    val translates by viewModel.translates.collectAsState()
    val fullFarsi by viewModel.isFullFarsiEnabled.collectAsState()
    val isKurdishEnabled by viewModel.isKurdishEnabled.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.select_translates),
                fontFamily = FontFamily(appFont),
                fontSize = 16.sp
            )

            //Farsi Translate
            translates.find { it.id == 1 }?.let {
                Row {
                    MySwitchBox(checked = it.isActive, label = it.name, onCheckChanged = { active ->
                        it.isActive = active
                        viewModel.updateSetting(it)
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
                MySwitchBox(checked = it.isActive, label = it.name, onCheckChanged = { active ->
                    it.isActive = active
                    viewModel.updateSetting(it)
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
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalArrangement = Arrangement.Center
                ) {
                    translates.filter { it.id > 2 }.forEach { item ->
                        MySwitchBox(checked = item.isActive,
                            label = item.name,
                            onCheckChanged = {
                                viewModel.updateSetting(item.apply {
                                    isActive = it
                                })
                            })
                    }
                }
            }
        }
    }
}

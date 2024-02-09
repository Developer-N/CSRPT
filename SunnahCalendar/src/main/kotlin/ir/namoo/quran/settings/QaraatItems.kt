package ir.namoo.quran.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import ir.namoo.quran.qari.QariEntity
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun QaraatItems(viewModel: SettingViewModel = koinViewModel()) {
    viewModel.loadPaths(LocalContext.current)
    val playType by viewModel.playType.collectAsState()
    val rootPaths by viewModel.rootPaths.collectAsState()
    val selectedPath by viewModel.selectedPath.collectAsState()
    val playNextSura by viewModel.playNextSura.collectAsState()

    val playTypeList = listOf(
        stringResource(id = R.string.arabic_translate),
        stringResource(id = R.string.just_arabic),
        stringResource(id = R.string.just_translate)
    )
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
    ) {
        Text(
            modifier = Modifier.padding(4.dp, 2.dp),
            text = stringResource(id = R.string.select_qari_translate_for_play)
        )
        SelectQariAndTranslateToPlay(modifier = Modifier, viewModel)
        HorizontalDivider(modifier = Modifier.padding(16.dp, 0.dp))
        MyBtnGroup(title = stringResource(id = R.string.play_type),
            items = playTypeList,
            checkedItem = playTypeList[playType - 1],
            onCheckChanged = { viewModel.updatePlayType(playTypeList.indexOf(it) + 1) })
        HorizontalDivider(modifier = Modifier.padding(16.dp, 0.dp))
        MySwitchBox(modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            checked = playNextSura,
            label = stringResource(id = R.string.play_next_sura),
            onCheckChanged = { play -> viewModel.updatePlayNextSura(play) })
        HorizontalDivider(modifier = Modifier.padding(16.dp, 0.dp))
        MyBtnGroup(
            title = stringResource(id = R.string.select_storage),
            items = rootPaths,
            checkedItem = selectedPath,
            onCheckChanged = { viewModel.updateSelectedPath(rootPaths[rootPaths.indexOf(it)]) },
            isPathSetting = true
        )
    }
}


@Composable
fun SelectQariAndTranslateToPlay(
    modifier: Modifier = Modifier, viewModel: SettingViewModel = koinViewModel()
) {
    val qariList by viewModel.qariList.collectAsState()
    val translateList by viewModel.translatePlayList.collectAsState()
    val selectedQari by viewModel.selectedQari.collectAsState()
    val selectedTranslate by viewModel.selectedTranslateToPlay.collectAsState()
    val isQariLoading by viewModel.isQariLoading.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QariSelector(modifier = Modifier
            .padding(4.dp, 2.dp)
            .weight(1f),
            title = qariList.find { it.folderName == selectedQari }?.name
                ?: stringResource(id = R.string.select_qari),
            enable = !isQariLoading,
            list = qariList,
            selected = selectedQari,
            onSelectChange = { viewModel.updateSelectedQari(it) })

        QariSelector(modifier = Modifier
            .padding(4.dp, 2.dp)
            .weight(1f),
            title = translateList.find { it.folderName == selectedTranslate }?.name
                ?: stringResource(id = R.string.select_translate_to_play),
            enable = !isQariLoading,
            list = translateList,
            selected = selectedTranslate,
            onSelectChange = { viewModel.updateSelectedTranslateToPlay(it) })
    }
}

@Composable
fun QariSelector(
    modifier: Modifier = Modifier,
    title: String,
    enable: Boolean,
    list: List<QariEntity>,
    selected: String,
    onSelectChange: (String) -> Unit
) {

    val expanded = remember { mutableStateOf(false) }
    val rotate = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(modifier = modifier.clickable {
        expanded.value = !expanded.value
        coroutineScope.launch {
            rotate.animateTo(
                if (expanded.value) 180f else 0f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier.rotate(rotate.value),
                imageVector = Icons.Filled.ExpandCircleDown,
                contentDescription = "DropDown",
                tint = MaterialTheme.colorScheme.primary
            )
            DropdownMenu(expanded = expanded.value, onDismissRequest = {
                expanded.value = false
                coroutineScope.launch {
                    rotate.animateTo(
                        if (expanded.value) 180f else 0f, animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }) {
                list.forEach { qari ->
                    DropdownMenuItem(text = {
                        Text(text = qari.name)
                    }, onClick = {
                        onSelectChange(qari.folderName)
                        expanded.value = false
                        coroutineScope.launch {
                            rotate.animateTo(
                                if (expanded.value) 180f else 0f, animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }, leadingIcon = {
                        if (qari.photoLink.isNullOrBlank()) Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = qari.name,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        else GlideImage(modifier = Modifier
                            .size(32.dp)
                            .clip(MaterialTheme.shapes.large),
                            imageModel = { qari.photoLink.trim() },
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Fit, alignment = Alignment.Center
                            ),
                            component = rememberImageComponent {
                                +ShimmerPlugin(
                                    baseColor = MaterialTheme.colorScheme.surface,
                                    highlightColor = MaterialTheme.colorScheme.primary
                                )
                            },
                            failure = {
                                Icon(
                                    imageVector = Icons.Filled.Image, contentDescription = qari.name
                                )
                            })
                    }, trailingIcon = {
                        if (qari.folderName == selected) Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Check",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    })
                }
            }

            Text(text = title)

            AnimatedVisibility(
                visible = !enable, enter = expandHorizontally(), exit = shrinkHorizontally()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), strokeCap = StrokeCap.Round
                )
            }
            AnimatedVisibility(
                visible = enable, enter = expandHorizontally(), exit = shrinkHorizontally()
            ) {
                val qari = list.find { it.folderName == selected }
                if (qari == null || qari.photoLink.isNullOrBlank()) Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
                else GlideImage(modifier = Modifier
                    .size(34.dp)
                    .clip(MaterialTheme.shapes.large),
                    imageModel = { qari.photoLink.trim() },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Fit, alignment = Alignment.Center
                    ),
                    component = rememberImageComponent {
                        +ShimmerPlugin(
                            baseColor = MaterialTheme.colorScheme.surface,
                            highlightColor = MaterialTheme.colorScheme.primary
                        )
                    },
                    failure = {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = qari.name,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    })
            }
        }
    }
}


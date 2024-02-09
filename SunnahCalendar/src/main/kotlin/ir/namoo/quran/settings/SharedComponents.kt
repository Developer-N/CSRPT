package ir.namoo.quran.settings

import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import kotlinx.coroutines.launch

@Composable
fun MySwitchBox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    label: String,
    onCheckChanged: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .padding(vertical = 2.dp, horizontal = 4.dp)
            .clickable {
                onCheckChanged(!checked)
            }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 0.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = label,
                fontSize = 16.sp
            )
            Switch(checked = checked, onCheckedChange = {
                onCheckChanged(it)
            }, thumbContent = {
                AnimatedVisibility(
                    visible = checked, enter = expandVertically(), exit = shrinkVertically()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Check",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            })
        }
    }
}

@Composable
fun MySlider(modifier: Modifier = Modifier, value: Float, onValueChanged: (Float) -> Unit) {
    ElevatedCard(modifier = modifier.clip(MaterialTheme.shapes.extraLarge)) {
        Slider(
            modifier = modifier,
            value = value,
            onValueChange = { onValueChanged(it) },
            valueRange = 10f..52f,
            steps = 40
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyBtnGroup(
    modifier: Modifier = Modifier,
    title: String,
    items: List<String>,
    checkedItem: String,
    onCheckChanged: (String) -> Unit,
    isPathSetting: Boolean = false
) {
    ElevatedCard(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .fillMaxWidth()
    ) {
        if (title.isNotEmpty())
            Text(
                modifier = Modifier.padding(16.dp, 4.dp),
                text = title,
                fontSize = 16.sp
            )
        FlowRow(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.Center
        ) {
            items.forEach {
                ElevatedAssistChip(
                    modifier = Modifier
                        .padding(2.dp)
                        .animateContentSize(animationSpec = spring()),
                    label = {
                        Text(
                            text = if (isPathSetting) {
                                if (it.contains("emulated")) stringResource(id = R.string.internal)
                                else stringResource(id = R.string.external)
                            } else it
                        )
                    },
                    onClick = { onCheckChanged(it) },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = it == checkedItem,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = "Check")
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = AssistChipDefaults.elevatedAssistChipColors()
                )
            }
        }
    }
}

@Composable
fun MyFontSelector(
    modifier: Modifier = Modifier,
    title: String,
    preview: String,
    fontList: List<String>,
    fontNames: List<String>,
    selectedFont: String,
    fontSize: Float,
    onFontFamilyChanged: (String) -> Unit,
    onFontSizeChanged: (Float) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val rotate = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .padding(16.dp, 4.dp)
                    .weight(2f),
                text = title,
                fontSize = 16.sp
            )
            ElevatedAssistChip(modifier = Modifier
                .padding(4.dp, 2.dp)
                .weight(1f), onClick = {
                expanded.value = !expanded.value
                coroutineScope.launch {
                    rotate.animateTo(
                        if (expanded.value) 180f else 0f, animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }, label = {
                Text(text = stringResource(id = R.string.select_font))
            }, leadingIcon = {
                Icon(
                    modifier = Modifier.rotate(rotate.value),
                    imageVector = Icons.Filled.ExpandCircleDown,
                    contentDescription = "DropDown"
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
                    fontList.forEach { font ->
                        DropdownMenuItem(text = { Text(text = fontNames[fontList.indexOf(font)]) },
                            onClick = {
                                onFontFamilyChanged(font)
                                expanded.value = false
                                coroutineScope.launch {
                                    rotate.animateTo(
                                        if (expanded.value) 180f else 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }, trailingIcon = {
                                if (selectedFont == font) Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Check",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            })
                    }
                }

            })
        }

        MySlider(modifier = Modifier.padding(16.dp, 2.dp),
            value = fontSize,
            onValueChanged = { onFontSizeChanged(it) })

        Text(
            modifier = Modifier
                .padding(16.dp, 4.dp)
                .fillMaxWidth(),
            text = preview,
            fontFamily = FontFamily(
                Typeface.createFromAsset(LocalContext.current.assets, selectedFont)
            ),
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.7).sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyItemsPrev() {
    MaterialTheme {
        val checked = remember { mutableStateOf(true) }
        val slider = remember { mutableFloatStateOf(16f) }
        Column(modifier = Modifier.padding(4.dp)) {
            MySwitchBox(checked = checked.value,
                label = "Check box",
                onCheckChanged = { checked.value = it })
            MySlider(value = slider.floatValue, onValueChanged = { slider.floatValue = it })
            Spacer(modifier = Modifier.height(4.dp))
            MyBtnGroup(title = "Title",
                items = listOf("First", "Second", "Thread"),
                checkedItem = "First",
                onCheckChanged = {})
        }
    }
}

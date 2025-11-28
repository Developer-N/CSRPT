package ir.namoo.quran.settings

import android.graphics.Typeface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R

@Composable
fun MySwitchBox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    label: String,
    onCheckChanged: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onCheckChanged(!checked) }
            .padding(2.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(6.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            modifier = Modifier.padding(4.dp),
            text = label,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
        )
        Switch(checked = checked, onCheckedChange = null, thumbContent = {
            AnimatedContent(
                targetState = checked,
                transitionSpec = {
                    if (!checked)
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) { -it } togetherWith slideOutHorizontally { it }
                    else
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) { it } togetherWith slideOutHorizontally { -it }
                }
            ) {
                Icon(
                    imageVector = if (it) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = "Check",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        })
    }
}

@Composable
fun MySlider(modifier: Modifier = Modifier, value: Float, onValueChanged: (Float) -> Unit) {
    Slider(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChanged(it) },
        valueRange = 10f..52f,
        steps = 40
    )
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        if (title.isNotEmpty()) Text(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            text = title,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.Center
        ) {
            items.forEach {
                ElevatedButton(
                    modifier = Modifier
                        .padding(2.dp)
                        .animateContentSize(animationSpec = spring()),
                    onClick = { onCheckChanged(it) },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (it == checkedItem) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = if (it == checkedItem) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = if (isPathSetting) {
                            if (it.contains("emulated")) stringResource(id = R.string.internal)
                            else stringResource(id = R.string.external)
                        } else it,
                        fontWeight = if (it == checkedItem) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
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
    var expanded by remember { mutableStateOf(false) }
    val rotate by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "rotate", animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        )
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp), shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = title,
                fontWeight = FontWeight.SemiBold
            )
            ElevatedButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { expanded = !expanded }) {
                Icon(
                    modifier = Modifier.rotate(rotate),
                    imageVector = Icons.Filled.ExpandCircleDown,
                    contentDescription = "DropDown"
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedContent(
                    targetState = fontNames[fontList.indexOf(selectedFont)],
                    label = "font"
                ) {
                    Text(text = it)
                }
                DropdownMenu(
                    expanded = expanded,
                    shape = MaterialTheme.shapes.extraLarge,
                    onDismissRequest = { expanded = false }) {
                    fontList.forEach { font ->
                        DropdownMenuItem(
                            text = { Text(text = fontNames[fontList.indexOf(font)]) },
                            onClick = {
                                expanded = false
                                onFontFamilyChanged(font)
                            },
                            trailingIcon = {
                                if (selectedFont == font) Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Check",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            })
                    }
                }
            }
        }

        MySlider(
            modifier = Modifier.padding(16.dp, 2.dp),
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
            MySwitchBox(
                checked = checked.value,
                label = "Check box",
                onCheckChanged = { checked.value = it })
            MySlider(value = slider.floatValue, onValueChanged = { slider.floatValue = it })
            Spacer(modifier = Modifier.height(4.dp))
            MyBtnGroup(
                title = "Title",
                items = listOf("First", "Second", "Thread"),
                checkedItem = "First",
                onCheckChanged = {})
        }
    }
}

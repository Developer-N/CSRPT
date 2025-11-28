package ir.namoo.hadeeth.ui.chapter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.global.numeral
import ir.namoo.hadeeth.repository.CategoryEntity

@Composable
fun HadeethCategoryItem(
    category: CategoryEntity,
    onItemClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hadeethsCount by animateFloatAsState(targetValue = category.hadeethsCount.toFloat())
    val numeral by numeral.collectAsState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 4.dp)
            .clickable(onClick = { onItemClicked(category.categoryID) })
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(vertical = 2.dp, horizontal = 4.dp),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            AnimatedContent(targetState = category.title) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = it.ifEmpty { "-" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
            }
            Text(
                modifier = Modifier.padding(8.dp),
                text = numeral.format(hadeethsCount.toInt()),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

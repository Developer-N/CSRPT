package ir.namoo.religiousprayers.ui.shared

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byagowi.persiancalendar.R
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.iconColor

@Composable
fun NothingFoundUIElement(
    message: String = stringResource(id = R.string.nothing_found)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinity_transition")
    val rotate by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 90f, animationSpec = infiniteRepeatable(
            animation = tween(500), repeatMode = RepeatMode.Reverse
        ), label = "rotate_animation"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            fontFamily = FontFamily(appFont),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            Icons.Filled.Search,
            modifier = Modifier
                .rotate(rotate)
                .size(32.dp),
            contentDescription = message,
            tint = iconColor
        )
    }
}
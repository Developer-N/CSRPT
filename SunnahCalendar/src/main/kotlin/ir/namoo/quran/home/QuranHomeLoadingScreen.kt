package ir.namoo.quran.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement

@Composable
fun QuranHomeLoadingScreen() {
    Scaffold { paddingValues ->
        Surface(
            shape = materialCornerExtraLargeTop(),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                LoadingUIElement()
            }
        }
    }
}

@Preview(
    name = "light", showSystemUi = true, locale = "fa",
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Preview(
    name = "dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true, showSystemUi = true, locale = "fa"
)
@Composable
private fun QuranHomeLoadingScreenPreview() {
    MaterialTheme {
        QuranHomeLoadingScreen()
    }
}

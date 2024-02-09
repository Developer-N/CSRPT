package ir.namoo.quran.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.appTopAppBarColors
import com.byagowi.persiancalendar.ui.utils.materialCornerExtraLargeTop
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(drawerState: DrawerState, viewModel: SettingViewModel = koinViewModel()) {
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()
    viewModel.loadData()

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.settings))
        }, navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    drawerState.apply {
                        if (isOpen) close() else open()
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Menu, contentDescription = "Menu"
                )
            }
        }, colors = appTopAppBarColors()
        )
    }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            shape = materialCornerExtraLargeTop()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 4.dp)
                            .height(2.dp),
                        strokeCap = StrokeCap.Round
                    )
                }
                TranslateItems(viewModel)
                QaraatItems(viewModel)
                FontSettingItems(viewModel)
            }
        }
    }
}

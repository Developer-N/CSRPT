package ir.namoo.religiousprayers.ui.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(query: String, updateQuery: (String) -> Unit, closeSearchBar: () -> Unit) {
    val focus = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { updateQuery(it) },
                    onSearch = {
                        focus.clearFocus()
                        updateQuery(it)
                    },
                    placeholder = {
                        Text(text = stringResource(R.string.search))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            focus.clearFocus()
                            if (query.isNotEmpty()) updateQuery("")
                            else closeSearchBar()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    expanded = false,
                    onExpandedChange = {}
                )
            },
            expanded = false,
            onExpandedChange = {}
        ) { }
    }
}

@Preview(locale = "fa")
@Composable
fun PrevSearchAppBar() {
    AppTheme {
        SearchAppBar(query = "", updateQuery = {}, closeSearchBar = {})
    }
}

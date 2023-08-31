package ir.namoo.religiousprayers.ui.downloadtimes

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentDownloadTimesBinding
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.appFont
import ir.namoo.commons.utils.cardColor
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import org.koin.android.ext.android.get

class DownloadPrayTimesFragment : Fragment() {
    private lateinit var binding: FragmentDownloadTimesBinding
    private val viewModel: DownloadPrayTimesViewModel = get()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadTimesBinding.inflate(inflater, container, false)

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.download_upload)
            toolbar.setupMenuNavigation()
            toolbar.menu.add(R.string.search).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.actionView = SearchView(toolbar.context).also { searchView ->
                    searchView.findViewById<LinearLayout>(androidx.appcompat.R.id.search_bar).layoutTransition =
                        LayoutTransition()
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            viewModel.search(newText ?: "")
                            return true
                        }
                    })
                }
            }
            toolbar.menu.add(R.string.update).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_refresh)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.onClick { updateView() }
            }
        }
        binding.appBar.root.hideToolbarBottomShadow()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentRoot.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        binding.downloadView.setContent {
            Mdc3Theme {
                val isLoading by viewModel.isLoading.collectAsState()
                val query by viewModel.query.collectAsState()
                val addedCities by viewModel.addedCities.collectAsState()
                val cityItemState by viewModel.cityIteState.collectAsState()
                val selectedCity by viewModel.selectedCity.collectAsState()

                Column(
                    modifier = Modifier
                        .background(cardColor)
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(id = R.string.available_cities_list),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        fontFamily = FontFamily(appFont),
                        textAlign = TextAlign.Center
                    )
                    if (addedCities.isNotEmpty()) Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(4f),
                            text = stringResource(id = R.string.city),
                            fontFamily = FontFamily(appFont)
                        )
                        Text(
                            modifier = Modifier.weight(5f),
                            text = stringResource(id = R.string.update_date),
                            fontFamily = FontFamily(appFont)
                        )
                    }

                    AnimatedVisibility(visible = isLoading) {
                        LoadingUIElement()
                    }
                    val listState = rememberLazyListState()
                    if (addedCities.isNotEmpty() && cityItemState.isNotEmpty()) {
                        LazyColumn(state = listState) {
                            items(items = addedCities, key = { it.id }) { city ->
                                Box(modifier = Modifier.animateItemPlacement()) {
                                    CityItemUIElement(city = city,
                                        searchText = query,
                                        cityItemState = cityItemState[addedCities.indexOf(city)].apply {
                                            isSelected = selectedCity == city.name
                                        },
                                        download = { viewModel.download(city, requireContext()) })
                                }
                            }
                        }
                    } else if (!isLoading && query.isNotEmpty())
                        NothingFoundUIElement()
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateView()
    }

    private fun updateView() {
        if (isNetworkConnected(requireContext())) viewModel.loadData(requireContext())
        else {
            onPause()
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.network_error_title)
                setMessage(R.string.network_error_message)
                setPositiveButton(R.string.str_retry) { dialog, _ ->
                    dialog.dismiss()
                    onResume()
                }
                show()
            }
        }
    }
}//end of class DownloadUploadFragment

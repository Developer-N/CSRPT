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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.PREF_GEOCODED_CITYNAME
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentDownloadTimesBinding
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.namoo.commons.utils.getAppFont
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
                val typeface = remember { getAppFont(requireContext()) }
                val iconColor =
                    remember { Color(requireContext().resolveColor(android.R.attr.colorAccent)) }
                val cardColor =
                    remember { Color(requireContext().resolveColor(com.google.accompanist.themeadapter.material3.R.attr.colorSurface)) }
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
                        fontFamily = FontFamily(typeface),
                        textAlign = TextAlign.Center
                    )
                    if (viewModel.serverCitiesList.isNotEmpty()) Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(4f),
                            text = stringResource(id = R.string.city),
                            fontFamily = FontFamily(typeface)
                        )
                        Text(
                            modifier = Modifier.weight(5f),
                            text = stringResource(id = R.string.update_date),
                            fontFamily = FontFamily(typeface)
                        )
                    }

                    AnimatedVisibility(visible = viewModel.isLoading) {
                        LoadingUIElement(typeface = typeface)
                    }
                    val listState = rememberLazyListState()
                    LazyColumn(state = listState) {
                        if (viewModel.serverCitiesList.isNotEmpty() && viewModel.citiesState.isNotEmpty()) {
                            viewModel.selectCity(
                                requireContext().appPrefs.getString(
                                    PREF_GEOCODED_CITYNAME, ""
                                )
                            )
                            items(items = viewModel.serverCitiesList.filter {
                                it.name.contains(viewModel.searchQuery)
                            }, key = { it.id }) { city ->
                                Box(modifier = Modifier.animateItemPlacement()) {
                                    CityItemUIElement(city = city,
                                        searchText = viewModel.searchQuery,
                                        typeface = typeface,
                                        cardColor = cardColor,
                                        iconColor = iconColor,
                                        cityItemState = viewModel.citiesState[viewModel.serverCitiesList.indexOf(
                                            city
                                        )],
                                        download = { viewModel.download(city, requireContext()) })
                                }
                            }
                        }
                    }
                    if (viewModel.serverCitiesList.none {
                            it.name.contains(
                                viewModel.searchQuery
                            )
                        } && viewModel.searchQuery.isNotEmpty()) NothingFoundUIElement(
                        typeface = typeface, iconColor = iconColor
                    )
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
        if (isNetworkConnected(requireContext())) viewModel.loadAddedCities()
        else {
            onPause()
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle(R.string.network_error_title)
                setMessage(R.string.network_error_message)
                setPositiveButton(R.string.str_retray) { dialog, _ ->
                    dialog.dismiss()
                    onResume()
                }
                show()
            }
        }
    }
}//end of class DownloadUploadFragment

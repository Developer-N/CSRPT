package ir.namoo.religiousprayers.ui.azkar

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentAzkarBinding
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import ir.namoo.commons.DEFAULT_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_LANG
import ir.namoo.commons.PREF_AZKAR_REINDER
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.getAppFont
import ir.namoo.religiousprayers.ui.shared.LoadingUIElement
import ir.namoo.religiousprayers.ui.shared.NothingFoundUIElement
import org.koin.androidx.viewmodel.ext.android.viewModel


@SuppressLint("SdCardPath")
class AzkarFragment : Fragment() {
    private lateinit var binding: FragmentAzkarBinding
    private val azkarViewModel: AzkarViewModel by viewModel()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAzkarBinding.inflate(inflater, container, false)
        azkarViewModel.setLang(
            requireContext().appPrefsLite.getString(
                PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG
            ) ?: DEFAULT_AZKAR_LANG
        )
        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.azkar)
            toolbar.setSubtitle(R.string.hisnulmuslim)
            toolbar.setupMenuNavigation()

            toolbar.menu.also { menu ->
                menu.add(R.string.search).also {
                    it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    it.actionView = SearchView(toolbar.context).also { searchView ->
                        searchView.findViewById<LinearLayout>(androidx.appcompat.R.id.search_bar).layoutTransition =
                            LayoutTransition()
                        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                azkarViewModel.search(newText ?: "")
                                return true
                            }
                        })
                    }
                }
                menu.add(R.string.favorite).also {
                    it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_favorite_border)
                    it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                    it.onClick {
                        azkarViewModel.showBookmarks()
                        it.setIcon(
                            if (azkarViewModel.isBookmarkShowed) R.drawable.ic_favorite
                            else R.drawable.ic_favorite_border
                        )
                    }
                }
                menu.add(R.string.azkar_reminder).also {
                    it.isCheckable = true
                    it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                    it.isChecked =
                        requireContext().appPrefsLite.getBoolean(PREF_AZKAR_REINDER, false)
                    it.onClick {
                        it.isChecked = !it.isChecked
                        requireContext().appPrefsLite.edit {
                            putBoolean(PREF_AZKAR_REINDER, it.isChecked)
                        }
                    }
                }
                menu.addSubMenu(R.string.language).also { subMenu ->
                    val groupID = Menu.FIRST
                    val langList = listOf(Language.FA, Language.CKB, Language.AR)
                    val prefs = requireContext().appPrefsLite
                    val selectedLang =
                        prefs.getString(PREF_AZKAR_LANG, DEFAULT_AZKAR_LANG) ?: DEFAULT_AZKAR_LANG
                    langList.forEach { lang ->
                        val item = subMenu.add(groupID, Menu.NONE, Menu.NONE, lang.nativeName)
                        item.isChecked = lang.code == selectedLang
                        item.onClick {
                            prefs.edit { putString(PREF_AZKAR_LANG, lang.code) }
                            azkarViewModel.setLang(lang.code)
                            item.isChecked = true
                        }
                    }
                    subMenu.setGroupCheckable(groupID, true, true)
                }
            }
        }
        binding.appBar.root.hideToolbarBottomShadow()
        azkarViewModel.getChapters()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentRoot.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        binding.azkarChapter.setContent {
            Mdc3Theme {
                val appFont = remember { getAppFont(requireContext()) }
                val iconColor =
                    remember { Color(requireContext().resolveColor(android.R.attr.colorAccent)) }
                val cardColor =
                    remember { Color(requireContext().resolveColor(com.google.accompanist.themeadapter.material3.R.attr.colorSurface)) }
                Column(
                    modifier = Modifier
                        .background(cardColor)
                        .fillMaxSize()
                ) {
//                    Button(onClick = { startAzkar(requireContext(), "morning", null) }) {
//                        Text(text = stringResource(id = R.string.morning_azkar))
//                    }
//                    Button(onClick = { startAzkar(requireContext(), "evening", null) }) {
//                        Text(text = stringResource(id = R.string.evening_azkar))
//                    }
                    AnimatedVisibility(azkarViewModel.isLoading) {
                        LoadingUIElement(typeface = appFont)
                    }
                    val listState = rememberLazyListState()
                    LazyColumn(state = listState) {
                        if (azkarViewModel.filteredAzkarChapters.isNotEmpty()) {
                            items(items = azkarViewModel.filteredAzkarChapters,
                                key = { it.id }) { zikr ->
                                Box(modifier = Modifier.animateItemPlacement()) {
                                    ZikrChapterUI(zikr,
                                        searchText = azkarViewModel.searchQuery,
                                        lang = azkarViewModel.azkarLang,
                                        typeface = appFont,
                                        cardColor = cardColor,
                                        iconColor = iconColor,
                                        onFavClick = { azkarViewModel.updateAzkarChapter(it) },
                                        onCardClick = { id ->
                                            startActivity(Intent(
                                                requireContext(), AzkarActivity::class.java
                                            ).apply {
                                                putExtra("chapterID", id)
                                            })
                                        })
                                }
                            }
                        }
                    }
                    if (azkarViewModel.filteredAzkarChapters.size == 0) {
                        NothingFoundUIElement(
                            typeface = appFont, iconColor = iconColor
                        )
                        if (azkarViewModel.searchQuery.isEmpty() && !azkarViewModel.isBookmarkShowed) azkarViewModel.getChapters()
                    }
                }
            }
        }

        return binding.root
    }//end of onCreateView
}//end of AzkarFragment

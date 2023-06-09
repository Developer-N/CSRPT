package ir.namoo.quran.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSearchBinding
import com.byagowi.persiancalendar.databinding.ItemSearchBinding
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.appbar.MaterialToolbar
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import ir.namoo.quran.utils.ACTION_CHANGE_SURA
import ir.namoo.quran.utils.arabicFont
import ir.namoo.quran.utils.englishFont
import ir.namoo.quran.utils.farsiFont
import ir.namoo.quran.utils.kKurdishToArabicCharacters
import ir.namoo.quran.utils.kurdishFont
import ir.namoo.quran.utils.kyFarsiToArabicCharacters
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var chapters: MutableList<ChapterEntity>
    private val adapter = SearchAdapter()
    private var mToast: Toast? = null

    private val db: QuranDB by inject()

    @SuppressLint("PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        binding.searchRecycler.visibility = View.GONE
        binding.searchRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRecycler.adapter = adapter
        binding.searchTxtWait.visibility = View.VISIBLE
        binding.searchTxtWait.startAnimation(
            AnimationUtils.loadAnimation(
                requireContext(),
                com.google.android.material.R.anim.abc_fade_in
            )
        )
        chapters = db.chaptersDao().getAllChapters()
        Handler(Looper.getMainLooper()).post {
            val model: QuranViewModel by viewModel()
            model.getAllVerses().observe(requireActivity()) {
                if (it.size > 0) {
                    binding.appBar.toolbar.subtitle = formatNumber(it.size)

                    binding.searchTxtWait.visibility = View.GONE
                    binding.searchRecycler.visibility = View.VISIBLE
                    adapter.setVerses(it)
                } else {
                    binding.appBar.toolbar.subtitle = ""

                }
            }
        }
        setupMenu(binding.appBar.toolbar)
        binding.appBar.root.hideToolbarBottomShadow()


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentRoot.updatePadding(bottom = insets.bottom)
            binding.appBar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }

        return binding.root

    }//end of onCreateView

    private fun setupMenu(toolbar: MaterialToolbar) {
        toolbar.setTitle(R.string.search)
        toolbar.setupMenuNavigation()
        val searchView = SearchView(toolbar.context)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
        toolbar.menu.add(R.string.search).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.actionView = searchView
        }
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Adapter
    private inner class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>(),
        Filterable {
        private var filteredVerses: MutableList<QuranEntity>? = null
        private var allVerses: MutableList<QuranEntity>? = null
        private var filter = ""
        private var lastExpend: ItemSearchBinding? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder =
            SearchViewHolder(ItemSearchBinding.inflate(layoutInflater, parent, false))

        override fun getItemCount(): Int = runCatching {
            filteredVerses!!.size
        }.onFailure(logException).getOrDefault(0)

        override fun getItemViewType(position: Int): Int = position

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            filteredVerses?.let {
                holder.bind(it[position], position)
            }
        }

        fun setVerses(verses: MutableList<QuranEntity>) {
            allVerses = verses
            filteredVerses = verses
            notifyDataSetChanged()
        }

        override fun getFilter(): Filter {

            return object : Filter() {
                @SuppressLint("ShowToast")
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val charString: String = constraint.toString()
                    if (charString.isEmpty() || charString == "") {
                        filter = ""
                        filteredVerses = allVerses
                    } else {
                        val fl = ArrayList<QuranEntity>()
                        for (quranEntity: QuranEntity in allVerses!!)
                            if (quranEntity.simple!!.contains(kyFarsiToArabicCharacters(charString)) ||
                                quranEntity.simple_clean!!.contains(
                                    kyFarsiToArabicCharacters(
                                        charString
                                    )
                                ) ||
                                quranEntity.en_pickthall!!.contains(charString) ||
                                quranEntity.fa_khorramdel!!.contains(charString) ||
                                quranEntity.ku_asan!!.contains(kKurdishToArabicCharacters(charString)) ||
                                quranEntity.ku_asan!!.contains(charString)
                            )
                                fl.add(quranEntity)
                        filteredVerses = fl
                        filter = constraint.toString()
                    }
                    val filterResult = FilterResults()

                    filterResult.values = filteredVerses
                    filteredVerses?.let {
                        mToast = if (mToast == null) {
                            Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT)
                        } else {
                            mToast?.cancel()
                            Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT)
                        }
                        mToast?.setText(
                            String.format(
                                resources.getString(R.string.result_found),
                                it.size.toString()
                            )
                        )
                        mToast?.show()
                    }
                    return filterResult
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredVerses = results!!.values as MutableList<QuranEntity>?
                    notifyDataSetChanged()
                }

            }
        }

        //#######################################################33
        inner class SearchViewHolder(private val itemBinding: ItemSearchBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            private var mPosition = 0

            init {
                itemBinding.searchTxtItemArabic.typeface = arabicFont
                itemBinding.searchTxtItemArabic.textSize = 18f
                itemBinding.searchTxtItemEnglish.typeface = englishFont
                itemBinding.searchTxtItemKurdish.typeface = kurdishFont
                itemBinding.searchTxtItemFarsi.typeface = farsiFont

                itemBinding.searchTxtItemEnglish.visibility = View.GONE
                itemBinding.searchTxtItemKurdish.visibility = View.GONE
                itemBinding.searchTxtItemFarsi.visibility = View.GONE
                itemBinding.searchTxtItemArabic.maxLines = 2

                itemBinding.btnSearchExpand.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    expand()
                }
                itemBinding.root.setOnClickListener {
                    expand()
                }
            }

            @SuppressLint("PrivateResource")
            fun bind(aya: QuranEntity, position: Int) {
                mPosition = position
                itemBinding.searchTxtItemArabic.text = aya.simple_clean
                itemBinding.searchTxtItemEnglish.text = aya.en_pickthall
                itemBinding.searchTxtItemKurdish.text = aya.ku_asan
                itemBinding.searchTxtItemFarsi.text = aya.fa_khorramdel

                itemBinding.searchTxtItemSuraAya.text = formatNumber(
                    String.format(
                        itemView.context!!.getString(R.string.search_head),
                        chapters.first { it.sura == aya.sura }.nameArabic,
                        aya.aya
                    )
                )
                itemBinding.searchBtnGoToAya.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    Intent().apply {
                        action = ACTION_CHANGE_SURA
                        putExtra("sura", aya.sura)
                        putExtra("aya", aya.aya)
                        it.context!!.sendBroadcast(this)
                    }
                }

                if (filter.isNotEmpty()) {
                    val strReplace = "<font color='#F44336'>$filter</font>"
                    itemBinding.searchTxtItemArabic.text = Html.fromHtml(
                        itemBinding.searchTxtItemArabic.text.toString().replace(filter, strReplace)
                    )
                    itemBinding.searchTxtItemEnglish.text = Html.fromHtml(
                        itemBinding.searchTxtItemEnglish.text.toString().replace(filter, strReplace)
                    )
                    itemBinding.searchTxtItemKurdish.text = Html.fromHtml(
                        itemBinding.searchTxtItemKurdish.text.toString().replace(filter, strReplace)
                    )
                    itemBinding.searchTxtItemFarsi.text = Html.fromHtml(
                        itemBinding.searchTxtItemFarsi.text.toString().replace(filter, strReplace)
                    )
                }
            }//end of bind

            @SuppressLint("PrivateResource")
            fun expand() {
                val arrowRotationAnimationDuration =
                    resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

                if (itemBinding.searchTxtItemEnglish.visibility == View.VISIBLE) {
                    itemBinding.btnSearchExpand.animate()
                        .rotation(0f)
                        .setDuration(arrowRotationAnimationDuration)
                        .start()
                    itemBinding.searchTxtItemEnglish.visibility = View.GONE
                    itemBinding.searchTxtItemKurdish.visibility = View.GONE
                    itemBinding.searchTxtItemFarsi.visibility = View.GONE
                    itemBinding.searchTxtItemArabic.maxLines = 2
                    lastExpend = null
                } else {
                    if (lastExpend != null && lastExpend != itemBinding) {
                        lastExpend?.let {
                            it.btnSearchExpand.animate()
                                .rotation(0f)
                                .setDuration(arrowRotationAnimationDuration)
                                .start()
                            it.searchTxtItemEnglish.visibility = View.GONE
                            it.searchTxtItemKurdish.visibility = View.GONE
                            it.searchTxtItemFarsi.visibility = View.GONE
                            it.searchTxtItemArabic.maxLines = 2
                        }
                    }
                    itemBinding.btnSearchExpand.animate()
                        .rotation(180f)
                        .setDuration(arrowRotationAnimationDuration)
                        .start()
                    itemBinding.searchTxtItemEnglish.visibility = View.VISIBLE
                    itemBinding.searchTxtItemKurdish.visibility = View.VISIBLE
                    itemBinding.searchTxtItemFarsi.visibility = View.VISIBLE
                    itemBinding.searchTxtItemArabic.maxLines = 10
                    lastExpend = itemBinding
                    (binding.searchRecycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        mPosition,
                        0
                    )
                }
                val transition = ChangeBounds()
                transition.interpolator = AnticipateOvershootInterpolator()
                TransitionManager.beginDelayedTransition(binding.searchRecycler, transition)
            }
        }
    }//end of class SearchAdapter
}//end of class

package ir.namoo.quran.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentChapterBinding
import com.byagowi.persiancalendar.databinding.ItemChapterBinding
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.ktor.client.*
import io.ktor.client.engine.android.*
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.commons.utils.appPrefsLite
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.snackMessage
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.utils.ACTION_CHANGE_SURA
import ir.namoo.quran.utils.DB_LINK
import ir.namoo.quran.utils.PREF_LAST_VISITED_VERSE
import ir.namoo.quran.utils.getQuranDBDownloadFolder
import ir.namoo.quran.utils.isDigit
import ir.namoo.quran.utils.kyFarsiToArabicCharacters
import ir.namoo.quran.viewmodels.ChapterViewModel
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class ChapterFragment : Fragment() {

    private lateinit var binding: FragmentChapterBinding
    private lateinit var adapter: ChapterAdapter
    private var isFavShown = false
    private val viewModel: ChapterViewModel by viewModel()
    private lateinit var searchView: SearchView

    private val db: QuranDB by inject()

    @SuppressLint("SdCardPath", "PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChapterBinding.inflate(inflater)
        //############################# db check
        val dbFile = File("/data/data/${requireContext().packageName}/databases/quran.db")
        if (dbFile.exists()) {// db exist and show chapters
            loadChapters()
        } else {// db not exist show download
            binding.downloadLayout.visibility = View.VISIBLE
            binding.txtQuranDownloadSize.text =
                formatNumber(binding.txtQuranDownloadSize.text.toString())
            binding.chapterLayout.visibility = View.GONE
            binding.btnQuranDownload.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(), com.google.android.material.R.anim.abc_fade_in
                    )
                )
                if (!isNetworkConnected(requireContext())) {
                    MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.network_error_title))
                        .setMessage(resources.getString(R.string.network_error_message))
                        .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                            dialog.dismiss()
                        }.show()
                } else {
                    download()
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

    @SuppressLint("SdCardPath")
    override fun onResume() {
        super.onResume()
        val dbFile = File("/data/data/${requireContext().packageName}/databases/quran.db")
        if (dbFile.exists()) {
            initLatestVisited()
        }
    }

    @SuppressLint("SdCardPath")
    private fun loadChapters() {
        binding.downloadLayout.visibility = View.GONE
        binding.chapterLayout.visibility = View.VISIBLE
        binding.recyclerQuranChapter.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChapterAdapter()
        binding.recyclerQuranChapter.adapter = adapter
        viewModel.getChapters().observe(requireActivity()) {
            if (it.size > 0) adapter.setChapters(it)
            else File("/data/data/${requireContext().packageName}/databases/quran.db").delete()
        }
        initLatestVisited()
    }//end of loadChapters

    private fun initLatestVisited() {
        val latestVisited = requireContext().appPrefsLite.getInt(PREF_LAST_VISITED_VERSE, -1)
        if (latestVisited >= 0) {
            binding.cardLatestVisitedVerse.visibility = View.VISIBLE
            val verse = db.quranDao().getVerseByIndex(latestVisited)
            val chapter = db.chaptersDao().getChapter(verse?.sura ?: 1)
            binding.txtLatestVisitedVerse.text = formatNumber(
                String.format(
                    getString(R.string.latest_visited), chapter.nameArabic, verse?.aya ?: 1
                )
            )
            binding.cardLatestVisitedVerse.setOnClickListener {
                requireContext().sendBroadcast(Intent().apply {
                    action = ACTION_CHANGE_SURA
                    putExtra("sura", chapter.sura)
                    putExtra("aya", verse?.aya ?: 1)
                })
            }
        } else {
            binding.cardLatestVisitedVerse.visibility = View.GONE
        }
    }

    //#################################### menu
    private fun setupMenu(toolbar: MaterialToolbar) {
        toolbar.setTitle(R.string.chapter)
        toolbar.setupMenuNavigation()
        val searchView = SearchView(toolbar.context).also { searchView = it }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty() && isDigit(newText)) {
                    (binding.recyclerQuranChapter.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
                        newText.toInt() - 1, 0
                    )
                } else adapter.filter.filter(kyFarsiToArabicCharacters(newText))
                return true
            }
        })
        toolbar.menu.add(R.string.favorite).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_favorite_border)
            it.onClick {
                if (isFavShown) {
                    it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_favorite_border)
                    adapter.filter.filter("fav0")
                } else {
                    it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_favorite)
                    adapter.filter.filter("fav1")
                }
                isFavShown = !isFavShown
            }
        }
        toolbar.menu.add(R.string.search).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.actionView = searchView
        }
        toolbar.menu.add(R.string.sort_order).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_filter)
            it.onClick {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(resources.getString(R.string.chapter_sort_dialog_title))
                    setItems(R.array.sort_order) { _, which ->
                        run {
                            when (which) {
                                0 -> adapter.filter.filter("default")
                                1 -> adapter.filter.filter("alphabet")
                                2 -> adapter.filter.filter("revelation")
                                3 -> adapter.filter.filter("ayaIncrease")
                                4 -> adapter.filter.filter("ayaDecrease")

                            }
                        }
                    }
                    show()
                }
            }
        }
        toolbar.menu.add(R.string.go_to_page).also { menuItem ->
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menuItem.onClick {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(getString(R.string.select_page))
                    val input = AppCompatEditText(requireContext())
                    input.inputType = InputType.TYPE_CLASS_NUMBER
                    setView(input)
                    setPositiveButton(resources.getString(R.string.go_to_page)) { _, _ ->
                        if (input.text.toString().toInt() in 1..604) run {
                            viewModel.getPage(input.text.toString().toInt())
                                .observe(viewLifecycleOwner) {
                                    val tj = it[0]
                                    val intent = Intent()
                                    intent.action = ACTION_CHANGE_SURA
                                    intent.putExtra("sura", tj.sura)
                                    intent.putExtra("aya", tj.aya)
                                    requireContext().sendBroadcast(intent)
                                }
                        }
                        else snackMessage(binding.root, getString(R.string.page_out_of_bounds))
                    }
                    setNegativeButton(getString(R.string.cancel)) { _, _ ->
                        run {}
                    }
                    show()
                }
            }
        }
        toolbar.menu.add(R.string.go_to_juz).also { menuItem ->
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menuItem.onClick {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(getString(R.string.select_juz))
                    val juz = arrayOfNulls<String>(30)
                    for (i in 0..29) juz[i] = formatNumber((i + 1).toString())
                    setItems(juz) { _, witch ->
                        run {
                            viewModel.getAllJuz().observe(viewLifecycleOwner) {
                                val tj = it[witch]
                                val intent = Intent()
                                intent.action = ACTION_CHANGE_SURA
                                intent.putExtra("sura", tj.sura)
                                intent.putExtra("aya", tj.aya)
                                requireContext().sendBroadcast(intent)
                            }
                        }
                    }
                    show()
                }
            }
        }
        toolbar.menu.add(R.string.go_to_hizb).also { menuItem ->
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menuItem.onClick {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(getString(R.string.select_hizb))
                    val juz = arrayOfNulls<String>(60)
                    for (i in 0..59) juz[i] = formatNumber((i + 1).toString())
                    setItems(juz) { _, witch ->
                        viewModel.getAllHezb().observe(viewLifecycleOwner) {
                            val tj = it[witch * 2]
                            requireContext().sendBroadcast(Intent().apply {
                                action = ACTION_CHANGE_SURA
                                putExtra("sura", tj.sura)
                                putExtra("aya", tj.aya)
                            })
                        }
                    }
                    create().show()
                }
            }
        }

    }//end of onCreateOptionsMenu

    //################################################ Adapter
    private inner class ChapterAdapter : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>(),
        Filterable {

        var chapterList: MutableList<ChapterEntity>? = null
        var filteredChapter: MutableList<ChapterEntity>? = null
        var isInFavMode: Boolean = false
        private var filter = ""

        @SuppressLint("NotifyDataSetChanged")
        fun setChapters(chapterList: MutableList<ChapterEntity>) {
            this.chapterList = chapterList
            this.filteredChapter = chapterList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder =
            ChapterViewHolder(
                ItemChapterBinding.inflate(layoutInflater, parent, false)
            )

        override fun getItemCount(): Int =
            if (filteredChapter == null) 0 else filteredChapter!!.size

        override fun getItemViewType(position: Int): Int = position

        override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
            filteredChapter?.let {
                holder.bind(it[position], position)
            }
        }


        //##############################################
        override fun getFilter(): Filter {

            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    isInFavMode = false
                    val charString: String = constraint.toString()
                    if (charString == "default") {
                        filteredChapter!!.sortBy {
                            it.sura
                        }
                    } else if (charString == "alphabet") {
                        filteredChapter!!.sortBy {
                            it.nameArabic
                        }
                    } else if (charString == "ayaIncrease") {
                        filteredChapter!!.sortBy {
                            it.ayaCount
                        }
                    } else if (charString == "ayaDecrease") {
                        filteredChapter!!.sortByDescending {
                            it.ayaCount
                        }
                    } else if (charString == "revelation") {
                        filteredChapter!!.sortBy {
                            it.revelationOrder
                        }
                    } else if (charString == "fav1") {
                        isInFavMode = true
                        val fl = ArrayList<ChapterEntity>()
                        for (chapter: ChapterEntity in chapterList!!) if (chapter.fav == 1) fl.add(
                            chapter
                        )
                        filteredChapter = fl
                    } else if (charString.isEmpty() || charString == "" || charString == "fav0") {
                        filter = ""
                        filteredChapter = chapterList
                    } else {
                        val fl = ArrayList<ChapterEntity>()
                        for (chapter: ChapterEntity in chapterList!!) if (chapter.nameArabic!!.contains(
                                constraint.toString()
                            )
                        ) fl.add(chapter)
                        filteredChapter = fl
                        filter = constraint.toString()
                    }
                    val filterResult = FilterResults()

                    filterResult.values = filteredChapter
                    return filterResult
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredChapter = results!!.values as MutableList<ChapterEntity>?
                    notifyDataSetChanged()
                }

            }

        }

        //##############################################
        inner class ChapterViewHolder(private val itemBinding: ItemChapterBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            @SuppressLint("PrivateResource")
            fun bind(chapter: ChapterEntity, position: Int) {
                itemBinding.chapterTxtRow.text = formatNumber("${position + 1}:")
                itemBinding.chapterTxtSuraName.text = chapter.nameArabic
                itemBinding.chapterTxtAyaCount.text =
                    formatNumber(String.format(getString(R.string.aya_count), chapter.ayaCount))
                itemBinding.chapterTxtRevelationOrder.text = formatNumber(
                    String.format(
                        getString(R.string.revelation_order), chapter.revelationOrder
                    )
                )
                itemBinding.chapterTxtType.text =
                    if (chapter.type == "Meccan") getString(R.string.meccan)
                    else getString(R.string.medinan)
                itemBinding.btnChapterFav.setImageResource(
                    if (chapter.fav == 1) R.drawable.ic_favorite
                    else R.drawable.ic_favorite_border
                )
                if (filter.isNotEmpty()) {
                    runCatching {
                        val fColorSpan = ForegroundColorSpan(
                            itemView.context.resolveColor(R.attr.colorTextHoliday)
                        )
                        val spannableStringBuilder =
                            SpannableStringBuilder(itemBinding.chapterTxtSuraName.text)
                        spannableStringBuilder.setSpan(
                            fColorSpan,
                            spannableStringBuilder.indexOf(filter),
                            spannableStringBuilder.indexOf(filter) + filter.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        itemBinding.chapterTxtSuraName.text = spannableStringBuilder
                    }.onFailure(logException)
                }//end of if Filter.isNotEmpty
                itemBinding.btnChapterFav.setOnClickListener {
                    it as AppCompatImageButton
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(), com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    chapter.fav = if (chapter.fav == 1) 0 else 1
                    it.setImageResource(
                        if (chapter.fav == 1) R.drawable.ic_favorite
                        else R.drawable.ic_favorite_border
                    )
                    if (isInFavMode) {
                        filteredChapter!!.remove(chapter)
                        notifyItemRemoved(position)
                    }
                    db.chaptersDao().update(chapter)
                }
                itemBinding.root.setOnClickListener {
                    val intent = Intent()
                    intent.action = ACTION_CHANGE_SURA
                    intent.putExtra("sura", chapter.sura)
                    intent.putExtra("aya", 1)
                    requireContext().sendBroadcast(intent)
                }
            }//end of bind()
        }//end of ChapterViewHolder
    }//end of class ChapterAdapter

    //################################################ Download
    @SuppressLint("SdCardPath")
    private fun download() {
        binding.btnQuranDownload.isEnabled = false
        binding.progressQuranDownload.visibility = View.VISIBLE
        File("/data/data/${requireContext().packageName}/databases/quran.zip").apply {
            if (exists()) delete()
        }
        val downloadedFile = File(getQuranDBDownloadFolder(requireContext()), "quran.zip")
        val ktor = HttpClient(Android)
        lifecycleScope.launch {
            ktor.downloadFile(downloadedFile, DB_LINK).collect {
                when (it) {
                    is DownloadResult.Success -> {
                        snackMessage(binding.root, getString(R.string.downloaded))
                        unzip(requireContext())
                    }
                    is DownloadResult.Error -> {
                        binding.btnQuranDownload.isEnabled = true
                        binding.progressQuranDownload.visibility = View.GONE
                        snackMessage(binding.root, getString(R.string.download_failed_tray_again))
                    }
                    is DownloadResult.Progress -> {
                        ObjectAnimator.ofInt(
                            binding.progressQuranDownload,
                            "progress",
                            binding.progressQuranDownload.progress,
                            it.progress
                        ).apply {
                            interpolator = AccelerateDecelerateInterpolator()
                            start()
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("SdCardPath")
    private fun unzip(context: Context): Boolean = runCatching {
        File(getQuranDBDownloadFolder(requireContext()), "quran.zip").copyTo(
            File("/data/data/${requireContext().packageName}/databases/quran.zip")
        )
        ZipFile("/data/data/${context.packageName}/databases/quran.zip").extractAll("/data/data/${context.packageName}/databases/")
        File("/data/data/${context.packageName}/databases/quran.zip").delete()
        loadChapters()
        true
    }.onFailure(logException).getOrDefault(false)

}//end of ChapterFragment

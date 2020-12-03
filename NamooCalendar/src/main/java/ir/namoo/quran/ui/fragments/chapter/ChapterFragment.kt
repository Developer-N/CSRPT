package ir.namoo.quran.ui.fragments.chapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.os.postDelayed
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.ui.QuranActivity
import ir.namoo.quran.utils.*
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentChapterBinding
import ir.namoo.religiousprayers.databinding.ItemChapterBinding
import ir.namoo.religiousprayers.utils.*
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ChapterFragment : Fragment() {

    private lateinit var binding: FragmentChapterBinding
    private lateinit var adapter: ChapterAdapter
    private var isFavShown = false
    private val model: ChapterViewModel by viewModels()
    private lateinit var searchView: SearchView
    private var menu: Menu? = null

    @SuppressLint("SdCardPath", "PrivateResource")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChapterBinding.inflate(inflater)
        (requireActivity() as QuranActivity).setTitleAndSubtitle(
            getString(R.string.chapter),
            ""
        )
        setHasOptionsMenu(true)
        //############################# db check
        val dbFile = File("/data/data/${requireContext().packageName}/databases/quran.db")
        if (dbFile.exists()) {// db exist and show chapters
            loadChapters()
        } else {// db not exist show download
            Handler(Looper.getMainLooper()).postDelayed(1000) {
                enableQuran(false)
            }
            binding.downloadLayout.visibility = View.VISIBLE
            binding.txtQuranDownloadSize.text =
                formatNumber(binding.txtQuranDownloadSize.text.toString())
            binding.chapterLayout.visibility = View.GONE
            binding.btnQuranDownload.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        com.google.android.material.R.anim.abc_fade_in
                    )
                )
                if (isNetworkConnected(requireContext()))
                    DownloadTask().execute()
                else
                    snackMessage(it, getString(R.string.network_error_message))
            }
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
        Handler(Looper.getMainLooper()).postDelayed(1000) {
            enableQuran(true)
        }
        adapter = ChapterAdapter()
        binding.recyclerQuranChapter.adapter = adapter
        model.getChapters().observe(requireActivity(), {
            if (it.size > 0)
                adapter.setChapters(it)
            else
                File("/data/data/${requireContext().packageName}/databases/quran.db").delete()
        })
        initLatestVisited()
    }//end of loadChapters

    private fun initLatestVisited() {
        val latestVisited = requireContext().appPrefsLite.getInt(PREF_LAST_VISITED_VERSE, -1)
        if (latestVisited >= 0) {
            binding.cardLatestVisitedVerse.visibility = View.VISIBLE
            val db = QuranDB.getInstance(requireContext().applicationContext)
            val verse = db.quranDao().getVerseByIndex(latestVisited)
            val chapter = db.chaptersDao().getChapter(verse?.sura ?: 1)
            binding.txtLatestVisitedVerse.text =
                formatNumber(
                    String.format(
                        getString(R.string.latest_visited),
                        chapter.nameArabic,
                        verse?.aya ?: 1
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
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.chapter_menu, menu)
        this.menu = menu
        searchView = menu.findItem(R.id.mnu_chapter_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty() && isDigit(newText)) {
                    (binding.recyclerQuranChapter.layoutManager!! as LinearLayoutManager)
                        .scrollToPositionWithOffset(
                            newText.toInt() - 1, 0
                        )
                } else
                    adapter.filter.filter(kyFarsiToArabicCharacters(newText))
                return true
            }
        })
    }//end of onCreateOptionsMenu

    fun closeSearch() = searchView.run {
        if (!isIconified) {
            onActionViewCollapsed()
            return true
        } else false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnu_chapter_search -> return true
            R.id.mnu_chapter_fav -> {// filter fav chapters
                if (isFavShown) {
                    item.setIcon(R.drawable.ic_favorite_border)
                    adapter.filter.filter("fav0")
                } else {
                    item.setIcon(R.drawable.ic_favorite)
                    adapter.filter.filter("fav1")
                }
                isFavShown = !isFavShown
            }
            R.id.mnu_chapter_sortOrder -> {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(resources.getString(R.string.chapter_sort_dialog_title))
                dialog.setItems(R.array.sort_order) { _, which ->
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
                dialog.create().show()
            }

            R.id.mnu_chapter_go_to_page -> {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.select_page))
                val input = AppCompatEditText(requireContext())
                input.inputType = InputType.TYPE_CLASS_NUMBER

                dialog.setView(input)
                dialog.setPositiveButton(resources.getString(R.string.go_to_page)) { _, _ ->
                    if (input.text.toString().toInt() in 1..604)
                        run {
                            model.getPage(input.text.toString().toInt())
                                .observe(this, {
                                    val tj = it[0]
                                    val intent = Intent()
                                    intent.action = ACTION_CHANGE_SURA
                                    intent.putExtra("sura", tj.sura)
                                    intent.putExtra("aya", tj.aya)
                                    requireContext().sendBroadcast(intent)
                                })
                        }
                    else snackMessage(binding.root, getString(R.string.page_out_of_bounds))
                }
                dialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    run {
                    }
                }
                dialog.create().show()
            }

            R.id.mnu_chapter_go_to_joz -> {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.select_juz))
                val juz = arrayOfNulls<String>(30)
                for (i in 0..29)
                    juz[i] = formatNumber((i + 1).toString())
                dialog.setItems(juz) { _, witch ->
                    run {
                        model.getAllJuz().observe(this, {
                            val tj = it[witch]
                            val intent = Intent()
                            intent.action = ACTION_CHANGE_SURA
                            intent.putExtra("sura", tj.sura)
                            intent.putExtra("aya", tj.aya)
                            requireContext().sendBroadcast(intent)
                        })
                    }
                }
                dialog.create().show()
            }
            R.id.mnu_chapter_go_to_hizb -> {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.select_hizb))
                val juz = arrayOfNulls<String>(120)
                for (i in 0..119)
                    juz[i] = formatNumber((i + 1).toString())
                dialog.setItems(juz) { _, witch ->
                    run {
                        model.getAllHezb().observe(this, {
                            val tj = it[witch]
                            val intent = Intent()
                            intent.action = ACTION_CHANGE_SURA
                            intent.putExtra("sura", tj.sura)
                            intent.putExtra("aya", tj.aya)
                            requireContext().sendBroadcast(intent)
                        })
                    }
                }
                dialog.create().show()
            }
        }
        return true
    }

    private fun enableQuran(enable: Boolean) {
        menu?.let {
            for (m in it.iterator())
                m.isEnabled = enable
        }
        (requireActivity() as QuranActivity).enableDrawerMenu(enable)
    }

    //################################################ Adapter
    private inner class ChapterAdapter :
        RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>(), Filterable {

        var chapterList: MutableList<ChapterEntity>? = null
        var filteredChapter: MutableList<ChapterEntity>? = null
        var isInFavMode: Boolean = false
        private var filter = ""

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
                        for (chapter: ChapterEntity in chapterList!!)
                            if (chapter.fav == 1)
                                fl.add(chapter)
                        filteredChapter = fl
                    } else if (charString.isEmpty() || charString == "" || charString == "fav0") {
                        filter = ""
                        filteredChapter = chapterList
                    } else {
                        val fl = ArrayList<ChapterEntity>()
                        for (chapter: ChapterEntity in chapterList!!)
                            if (chapter.nameArabic!!.contains(constraint.toString()))
                                fl.add(chapter)
                        filteredChapter = fl
                        filter = constraint.toString()
                    }
                    val filterResult = FilterResults()

                    filterResult.values = filteredChapter
                    return filterResult
                }

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
                itemBinding.chapterTxtRevelationOrder.text =
                    formatNumber(
                        String.format(
                            getString(R.string.revelation_order),
                            chapter.revelationOrder
                        )
                    )
                itemBinding.chapterTxtType.text =
                    if (chapter.type == "Meccan")
                        getString(R.string.meccan)
                    else
                        getString(R.string.medinan)
                itemBinding.btnChapterFav.setImageResource(
                    if (chapter.fav == 1)
                        R.drawable.ic_favorite
                    else
                        R.drawable.ic_favorite_border
                )
                if (filter.isNotEmpty()) {
                    try {
                        val fColorSpan = ForegroundColorSpan(
                            getColorFromAttr(
                                itemView.context,
                                R.attr.colorHighlight
                            )
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
                    } catch (ex: Exception) {
                    }
                }//end of if Filter.isNotEmpty
                itemBinding.btnChapterFav.setOnClickListener {
                    it as AppCompatImageButton
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    chapter.fav = if (chapter.fav == 1) 0 else 1
                    it.setImageResource(
                        if (chapter.fav == 1)
                            R.drawable.ic_favorite
                        else
                            R.drawable.ic_favorite_border
                    )
                    if (isInFavMode) {
                        filteredChapter!!.remove(chapter)
                        notifyItemRemoved(position)
                    }
                    QuranDB.getInstance(requireContext()).chaptersDao().update(chapter)
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

    //################################################ Download Task
    @SuppressLint("StaticFieldLeak")
    private inner class DownloadTask : AsyncTask<Unit, Int, String>() {
        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            values.let {
                binding.progressQuranDownload.progress = it[0]!!
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == "OK") {
                loadChapters()
            } else {
                snackMessage(binding.root, "Download failed! try again later")
            }
            binding.btnQuranDownload.isEnabled = true
            binding.progressQuranDownload.visibility = View.GONE

        }

        override fun onPreExecute() {
            super.onPreExecute()
            binding.btnQuranDownload.isEnabled = false
            binding.progressQuranDownload.visibility = View.VISIBLE

        }

        @SuppressLint("SdCardPath")
        override fun doInBackground(vararg p0: Unit?): String {
            return try {
                val dbFile =
                    File("/data/data/${requireContext().packageName}/databases/quran.zip")
                if (dbFile.exists())
                    if (unzip(requireContext()))
                        return "OK"
                val url = URL(DB_LINK)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    "Error"
                } else {
                    binding.progressQuranDownload.max = connection.contentLength

                    val input = connection.inputStream
                    val output = FileOutputStream(dbFile)
                    val data = ByteArray(4096)
                    var total = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        if (isCancelled) {
                            input.close()
                            return "Error"
                        }
                        total += count
                        if (connection.contentLength > 0)
                            publishProgress(total)
                        output.write(data, 0, count)
                    }
                    output.close()
                    input.close()
                    connection.disconnect()
                    if (!unzip(requireContext()))
                        "Error"
                    else
                        "OK"
                }
            } catch (ex: Exception) {
                Log.e(TAG, "quran download: ", ex)
                "Error"
            }
        }//end of doInBackground

        @SuppressLint("SdCardPath")
        private fun unzip(context: Context): Boolean {
            return try {
                ZipFile("/data/data/${context.packageName}/databases/quran.zip").extractAll("/data/data/${context.packageName}/databases/")
                File("/data/data/${context.packageName}/databases/quran.zip").delete()
                true
            } catch (ex: Exception) {
                Log.d("NAMOO", "Error unzip db $ex")
                false
            }
        }
    }//end of download task

}//end of ChapterFragment
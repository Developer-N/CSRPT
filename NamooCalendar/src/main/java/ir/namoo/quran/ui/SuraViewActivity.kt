package ir.namoo.quran.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Filter
import android.widget.Filterable
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import ir.namoo.quran.utils.*
import ir.namoo.religiousprayers.LANG_EN_US
import ir.namoo.religiousprayers.LANG_JA
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.appLink
import ir.namoo.religiousprayers.databinding.ActivitySuraViewBinding
import ir.namoo.religiousprayers.databinding.ItemAyaBinding
import ir.namoo.religiousprayers.utils.*
import java.io.File

class SuraViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuraViewBinding
    private lateinit var searchView: SearchView
    private lateinit var adapter: SuraViewAdapter
    private val model: SuraViewModel by viewModels()
    private lateinit var chapter: ChapterEntity
    private lateinit var db: QuranDB
    private var isPlaying = false
    private val playerReceiver = object : BroadcastReceiver() {
        @SuppressLint("PrivateResource")
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1 != null) {
                Log.e(TAG, "sura view playerReceiver ${p1.extras?.get("action")}")
                val aya = p1.extras?.getInt("aya") ?: 1
                p1.extras?.get("action")?.let {
                    it as String
                    when (it) {
                        QURAN_NOTIFY_VIEW_PLAYER_PLAY -> {
                            isPlaying = true
                            (binding.suraRecycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                                aya - 1,
                                0
                            )
                            if (!binding.cardQuranPalayer.isVisible) {
                                ObjectAnimator.ofFloat(
                                    binding.cardQuranPalayer,
                                    "translationY",
                                    -binding.cardQuranPalayer.height.toFloat(), 0f
                                ).apply {
                                    duration = 500
                                    doOnStart { binding.cardQuranPalayer.visibility = View.VISIBLE }
                                    interpolator = AnticipateOvershootInterpolator()
                                    start()
                                }
                                ObjectAnimator.ofFloat(
                                    binding.suraRecycler,
                                    "translationY",
                                    -binding.cardQuranPalayer.height.toFloat(), 0f
                                ).apply {
                                    duration = 500
                                    interpolator = AnticipateOvershootInterpolator()
                                    start()
                                }
                            }
                            binding.txtQuranPlayInfo.text =
                                formatNumber(String.format(getString(R.string.playing_verse), aya))
                            binding.btnQuranPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled)
                        }
                        QURAN_NOTIFY_VIEW_PLAYER_PAUSE -> {
                            isPlaying = false
                            binding.btnQuranPause.setImageResource(R.drawable.ic_baseline_play_circle_filled)
                        }
                        QURAN_NOTIFY_VIEW_PLAYER_RESUME -> {
                            isPlaying = true
                            binding.btnQuranPause.setImageResource(R.drawable.ic_baseline_pause_circle_filled)
                        }
                        QURAN_NOTIFY_VIEW_PLAYER_STOP -> {
                            isPlaying = false
                            if (binding.cardQuranPalayer.isVisible) {
                                ObjectAnimator.ofFloat(
                                    binding.cardQuranPalayer,
                                    "translationY",
                                    -binding.cardQuranPalayer.height.toFloat()
                                ).apply {
                                    duration = 500
                                    interpolator = AnticipateOvershootInterpolator()
                                    doOnEnd { binding.cardQuranPalayer.visibility = View.GONE }
                                    start()
                                }
                                ObjectAnimator.ofFloat(
                                    binding.suraRecycler,
                                    "translationY",
                                    0f
                                ).apply {
                                    duration = 500
                                    interpolator = AnticipateOvershootInterpolator()
                                    start()
                                }
                            }

                        }
                    }
                }

            }
        }
    }

    companion object {
        var sura = 1
    }

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromName(getThemeFromPreference(this, appPrefs)))
        applyAppLanguage(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        initQuranUtils(this)
        super.onCreate(savedInstanceState)
        if (language !in listOf(LANG_EN_US, LANG_JA))
            overrideFont("SANS_SERIF", getAppFont(applicationContext))
        binding = ActivitySuraViewBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        setSupportActionBar(binding.suraToolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        changeToolbarTypeface(binding.suraToolbar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            binding.surabarLayout.outlineProvider = null
        applyAppLanguage(this)

        sura = intent.extras!!.getInt("sura")
        val aya = intent.extras!!.getInt("aya")
        db = QuranDB.getInstance(applicationContext)
        chapter = db.chaptersDao().getChapter1(sura)
        setTitleAndSubtitle(chapter.nameArabic!!, formatNumber(chapter.ayaCount!!))
        adapter = SuraViewAdapter()
        binding.suraRecycler.adapter = adapter
        binding.suraRecycler.layoutManager = LinearLayoutManager(this)
        model.getAyas().observe(this, {
            if (it.size > 0)
                adapter.setAyas(it)
            if (aya > 0)
                (binding.suraRecycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                    aya - 1,
                    0
                )
        })

        binding.btnQuranNext.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            sendBroadcast(Intent().apply {
                action = QURAN_PLAYER_ACTION
                putExtra("action", NOTIFY_QURAN_NEXT)
            })
        }
        binding.btnQuranPrev.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            sendBroadcast(Intent().apply {
                action = QURAN_PLAYER_ACTION
                putExtra("action", NOTIFY_QURAN_PREVIOUS)
            })
        }
        binding.btnQuranStop.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            sendBroadcast(Intent().apply {
                action = QURAN_PLAYER_ACTION
                putExtra("action", NOTIFY_QURAN_STOP)
            })
        }
        binding.btnQuranPause.setOnClickListener {
            it as AppCompatImageButton
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    this,
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            if (isPlaying) {
//                it.setImageResource(R.drawable.ic_baseline_play_circle_filled)
                sendBroadcast(Intent().apply {
                    action = QURAN_PLAYER_ACTION
                    putExtra("action", NOTIFY_QURAN_PAUSE)
                })
            } else {
//                it.setImageResource(R.drawable.ic_baseline_pause_circle_filled)
                sendBroadcast(Intent().apply {
                    action = QURAN_PLAYER_ACTION
                    putExtra("action", NOTIFY_QURAN_RESUME)
                })
            }
        }



        startPlayerService()
        registerReceiver(playerReceiver, IntentFilter(QURAN_VIEW_PLAYER_ACTION))
    }//end of onCreate

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sura_view_menu, menu)
        searchView = menu!!.findItem(R.id.mnu_sura_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.isNotEmpty() && isDigit(newText)) {
                    (binding.suraRecycler.layoutManager!! as LinearLayoutManager)
                        .scrollToPositionWithOffset(
                            newText.toInt() - 1, 0
                        )
                } else
                    adapter.filter.filter(newText)
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnu_sura_search -> return true
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initUtils(this)
    }

    override fun onResume() {
        super.onResume()
        applyAppLanguage(this)
    }

    fun setTitleAndSubtitle(title: String, subtitle: String): Unit = supportActionBar?.let {
        it.title = title
        it.subtitle = subtitle
    } ?: Unit

    override fun onDestroy() {
        super.onDestroy()
        stopPlayerService()
        unregisterReceiver(playerReceiver)
    }

    private fun startPlayerService() {
        val isRunning = getSystemService<ActivityManager>()?.let { am ->
            try {
                am.getRunningServices(Integer.MAX_VALUE).any {
                    QuranPlayer::class.java.name == it.service.className
                }
            } catch (ex: Exception) {
                Log.e(TAG, "start quran player error : ", ex)
                false
            }
        } ?: false
        if (!isRunning) {
            try {
                val qIntent = Intent(this, QuranPlayer::class.java).apply {
                    putExtra("sura", sura)
                    putExtra("aya", 1)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ContextCompat.startForegroundService(this, qIntent)
                startService(qIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "start quran player error : ", ex)
            }
        }
    }

    private fun stopPlayerService() {
        stopService(Intent(this, QuranPlayer::class.java))
    }

    //############################################
    private inner class SuraViewAdapter :
        RecyclerView.Adapter<SuraViewAdapter.SuraItemViewHolder>(), Filterable {

        private var quranList: MutableList<QuranEntity>? = null
        private var filteredQuranList: MutableList<QuranEntity>? = null
        private var filter = ""

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuraItemViewHolder =
            SuraItemViewHolder(ItemAyaBinding.inflate(layoutInflater, parent, false))

        override fun getItemCount(): Int =
            if (filteredQuranList == null) 0 else filteredQuranList!!.size

        override fun onBindViewHolder(holder: SuraItemViewHolder, position: Int) {
            filteredQuranList?.let {
                holder.bind(it[position])
            }
        }

        override fun getItemViewType(position: Int): Int = position
        fun setAyas(chapterList: MutableList<QuranEntity>) {
            this.quranList = chapterList
            this.filteredQuranList = chapterList
            notifyDataSetChanged()
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredQuranList = results!!.values as MutableList<QuranEntity>?
                    notifyDataSetChanged()
                }

                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val charString: String = constraint.toString()
                    val f1 = ArrayList<QuranEntity>()
                    for (quranEntity: QuranEntity in quranList!!)
                        if (quranEntity.simple!!.contains(kyFarsiToArabicCharacters(charString)) ||
                            quranEntity.simple_clean!!.contains(kyFarsiToArabicCharacters(charString)) ||
                            quranEntity.en_transilation!!.contains(charString) ||
                            quranEntity.en_pickthall!!.contains(charString) ||
                            quranEntity.fa_khorramdel!!.contains(charString) ||
                            quranEntity.ku_asan!!.contains(kKurdishToArabicCharacters(charString)) ||
                            quranEntity.ku_asan!!.contains(charString)
                        )
                            f1.add(quranEntity)
                    filteredQuranList = f1
                    val filterResult = FilterResults()
                    filterResult.values = filteredQuranList
                    filter = charString
                    return filterResult
                }

            }
        }

        //#####################################################
        inner class SuraItemViewHolder(private val itemBinding: ItemAyaBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            init {
                itemBinding.ayaItemArabic.typeface = arabicFont
                itemBinding.ayaItemArabic.textSize = arabicFontSize

                itemBinding.ayaItemEnglish.typeface = englishFont
                itemBinding.ayaItemEnglish.textSize = englishFontSize

                itemBinding.ayaItemEnTransilation.typeface = englishFont
                itemBinding.ayaItemEnTransilation.textSize = englishFontSize

                itemBinding.ayaItemKurdish.typeface = kurdishFont
                itemBinding.ayaItemKurdish.textSize = kurdishFontSize

                itemBinding.ayaItemFarsi.typeface = farsiFont
                itemBinding.ayaItemFarsi.textSize = farsiFontSize

            }//end of init items

            @SuppressLint("PrivateResource", "SetTextI18n")
            fun bind(aya: QuranEntity) {
                val description = StringBuilder()
                description.append("${getString(R.string.sura)} ${chapter.nameArabic}  ")
                description.append("${getString(R.string.aya)} ${aya.aya}\n")

                if (aya.aya != 1 || aya.sura == 1 || aya.sura == 9)
                    itemBinding.ayaItemArabic.text = aya.simple
                else {
                    itemBinding.ayaItemArabic.text =
                        "${getString(R.string.str_bismillah)}\n${aya.simple}"
                }

                description.append("${itemBinding.ayaItemArabic.text} \n")

                if (appPrefsLite.getBoolean(PREF_ENGLISH_TRANSLITERATION, false)) {
                    itemBinding.ayaItemEnTransilation.text = aya.en_transilation
                    description.append("${itemBinding.ayaItemEnTransilation.text} \n")
                } else
                    itemBinding.ayaItemEnTransilation.visibility = View.GONE

                if (appPrefsLite.getBoolean(PREF_ENGLISH_TRANSLATE, false)) {
                    itemBinding.ayaItemEnglish.text = aya.en_pickthall
                    description.append("${itemBinding.ayaItemEnglish.text} \n")
                } else
                    itemBinding.ayaItemEnglish.visibility = View.GONE

                if (appPrefsLite.getBoolean(PREF_KURDISH_TRANSLATE, false)) {
                    itemBinding.ayaItemKurdish.text = aya.ku_asan
                    description.append("${itemBinding.ayaItemKurdish.text} \n")
                } else
                    itemBinding.ayaItemKurdish.visibility = View.GONE

                if (appPrefsLite.getBoolean(PREF_FARSI_TRANSLATE, false)) {
                    itemBinding.ayaItemFarsi.text = getFarsi(aya.fa_khorramdel!!)
                    description.append("${itemBinding.ayaItemFarsi.text} \n")
                } else
                    itemBinding.ayaItemFarsi.visibility = View.GONE

                itemBinding.txtAyaNoteLayout.visibility = View.GONE
                if (aya.note == "-") {
                    itemBinding.txtAyaNote.setText("")
                    itemBinding.btnAyaNote.setImageResource(R.drawable.ic_note_add)
                } else {
                    itemBinding.txtAyaNote.setText(aya.note)
                    itemBinding.btnAyaNote.setImageResource(R.drawable.ic_note)
                }

                if (aya.fav == 1)
                    itemBinding.btnAyaBookmark.setImageResource(R.drawable.ic_bookmark1)
                else
                    itemBinding.btnAyaBookmark.setImageResource(R.drawable.ic_bookmark0)

                itemBinding.ayaItemAyaNumber.text = formatNumber(aya.aya.toString())

                //$$$$$$$$$$$$$$$$$$$ filter color
                if (filter.isNotEmpty()) {
                    val strReplace = "<font color='#F44336'>$filter</font>"
                    if (itemBinding.ayaItemArabic.visibility == View.VISIBLE)
                        itemBinding.ayaItemArabic.text =
                            Html.fromHtml(
                                itemBinding.ayaItemArabic.text.toString()
                                    .replace(filter, strReplace)
                            )
                    if (itemBinding.ayaItemEnglish.visibility == View.VISIBLE)
                        itemBinding.ayaItemEnglish.text =
                            Html.fromHtml(
                                itemBinding.ayaItemEnglish.text.toString()
                                    .replace(filter, strReplace)
                            )
                    if (itemBinding.ayaItemEnTransilation.visibility == View.VISIBLE)
                        itemBinding.ayaItemEnTransilation.text =
                            Html.fromHtml(
                                itemBinding.ayaItemEnTransilation.text.toString()
                                    .replace(filter, strReplace)
                            )
                    if (itemBinding.ayaItemKurdish.visibility == View.VISIBLE)
                        itemBinding.ayaItemKurdish.text =
                            Html.fromHtml(
                                itemBinding.ayaItemKurdish.text.toString()
                                    .replace(filter, strReplace)
                            )
                    if (itemBinding.ayaItemFarsi.visibility == View.VISIBLE)
                        itemBinding.ayaItemFarsi.text =
                            Html.fromHtml(
                                itemBinding.ayaItemFarsi.text.toString().replace(filter, strReplace)
                            )
                }

                description.append("\n$appLink")

                itemBinding.btnAyaCopy.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@SuraViewActivity,
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    val clipboardService = getSystemService<ClipboardManager>()
                    clipboardService?.setPrimaryClip(
                        ClipData.newPlainText(
                            getString(R.string.quran),
                            description
                        )
                    )
                    snackMessage(it, getString(R.string.copied))
                }//end of btnCopy

                itemBinding.btnAyaShare.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@SuraViewActivity,
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, description.toString())
                    startActivity(Intent.createChooser(intent, resources.getString(R.string.share)))
                }//end of btnShare

                itemBinding.btnAyaBookmark.setOnClickListener {
                    aya.fav = if (aya.fav == 1) 0 else 1
                    itemBinding.btnAyaBookmark.setImageResource(
                        if (aya.fav == 1)
                            R.drawable.ic_bookmark1
                        else
                            R.drawable.ic_bookmark0
                    )
                    db.quranDao().update(aya)
                }//end of bookmark

                itemBinding.btnAyaNote.setOnClickListener {
                    it as AppCompatImageButton
                    val transient = ChangeBounds().apply {
                        duration = 200
                        interpolator = AccelerateDecelerateInterpolator()
                    }
                    var note: String = itemBinding.txtAyaNote.text!!.toString()
                    Log.d("NAMOO", " ------------------->>  note : $note ")
                    if (note.isEmpty())
                        note = "-"
                    val im: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (itemBinding.txtAyaNoteLayout.visibility == View.GONE) {
                        it.setImageResource(R.drawable.ic_check)
                        itemBinding.txtAyaNoteLayout.visibility = View.VISIBLE
                        itemBinding.txtAyaNote.isFocusable = true
                        itemBinding.txtAyaNote.requestFocus()
                        itemBinding.txtAyaNote.setSelection(itemBinding.txtAyaNote.text.toString().length)
                        im.showSoftInput(itemBinding.txtAyaNote, InputMethodManager.SHOW_IMPLICIT)
                    } else {
                        im.hideSoftInputFromWindow(itemBinding.txtAyaNote.windowToken, 0)
                        aya.note = note
                        if (note == "-")
                            it.setImageResource(R.drawable.ic_note_add)
                        else
                            it.setImageResource(R.drawable.ic_note)
                        itemBinding.txtAyaNoteLayout.visibility = View.GONE
                        db.quranDao().update(aya)
                        snackMessage(it, getString(R.string.note_saved))
                    }
                    TransitionManager.beginDelayedTransition(binding.suraRecycler, transient)
                }//end of note

                itemBinding.txtAyaNoteLayout.setEndIconOnClickListener {
                    val im: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    im.hideSoftInputFromWindow(itemBinding.txtAyaNote.windowToken, 0)
                    itemBinding.txtAyaNote.setText(if (aya.note == "-") "" else aya.note)
                    if (aya.note == "-")
                        itemBinding.btnAyaNote.setImageResource(R.drawable.ic_note_add)
                    else
                        itemBinding.btnAyaNote.setImageResource(R.drawable.ic_note)
                    itemBinding.txtAyaNoteLayout.visibility = View.GONE
                    val transient = ChangeBounds().apply {
                        duration = 200
                        interpolator = AccelerateDecelerateInterpolator()
                    }
                    TransitionManager.beginDelayedTransition(binding.suraRecycler, transient)
                }

                itemBinding.btnAyaPlay.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            this@SuraViewActivity,
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (!isHaveStoragePermission(this@SuraViewActivity)) {
                        askForStoragePermission(this@SuraViewActivity)
                    } else {
                        if ((appPrefsLite.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE) != 3 && !File(
                                getQuranDirectoryInInternal(this@SuraViewActivity) + "/" + appPrefsLite.getString(
                                    PREF_SELECTED_QARI,
                                    DEFAULT_SELECTED_QARI
                                ) + "/" + getAyaFileName(
                                    aya.sura!!, aya.aya!!
                                )
                            ).exists()
                                    &&
                                    !File(
                                        getQuranDirectoryInSD(this@SuraViewActivity) + "/" + appPrefsLite.getString(
                                            PREF_SELECTED_QARI,
                                            DEFAULT_SELECTED_QARI
                                        ) + "/" + getAyaFileName(
                                            aya.sura!!, aya.aya!!
                                        )
                                    ).exists())
                            ||
                            (appPrefsLite.getInt(PREF_PLAY_TYPE, DEFAULT_PLAY_TYPE) == 3 && !File(
                                getQuranDirectoryInInternal(this@SuraViewActivity) + "/" + appPrefsLite.getString(
                                    PREF_TRANSLATE_TO_PLAY,
                                    DEFAULT_TRANSLATE_TO_PLAY
                                ) + "/" + getAyaFileName(
                                    aya.sura!!, aya.aya!!
                                )
                            ).exists()
                                    &&
                                    !File(
                                        getQuranDirectoryInSD(this@SuraViewActivity) + "/" + appPrefsLite.getString(
                                            PREF_TRANSLATE_TO_PLAY,
                                            DEFAULT_TRANSLATE_TO_PLAY
                                        ) + "/" + getAyaFileName(
                                            aya.sura!!, aya.aya!!
                                        )
                                    ).exists())
                        ) {
                            AlertDialog.Builder(this@SuraViewActivity).apply {
                                setTitle(getString(R.string.error))
                                setMessage(getString(R.string.audio_files_error))
                                setPositiveButton(getString(R.string.download)) { _, _ ->
                                    onBackPressed()
                                    sendBroadcast(Intent().apply {
                                        action = ACTION_GO_TO_DOWNLOAD_PAGE
                                        putExtra("sura", sura)
                                        putExtra(
                                            "folder", appPrefsLite.getString(
                                                PREF_SELECTED_QARI,
                                                DEFAULT_SELECTED_QARI
                                            )
                                        )
                                    })
                                }
                                setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                }
                                create()
                                show()
                            }
                        } else if (appPrefsLite.getInt(
                                PREF_PLAY_TYPE,
                                DEFAULT_PLAY_TYPE
                            ) != 2 && !File(
                                getSelectedQuranDirectoryPath(this@SuraViewActivity) + "/" + appPrefsLite.getString(
                                    PREF_TRANSLATE_TO_PLAY,
                                    DEFAULT_TRANSLATE_TO_PLAY
                                ) + "/" + getAyaFileName(
                                    aya.sura!!, aya.aya!!
                                )
                            ).exists()
                        ) {
                            AlertDialog.Builder(this@SuraViewActivity).apply {
                                setTitle(getString(R.string.error))
                                setMessage(getString(R.string.audio_translate_files_error))
                                setPositiveButton(getString(R.string.download)) { _, _ ->
                                    onBackPressed()
                                    sendBroadcast(Intent().apply {
                                        action = ACTION_GO_TO_DOWNLOAD_PAGE
                                        putExtra("sura", sura)
                                        putExtra(
                                            "folder", appPrefsLite.getString(
                                                PREF_TRANSLATE_TO_PLAY,
                                                DEFAULT_TRANSLATE_TO_PLAY
                                            )
                                        )
                                    })
                                }
                                setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                }
                                create()
                                show()
                            }
                        } else {
                            sendBroadcast(Intent().apply {
                                action = QURAN_PLAYER_ACTION
                                putExtra("action", NOTIFY_QURAN_PLAY)
                                putExtra("sura", chapter.sura)
                                putExtra("aya", aya.aya!!)
                            })
                        }
                    }
                }
                appPrefsLite.edit { putInt(PREF_LAST_VISITED_VERSE, aya.index) }
            }//end of bind

            private fun getFarsi(text: String): String {
                var result = ""
                if (!appPrefsLite.getBoolean(PREF_FARSI_FULL_TRANSLATE, false)) {
                    for (c in text) {
                        if (c == ']' || c == '[')
                            break
                        result += c
                    }
                } else
                    result = text
                return result
            }

        }//end of SuraItemViewHolder

    }//end of class SuraViewAdapter

}//end of class SuraViewActivity
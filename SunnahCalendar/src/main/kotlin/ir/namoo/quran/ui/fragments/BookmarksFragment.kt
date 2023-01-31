package ir.namoo.quran.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentBookmarksBinding
import com.byagowi.persiancalendar.databinding.ItemBookmarkBinding
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import ir.namoo.commons.utils.animateVisibility
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import ir.namoo.quran.utils.ACTION_CHANGE_SURA
import ir.namoo.quran.utils.arabicFont
import ir.namoo.quran.utils.englishFont
import ir.namoo.quran.utils.farsiFont
import ir.namoo.quran.utils.kurdishFont
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarksFragment : Fragment() {

    private lateinit var binding: FragmentBookmarksBinding
    private val model: BookmarkViewModel by viewModel()
    private lateinit var chapters: MutableList<ChapterEntity>

    private val db: QuranDB by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarksBinding.inflate(inflater)
        binding.appBar.toolbar.let {
            it.setTitle(R.string.bookmarks)
            it.setupMenuNavigation()
        }
        binding.appBar.root.hideToolbarBottomShadow()
        binding.bookmarkRecycler.visibility = View.GONE
        chapters = db.chaptersDao().getAllChapters()
        model.getBookmarkList().observe(requireActivity()) {
            if (it.size > 0) {
                binding.txtNoBookmarkFound.visibility = View.GONE
                binding.bookmarkRecycler.visibility = View.VISIBLE
                binding.bookmarkRecycler.layoutManager = LinearLayoutManager(requireContext())
                val adapter = BookmarkAdapter()
                binding.bookmarkRecycler.adapter = adapter
                adapter.setBookmarks(it)
                binding.appBar.toolbar.subtitle = formatNumber(it.size)
            } else {
                binding.txtNoBookmarkFound.visibility = View.VISIBLE
                binding.bookmarkRecycler.visibility = View.GONE
                binding.appBar.toolbar.subtitle = ""

            }
        }

        return binding.root
    }//end of onCreateView

    override fun onResume() {
        super.onResume()
        model.update()
    }

    //###############################################################
    inner class BookmarkAdapter :
        RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

        private var bookmarks: MutableList<QuranEntity>? = null
        private var lastExpend: ItemBookmarkBinding? = null
        fun setBookmarks(bookmarks: MutableList<QuranEntity>) {
            this.bookmarks = bookmarks
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder =
            BookmarkViewHolder(
                ItemBookmarkBinding.inflate(layoutInflater, parent, false)
            )

        override fun getItemCount(): Int = if (bookmarks != null) bookmarks!!.size else 0


        override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
            bookmarks?.let {
                holder.bind(it[position], position)
            }
        }


        override fun getItemViewType(position: Int): Int = position

        //#######################
        inner class BookmarkViewHolder(private val itemBinding: ItemBookmarkBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            private var mPosition = 0

            init {
                itemBinding.bookmarkTxtItemArabic.typeface = arabicFont
                itemBinding.bookmarkTxtItemArabic.textSize = 18f
                itemBinding.bookmarkTxtItemEnglish.typeface = englishFont
                itemBinding.bookmarkTxtItemKurdish.typeface = kurdishFont
                itemBinding.bookmarkTxtItemFarsi.typeface = farsiFont

                itemBinding.bookmarkTxtItemEnglish.visibility = View.GONE
                itemBinding.bookmarkTxtItemKurdish.visibility = View.GONE
                itemBinding.bookmarkTxtItemFarsi.visibility = View.GONE
                itemBinding.bookmarkTxtItemArabic.maxLines = 2

            }

            @SuppressLint("PrivateResource")
            fun bind(bookmark: QuranEntity, position: Int) {
                mPosition = position
                itemBinding.bookmarkTxtItemArabic.text = bookmark.simple
                itemBinding.bookmarkTxtItemEnglish.text = bookmark.en_pickthall
                itemBinding.bookmarkTxtItemKurdish.text = bookmark.ku_asan
                itemBinding.bookmarkTxtItemFarsi.text = bookmark.fa_khorramdel

                itemBinding.bookmarkTxtItemSuraAya.text = formatNumber(
                    String.format(
                        itemView.context!!.getString(R.string.search_head),
                        chapters.first { it.sura == bookmark.sura }.nameArabic,
                        bookmark.aya
                    )
                )
                itemBinding.bookmarkBtnGoToAya.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    Intent().apply {
                        action = ACTION_CHANGE_SURA
                        putExtra("sura", bookmark.sura)
                        putExtra("aya", bookmark.aya)
                        it.context!!.sendBroadcast(this)
                    }
                }
                itemBinding.btnBookmarkExpand.setOnClickListener {
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
                itemBinding.bookmarkBtnBookmark.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    bookmark.fav = 0
                    db.quranDao().update(bookmark)
                    bookmarks?.remove(bookmark)
                    notifyItemRemoved(position)
                    binding.appBar.toolbar.subtitle =
                        if (bookmarks!!.size > 0) formatNumber(bookmarks!!.size) else ""
                    if (bookmarks!!.size < 1)
                        animateVisibility(binding.txtNoBookmarkFound, true)
                }
            }//end of bind

            @SuppressLint("PrivateResource")
            fun expand() {
                val arrowRotationAnimationDuration =
                    resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

                if (itemBinding.bookmarkTxtItemEnglish.visibility == View.VISIBLE) {
                    itemBinding.btnBookmarkExpand.animate()
                        .rotation(0f)
                        .setDuration(arrowRotationAnimationDuration)
                        .start()
                    itemBinding.bookmarkTxtItemEnglish.visibility = View.GONE
                    itemBinding.bookmarkTxtItemKurdish.visibility = View.GONE
                    itemBinding.bookmarkTxtItemFarsi.visibility = View.GONE
                    itemBinding.bookmarkTxtItemArabic.maxLines = 2
                    lastExpend = null
                } else {
                    if (lastExpend != null && lastExpend != itemBinding) {
                        lastExpend?.let {
                            it.btnBookmarkExpand.animate()
                                .rotation(0f)
                                .setDuration(arrowRotationAnimationDuration)
                                .start()
                            it.bookmarkTxtItemEnglish.visibility = View.GONE
                            it.bookmarkTxtItemKurdish.visibility = View.GONE
                            it.bookmarkTxtItemFarsi.visibility = View.GONE
                            it.bookmarkTxtItemArabic.maxLines = 2
                        }
                    }
                    itemBinding.btnBookmarkExpand.animate()
                        .rotation(180f)
                        .setDuration(arrowRotationAnimationDuration)
                        .start()
                    itemBinding.bookmarkTxtItemEnglish.visibility = View.VISIBLE
                    itemBinding.bookmarkTxtItemKurdish.visibility = View.VISIBLE
                    itemBinding.bookmarkTxtItemFarsi.visibility = View.VISIBLE
                    itemBinding.bookmarkTxtItemArabic.maxLines = 10
                    lastExpend = itemBinding
                    (binding.bookmarkRecycler.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        mPosition,
                        0
                    )
                }
                val transition = ChangeBounds()
                transition.interpolator = AnticipateOvershootInterpolator()
                TransitionManager.beginDelayedTransition(binding.bookmarkRecycler, transition)
            }
        }
    }
}//end of class

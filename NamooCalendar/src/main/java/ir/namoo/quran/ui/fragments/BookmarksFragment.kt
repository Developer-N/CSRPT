package ir.namoo.quran.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import ir.namoo.quran.ui.QuranActivity
import ir.namoo.quran.utils.*
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentBookmarksBinding
import ir.namoo.religiousprayers.databinding.ItemBookmarkBinding
import ir.namoo.religiousprayers.utils.animateVisibility
import ir.namoo.religiousprayers.utils.formatNumber

class BookmarksFragment : Fragment() {

    private lateinit var binding: FragmentBookmarksBinding
    private val model: BookmarkViewModel by viewModels()
    private lateinit var chapters: MutableList<ChapterEntity>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookmarksBinding.inflate(inflater)
        setHasOptionsMenu(true)
        binding.bookmarkRecycler.visibility = View.GONE
        chapters =
            QuranDB.getInstance(requireContext().applicationContext).chaptersDao().getAllChapters()
        model.getBookmarkList().observe(requireActivity(), {
            if (it.size > 0) {
                binding.txtNoBookmarkFound.visibility = View.GONE
                binding.bookmarkRecycler.visibility = View.VISIBLE
                binding.bookmarkRecycler.layoutManager = LinearLayoutManager(requireContext())
                val adapter = BookmarkAdapter()
                binding.bookmarkRecycler.adapter = adapter
                adapter.setBookmarks(it)
                (requireActivity() as QuranActivity).setTitleAndSubtitle(
                    resources.getString(R.string.bookmarks),
                    formatNumber(it.size)
                )
            } else {
                binding.txtNoBookmarkFound.visibility = View.VISIBLE
                binding.bookmarkRecycler.visibility = View.GONE
                (requireActivity() as QuranActivity).setTitleAndSubtitle(
                    resources.getString(R.string.bookmarks),
                    ""
                )
            }
        })

        return binding.root
    }//end of onCreateView

    override fun onResume() {
        super.onResume()
        model.update(requireContext())
    }

    //###############################################################
    inner class BookmarkAdapter :
        RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

        private var bookmarks: MutableList<QuranEntity>? = null

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
                    QuranDB.getInstance(requireContext().applicationContext).quranDao()
                        .update(bookmark)
                    bookmarks?.remove(bookmark)
                    notifyItemRemoved(position)
                    (requireActivity() as QuranActivity).setTitleAndSubtitle(
                        resources.getString(R.string.bookmarks),
                        if (bookmarks!!.size > 0) formatNumber(bookmarks!!.size) else ""
                    )
                    if (bookmarks!!.size < 1)
                        animateVisibility(binding.txtNoBookmarkFound, true)
                }


            }//end of bind

            @SuppressLint("PrivateResource")
            fun expand() {
                val arrowRotationAnimationDuration =
                    resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                val transition = ChangeBounds()
                transition.duration = 300
                if (itemBinding.bookmarkTxtItemEnglish.visibility == View.VISIBLE) {
                    itemBinding.btnBookmarkExpand.animate()
                        .rotation(0f)
                        .setDuration(arrowRotationAnimationDuration)
                        .start()
                    itemBinding.bookmarkTxtItemEnglish.visibility = View.GONE
                    itemBinding.bookmarkTxtItemKurdish.visibility = View.GONE
                    itemBinding.bookmarkTxtItemFarsi.visibility = View.GONE
                    itemBinding.bookmarkTxtItemArabic.maxLines = 2
                } else {
                    itemBinding.btnBookmarkExpand.animate()
                        .rotation(180f)
                        .setDuration(arrowRotationAnimationDuration)
                        .start()
                    itemBinding.bookmarkTxtItemEnglish.visibility = View.VISIBLE
                    itemBinding.bookmarkTxtItemKurdish.visibility = View.VISIBLE
                    itemBinding.bookmarkTxtItemFarsi.visibility = View.VISIBLE
                    itemBinding.bookmarkTxtItemArabic.maxLines = 10
                }
                TransitionManager.beginDelayedTransition(binding.bookmarkRecycler, transition)
            }
        }
    }
}//end of class
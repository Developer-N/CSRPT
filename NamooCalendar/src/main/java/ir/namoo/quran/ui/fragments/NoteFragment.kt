package ir.namoo.quran.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import ir.namoo.quran.ui.QuranActivity
import ir.namoo.quran.utils.ACTION_CHANGE_SURA
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.FragmentNoteBinding
import ir.namoo.religiousprayers.databinding.ItemNoteBinding
import ir.namoo.religiousprayers.utils.animateVisibility
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.snackMessage

class NoteFragment : Fragment() {

    private lateinit var binding: FragmentNoteBinding
    private val model: NoteViewModel by viewModels()
    private lateinit var chapters: MutableList<ChapterEntity>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoteBinding.inflate(inflater)
        setHasOptionsMenu(true)
        binding.noteRecycler.visibility = View.GONE
        chapters =
            QuranDB.getInstance(requireContext().applicationContext).chaptersDao().getAllChapters()
        model.getNoteList().observe(requireActivity(), {
            if (it.size > 0) {
                binding.noteTxtNoNote.visibility = View.GONE
                binding.noteRecycler.visibility = View.VISIBLE
                binding.noteRecycler.layoutManager = LinearLayoutManager(requireContext())
                val adapter = NoteAdapter()
                adapter.setData(it)
                binding.noteRecycler.adapter = adapter
                (requireActivity() as QuranActivity).setTitleAndSubtitle(
                    resources.getString(R.string.notes),
                    formatNumber(it.size)
                )
            } else {
                binding.noteTxtNoNote.visibility = View.VISIBLE
                binding.noteRecycler.visibility = View.GONE
                (requireActivity() as QuranActivity).setTitleAndSubtitle(
                    resources.getString(R.string.notes),
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

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    inner class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        private var noteList: MutableList<QuranEntity>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder =
            NoteViewHolder(ItemNoteBinding.inflate(layoutInflater, parent, false))


        override fun getItemCount(): Int = try {
            noteList!!.size
        } catch (ex: Exception) {
            0
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            noteList?.let {
                holder.bind(it[position], position)
            }
        }


        override fun getItemViewType(position: Int): Int = position
        fun setData(notes: MutableList<QuranEntity>) {
            this.noteList = notes
            notifyDataSetChanged()
        }

        //@@@@@@@@@@@@@@@@@@
        inner class NoteViewHolder(private val itemBinding: ItemNoteBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            init {
                itemBinding.noteBtnSave.isEnabled = false
            }

            @SuppressLint("PrivateResource")
            fun bind(note: QuranEntity, position: Int) {
                itemBinding.noteTxtNote.setText(note.note!!)
                itemBinding.noteTxtHead.text = formatNumber(
                    String.format(
                        itemView.context!!.getString(R.string.search_head),
                        chapters.first { it.sura == note.sura }.nameArabic,
                        note.aya
                    )
                )
                itemBinding.noteTxtNote.doOnTextChanged { _, _, _, _ ->
                    itemBinding.noteBtnSave.isEnabled = true
                }
                itemBinding.noteBtnGoToAya.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    Intent().apply {
                        action = ACTION_CHANGE_SURA
                        putExtra("sura", note.sura)
                        putExtra("aya", note.aya)
                        it.context!!.sendBroadcast(this)
                    }
                }
                itemBinding.noteBtnSave.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    AlertDialog.Builder(it.context).apply {
                        setTitle(it.context.getString(R.string.alert))
                        setMessage(it.context.getString(R.string.save_alert_message))
                        setNegativeButton(R.string.no) { dialog, _ ->
                            run {
                                dialog.cancel()
                            }
                        }
                        setPositiveButton(R.string.yes) { _, _ ->
                            note.note = itemBinding.noteTxtNote.text!!.toString()
                            QuranDB.getInstance(requireContext().applicationContext).quranDao()
                                .update(note)
                            snackMessage(it, getString(R.string.saved))
                        }
                        create().show()
                    }
                }

                itemBinding.noteBtnDelete.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    AlertDialog.Builder(it.context).apply {
                        setTitle(it.context.getString(R.string.alert))
                        setMessage(it.context.getString(R.string.delete_alert_message))
                        setNegativeButton(R.string.no) { dialog, _ ->
                            run {
                                dialog.cancel()
                            }
                        }
                        setPositiveButton(R.string.yes) { _, _ ->
                            note.note = "-"
                            QuranDB.getInstance(requireContext().applicationContext).quranDao()
                                .update(note)
                            noteList?.remove(note)
                            (requireActivity() as QuranActivity).setTitleAndSubtitle(
                                resources.getString(R.string.notes),
                                if (noteList!!.size > 0) formatNumber(noteList!!.size) else ""
                            )
                            if (noteList!!.size < 1)
                                animateVisibility(binding.noteTxtNoNote, true)
                            notifyItemRemoved(position)
                        }
                        create().show()
                    }

                }

            }//end of bind
        }//end of class NoteViewHolder

    }//end of class NoteAdapter

}//end of class
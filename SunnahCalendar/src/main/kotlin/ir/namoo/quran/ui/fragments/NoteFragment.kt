package ir.namoo.quran.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentNoteBinding
import com.byagowi.persiancalendar.databinding.ItemNoteBinding
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import ir.namoo.commons.utils.animateVisibility
import ir.namoo.commons.utils.snackMessage
import ir.namoo.quran.db.ChapterEntity
import ir.namoo.quran.db.QuranDB
import ir.namoo.quran.db.QuranEntity
import ir.namoo.quran.utils.ACTION_CHANGE_SURA
import javax.inject.Inject

@AndroidEntryPoint
class NoteFragment : Fragment() {

    private lateinit var binding: FragmentNoteBinding
    private val model: NoteViewModel by viewModels()
    private lateinit var chapters: MutableList<ChapterEntity>

    @Inject
    lateinit var db: QuranDB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteBinding.inflate(inflater)
        binding.appBar.toolbar.let {
            it.setTitle(R.string.notes)
            it.setupMenuNavigation()
        }
        binding.appBar.root.hideToolbarBottomShadow()

        binding.noteRecycler.visibility = View.GONE
        chapters = db.chaptersDao().getAllChapters()
        model.getNoteList().observe(requireActivity()) {
            if (it.size > 0) {
                binding.noteTxtNoNote.visibility = View.GONE
                binding.noteRecycler.visibility = View.VISIBLE
                binding.noteRecycler.layoutManager = LinearLayoutManager(requireContext())
                val adapter = NoteAdapter()
                adapter.setData(it)
                binding.noteRecycler.adapter = adapter
                binding.appBar.toolbar.subtitle = formatNumber(it.size)

            } else {
                binding.noteTxtNoNote.visibility = View.VISIBLE
                binding.noteRecycler.visibility = View.GONE
                binding.appBar.toolbar.subtitle = ""

            }
        }
        return binding.root
    }//end of onCreateView

    override fun onResume() {
        super.onResume()
        model.update()
    }

    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    inner class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

        private var noteList: MutableList<QuranEntity>? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder =
            NoteViewHolder(ItemNoteBinding.inflate(layoutInflater, parent, false))


        override fun getItemCount(): Int = runCatching {
            noteList!!.size
        }.onFailure(logException).getOrDefault(0)

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            noteList?.let {
                holder.bind(it[position], position)
            }
        }


        override fun getItemViewType(position: Int): Int = position

        @SuppressLint("NotifyDataSetChanged")
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
                    MaterialAlertDialogBuilder(it.context).apply {
                        setTitle(it.context.getString(R.string.alert))
                        setMessage(it.context.getString(R.string.save_alert_message))
                        setNegativeButton(R.string.no) { dialog, _ ->
                            run {
                                dialog.cancel()
                            }
                        }
                        setPositiveButton(R.string.yes) { _, _ ->
                            note.note = itemBinding.noteTxtNote.text!!.toString()
                            db.quranDao()
                                .update(note)
                            snackMessage(it, getString(R.string.saved))
                        }
                        show()
                    }
                }

                itemBinding.noteBtnDelete.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    MaterialAlertDialogBuilder(it.context).apply {
                        setTitle(it.context.getString(R.string.alert))
                        setMessage(it.context.getString(R.string.delete_alert_message))
                        setNegativeButton(R.string.no) { dialog, _ ->
                            run {
                                dialog.cancel()
                            }
                        }
                        setPositiveButton(R.string.yes) { _, _ ->
                            note.note = "-"
                            db.quranDao()
                                .update(note)
                            noteList?.remove(note)
                            binding.appBar.toolbar.subtitle =
                                if (noteList!!.size > 0) formatNumber(noteList!!.size) else ""

                            if (noteList!!.size < 1)
                                animateVisibility(binding.noteTxtNoNote, true)
                            notifyItemRemoved(position)
                        }
                        show()
                    }

                }

            }//end of bind
        }//end of class NoteViewHolder

    }//end of class NoteAdapter

}//end of class

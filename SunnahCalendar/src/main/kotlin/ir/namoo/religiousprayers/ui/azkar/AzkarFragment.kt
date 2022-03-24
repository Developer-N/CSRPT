package ir.namoo.religiousprayers.ui.azkar

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentAzkarBinding
import com.byagowi.persiancalendar.databinding.ItemAzkarBinding
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.logException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("SdCardPath")
class AzkarFragment : Fragment() {

    private lateinit var binding: FragmentAzkarBinding
    private val viewModel: AzkarViewModel by viewModels()
    private var isBookmarkShown = false
    private val azkarAdapter = AzkarAdapter()

    @Inject
    lateinit var azkarDB: AzkarDB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAzkarBinding.inflate(inflater, container, false)

        binding.appBar.toolbar.let { toolbar ->
            toolbar.setTitle(R.string.azkar)
            toolbar.setSubtitle(R.string.hisnulmuslim)
            toolbar.setupMenuNavigation()
            toolbar.menu.add(R.string.search).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                it.actionView = SearchView(toolbar.context).also { searchView ->
                    searchView.findViewById<LinearLayout>(androidx.appcompat.R.id.search_bar)
                        .layoutTransition = LayoutTransition()
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            (binding.recyclerAzkarTitle.adapter as AzkarAdapter)
                                .filter.filter(newText)
                            return true
                        }
                    })
                }
            }
            toolbar.menu.add(R.string.favorite).also {
                it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_favorite_border)
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                it.onClick {
                    if (isBookmarkShown) {
                        (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter("")
                        it.setIcon(R.drawable.ic_favorite_border)
                    } else {
                        (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter("fav1")
                        it.setIcon(R.drawable.ic_favorite)
                    }
                    isBookmarkShown = !isBookmarkShown
                }
            }
        }
        binding.appBar.root.hideToolbarBottomShadow()

        if (!File("/data/data/" + requireContext().packageName.toString() + "/databases/azkar.db").exists())
            createDB()
        else if (azkarDB.azkarsDAO().getAllAzkar().isNullOrEmpty()) {
            File("/data/data/" + requireContext().packageName.toString() + "/databases/azkar.db").delete()
            createDB()
        } else
            loadAzkars()

        return binding.root
    }//end of onCreateView

    private fun loadAzkars() {
        binding.recyclerAzkarTitle.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAzkarTitle.adapter = azkarAdapter
        viewModel.azkarTitles.observe(viewLifecycleOwner) {
            azkarAdapter.updateAzkars(it)
        }
        viewModel.loadAzkarTitles()
    }

    private fun createDB() {
        lifecycleScope.launch {
            var isRun: Boolean
            isRun = copyDB()
            if (isRun)
                isRun = unzip()
            if (isRun)
                withContext(Dispatchers.Main) { loadAzkars() }
        }
    }

    private fun copyDB(): Boolean = runCatching {
        val dis = File("/data/data/${requireContext().packageName}/databases")
        if (!dis.exists())
            dis.mkdir()
        val outPutFile =
            File("/data/data/${requireContext().packageName}/databases/azkar.zip")
        val fileOutputStream = FileOutputStream(outPutFile)
        requireContext().assets.open("azkar.zip").copyTo(fileOutputStream)
        fileOutputStream.close()
        true
    }.onFailure(logException).getOrDefault(false)


    private fun unzip(): Boolean = runCatching {
        ZipFile(
            "/data/data/${requireContext().packageName}/databases/azkar.zip",
            ("@zKa6").toCharArray()
        ).extractAll("/data/data/${requireContext().packageName}/databases/")
        File("/data/data/${requireContext().packageName}/databases/azkar.zip").delete()
        true
    }.onFailure(logException).getOrDefault(false)

    //#################################################################
    private inner class AzkarAdapter :
        RecyclerView.Adapter<AzkarAdapter.AV>(), Filterable {
        private var filter: String
        private var filteredAzkarList: MutableList<AzkarTitles>
        private var titleList: List<AzkarTitles>

        init {
            filter = ""
            filteredAzkarList = arrayListOf()
            titleList = arrayListOf()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateAzkars(azkarList: List<AzkarTitles>) {
            titleList = azkarList
            filteredAzkarList.addAll(titleList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AV =
            AV(ItemAzkarBinding.inflate(parent.context.layoutInflater, parent, false))


        override fun getItemCount(): Int = filteredAzkarList.size

        override fun getItemViewType(position: Int): Int = position

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    if (constraint.toString()
                            .isEmpty() || constraint.toString() == "" || constraint.toString() == "fav0"
                    ) {
                        filter = ""
                        filteredAzkarList.clear()
                        filteredAzkarList.addAll(titleList)
                    } else if (constraint.toString() == "fav1") {
                        filter = ""
                        filteredAzkarList.clear()
                        filteredAzkarList.addAll(titleList.filter { it.fav == 1 })
                    } else {
                        filter = constraint.toString()
                        filteredAzkarList.clear()
                        for (t in titleList) {
                            if (language == Language.FA) {
                                if ((!t.title_fa.isNullOrEmpty() && t.title_fa!!.contains(constraint.toString())))
                                    filteredAzkarList.add(t)
                            } else if (language == Language.CKB) {
                                if ((!t.title_ku.isNullOrEmpty() && t.title_ku!!.contains(constraint.toString())))
                                    filteredAzkarList.add(t)
                            } else if (language == Language.EN_US) {
                                if ((!t.title_en.isNullOrEmpty() && t.title_en!!.contains(constraint.toString())))
                                    filteredAzkarList.add(t)
                            }
                        }
                    }
                    val results = FilterResults()
                    results.values = filteredAzkarList
                    return results
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredAzkarList = results!!.values as MutableList<AzkarTitles>
                    notifyDataSetChanged()
                }

            }
        }

        @SuppressLint("PrivateResource")
        override fun onBindViewHolder(holder: AV, position: Int) {
            holder.bind(position, filteredAzkarList[position], filter)
            val scaleDownX =
                ObjectAnimator.ofFloat(holder.itemBinding.root, "scaleX", 0.8f, 1f).apply {
                    duration = 250
                    interpolator = AnticipateOvershootInterpolator()
                }
            val scaleDownY =
                ObjectAnimator.ofFloat(holder.itemBinding.root, "scaleY", 0.8f, 1f).apply {
                    duration = 250
                    interpolator = AnticipateOvershootInterpolator()
                }
            val scaleDown = AnimatorSet()
            scaleDown.play(scaleDownX).with(scaleDownY)
            scaleDownX.addUpdateListener {
                holder.itemBinding.root.invalidate()
            }
            scaleDown.start()
        }

        private inner class AV(val itemBinding: ItemAzkarBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {

            @SuppressLint("SetTextI18n")
            fun bind(
                position: Int,
                title: AzkarTitles,
                filter: String
            ) {
                itemBinding.root.setOnClickListener {
                    val intent = Intent(it.context.applicationContext, AzkarActivity::class.java)
                    intent.putExtra("id", title.id)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptions
                            .makeSceneTransitionAnimation(
                                requireActivity(), itemBinding.root, "shared_element_container"
                            )
                        requireActivity().startActivity(intent, options.toBundle())
                    } else
                        it.context.startActivity(intent)
                }
                itemBinding.btnAzkarFav.setOnClickListener {
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            it.context,
                            com.google.android.material.R.anim.abc_fade_in
                        )
                    )
                    if (isBookmarkShown) {
                        title.fav = 0
                        filteredAzkarList.remove(title)
                        notifyItemRemoved(position)
                    } else {
                        title.fav = if (title.fav == 1) 0 else 1
                        notifyItemChanged(position)
                    }
                    viewModel.updateAzkarTitle(title)

                }
                itemBinding.txtAzkarTitle.text =
                    when (language) {
                        Language.FA -> "${formatNumber(position + 1)} : ${title.title_fa}"
                        Language.CKB -> "${formatNumber(position + 1)} : ${title.title_ku}"
                        else -> "${formatNumber(position + 1)} : ${title.title_en}"
                    }
                if (title.fav == 1)
                    itemBinding.btnAzkarFav.setImageResource(R.drawable.ic_favorite)
                else
                    itemBinding.btnAzkarFav.setImageResource(R.drawable.ic_favorite_border)
                if (filter.isNotEmpty()) {
                    runCatching {
                        val fColorSpan = ForegroundColorSpan(
                            itemView.context.resolveColor(R.attr.colorHoliday)
                        )
                        val spannableStringBuilder =
                            SpannableStringBuilder(itemBinding.txtAzkarTitle.text)
                        spannableStringBuilder.setSpan(
                            fColorSpan,
                            spannableStringBuilder.indexOf(filter),
                            spannableStringBuilder.indexOf(filter) + filter.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        itemBinding.txtAzkarTitle.text = spannableStringBuilder
                    }.onFailure(logException)
                }
            }
        }
    }

}//end of AzkarFragment

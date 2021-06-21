package ir.namoo.religiousprayers.ui.azkar

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.AzkarItemBinding
import ir.namoo.religiousprayers.databinding.FragmentAzkarBinding
import ir.namoo.religiousprayers.utils.*
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream


class AzkarFragment : Fragment() {

    private lateinit var binding: FragmentAzkarBinding
    private var isBookmarkShown = false
    private lateinit var azkarDB: AzkarDB
    private val azkarAdapter = AzkarAdapter()

    @SuppressLint("SdCardPath")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAzkarBinding.inflate(inflater, container, false).apply {
            appBar.toolbar.let {
                it.setTitle(R.string.azkar)
                it.subtitle = getString(R.string.hisnulmuslim)
                it.setupUpNavigation()
            }
        }
        azkarDB = AzkarDB.getInstance(requireContext().applicationContext)
        binding.recyclerAzkarTitle.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAzkarTitle.adapter = azkarAdapter

        if (!File("/data/data/" + requireContext().packageName.toString() + "/databases/azkar.db").exists())
            FirstCreateDB().execute()
        else
            azkarAdapter.updateAzkars()

        binding.appBar.let {
            it.toolbar.inflateMenu(R.menu.azkar_menu)
            it.toolbar.setOnMenuItemClickListener { clickedMenuItem ->
                when (clickedMenuItem?.itemId) {
                    R.id.mnu_azkar_fav -> {
                        if (isBookmarkShown) {
                            (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter("")
                            clickedMenuItem.setIcon(R.drawable.ic_favorite_border)
                        } else {
                            (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter("fav1")
                            clickedMenuItem.setIcon(R.drawable.ic_favorite)
                        }
                        isBookmarkShown = !isBookmarkShown
                    }
                }
                true
            }

            val searchView: SearchView =
                it.toolbar.menu.findItem(R.id.mnu_azkar_search).actionView as SearchView
            val searchBar: LinearLayout = searchView.findViewById(R.id.search_bar)
            searchBar.layoutTransition = LayoutTransition()
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter(newText)
                    return true
                }
            })
        }

        return binding.root
    }

    //#################################################################
    private inner class AzkarAdapter :
        RecyclerView.Adapter<AzkarAdapter.AV>(), Filterable {
        private var filter: String
        private var filteredAzkarList: MutableList<AzkarTitels>
        private var titleList: List<AzkarTitels>

        init {
            filter = ""
            filteredAzkarList = arrayListOf()
            titleList = arrayListOf()
        }

        fun updateAzkars() {
            titleList = azkarDB.azkarsDAO().getAzkarTitleFor()
            filteredAzkarList.addAll(titleList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AV =
            AV(AzkarItemBinding.inflate(parent.context.layoutInflater, parent, false))


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
                            if (language == "fa") {
                                if ((!t.title_fa.isNullOrEmpty() && t.title_fa!!.contains(constraint.toString())))
                                    filteredAzkarList.add(t)
                            } else if (language == "ckb") {
                                if ((!t.title_ku.isNullOrEmpty() && t.title_ku!!.contains(constraint.toString())))
                                    filteredAzkarList.add(t)
                            } else if (language == "en-US") {
                                if ((!t.title_en.isNullOrEmpty() && t.title_en!!.contains(constraint.toString())))
                                    filteredAzkarList.add(t)
                            }
                        }
                    }
                    val results = FilterResults()
                    results.values = filteredAzkarList
                    return results
                }

                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    filteredAzkarList = results!!.values as MutableList<AzkarTitels>
                    notifyDataSetChanged()
                }

            }
        }

        @SuppressLint("PrivateResource")
        override fun onBindViewHolder(holder: AV, position: Int) {
            holder.bind(position, filteredAzkarList[position], filter)
            val scaleDownX =
                ObjectAnimator.ofFloat(holder.itemBinding.root, "scaleX", 0.7f, 1f).apply {
                    duration = 250
                    interpolator = AnticipateOvershootInterpolator()
                }
            val scaleDownY =
                ObjectAnimator.ofFloat(holder.itemBinding.root, "scaleY", 0.7f, 1f).apply {
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

        private inner class AV(val itemBinding: AzkarItemBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            init {
                itemBinding.txtAzkarTitle.typeface = getAppFont(itemBinding.root.context)
            }

            @SuppressLint("SetTextI18n")
            fun bind(
                position: Int,
                title: AzkarTitels,
                filter: String
            ) {
                itemBinding.root.setOnClickListener {
                    val intent = Intent(it.context.applicationContext, AzkarActivity::class.java)
                    intent.putExtra("id", title.id)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptions
                            .makeSceneTransitionAnimation(
                                activity,
                                itemBinding.txtAzkarTitle,
                                "azkarT"
                            )
                        it.context.startActivity(intent, options.toBundle())
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
                    azkarDB.azkarsDAO().updateAzkarTitle(title)

                }
                itemBinding.txtAzkarTitle.text =
                    when (language) {
                        "fa" -> "${formatNumber(position + 1)} : ${title.title_fa}"
                        "ckb" -> "${formatNumber(position + 1)} : ${title.title_ku}"
                        else -> "${formatNumber(position + 1)} : ${title.title_en}"
                    }
                if (title.fav == 1)
                    itemBinding.btnAzkarFav.setImageResource(R.drawable.ic_favorite)
                else
                    itemBinding.btnAzkarFav.setImageResource(R.drawable.ic_favorite_border)
                if (filter.isNotEmpty()) {
                    runCatching {
                        val fColorSpan = ForegroundColorSpan(
                                itemView.context.resolveColor( R.attr.colorHighlight)
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

    @SuppressLint("StaticFieldLeak")
    private inner class FirstCreateDB() : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String {
            var result = "Success!!!"
            if (!copyDB(requireContext()))
                result = "DB Copy ERROR!!!"
            if (!unzip(requireContext()))
                result = "UNZIP Copy ERROR!!!"
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d("NAMOO", result!!)
            azkarAdapter.updateAzkars()
        }

        @SuppressLint("SdCardPath")
        private fun copyDB(context: Context): Boolean = runCatching {
            val dis = File("/data/data/${context.packageName}/databases")
            if (!dis.exists())
                dis.mkdir()
            val outPutFile =
                File("/data/data/${context.packageName}/databases/azkar.zip")
            val fileOutputStream = FileOutputStream(outPutFile)
            context.assets.open("azkar.zip").copyTo(fileOutputStream)
            fileOutputStream.close()
            true
        }.onFailure(logException).getOrDefault(false)


        @SuppressLint("SdCardPath")
        private fun unzip(context: Context): Boolean = runCatching {

            ZipFile(
                "/data/data/${context.packageName}/databases/azkar.zip",
                ("@zKa6").toCharArray()
            ).extractAll("/data/data/${context.packageName}/databases/")
            File("/data/data/${context.packageName}/databases/azkar.zip").delete()
            true
        }.onFailure(logException).getOrDefault(false)
    }
}//end of class
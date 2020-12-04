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
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.AzkarItemBinding
import ir.namoo.religiousprayers.databinding.FragmentAzkarBinding
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.*
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.FileOutputStream


class AzkarFragment : Fragment() {

    private lateinit var binding: FragmentAzkarBinding
    private var isBookmarkShown = false
    private var menu: Menu? = null

    @SuppressLint("SdCardPath")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAzkarBinding.inflate(inflater, container, false)
        (activity as MainActivity).setTitleAndSubtitle(
            getString(R.string.azkar),
            getString(R.string.hisnulmuslim)
        )
        setHasOptionsMenu(true)
        val db =
            File("/data/data/" + requireContext().packageName.toString() + "/databases/azkar.db")
        val dbExists: Boolean = db.exists()
        if (!dbExists) {
            FirstCreateDB(binding, requireContext(), activity as MainActivity).execute()
        } else {
            binding.recyclerAzkarTitle.layoutManager = LinearLayoutManager(context)
            val adb = AzkarDB.getInstance(requireContext())
            val titleList = adb.azkarsDAO().getAzkarTitleFor()
            binding.recyclerAzkarTitle.adapter = AzkarAdapter(titleList, activity as MainActivity)
        }

        return binding.root

    }

    private fun unBookmark() {
        if (isBookmarkShown && menu != null) {
            (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter("")
            menu!!.getItem(0).setIcon(R.drawable.ic_favorite_border)
            isBookmarkShown = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.azkar_menu, menu)
        this.menu = menu
        val searchView: SearchView =
            menu.findItem(R.id.mnu_azkar_search).actionView as SearchView
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_azkar_fav -> {
                if (isBookmarkShown) {
                    (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter("")
                    item.setIcon(R.drawable.ic_favorite_border)
                } else {
                    (binding.recyclerAzkarTitle.adapter as AzkarAdapter).filter.filter("fav1")
                    item.setIcon(R.drawable.ic_favorite)
                }
                isBookmarkShown = !isBookmarkShown
                return true
            }
            else -> super.onContextItemSelected(item)
        }

    }

    //#################################################################
    private class AzkarAdapter(
        private val azkarTitels: List<AzkarTitels>,
        private val activity: AppCompatActivity
    ) :
        RecyclerView.Adapter<AzkarAdapter.AV>(), Filterable {
        private var filter: String
        private var filteredAzkarList: MutableList<AzkarTitels>

        init {
            filter = ""
            filteredAzkarList = arrayListOf()
            filteredAzkarList.addAll(azkarTitels)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AV {
            return AV(AzkarItemBinding.inflate(parent.context.layoutInflater, parent, false))
        }

        override fun getItemCount(): Int {
            return filteredAzkarList.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    if (constraint.toString()
                            .isEmpty() || constraint.toString() == "" || constraint.toString() == "fav0"
                    ) {
                        filter = ""
                        filteredAzkarList.clear()
                        filteredAzkarList.addAll(azkarTitels)
                    } else if (constraint.toString() == "fav1") {
                        filter = ""
                        filteredAzkarList.clear()
                        filteredAzkarList.addAll(azkarTitels.filter { it.fav == 1 })
                    } else {
                        filter = constraint.toString()
                        filteredAzkarList.clear()
                        for (t in azkarTitels) {
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
            holder.bind(position, filteredAzkarList[position], filter, activity)
            holder.binding.btnAzkarFav.setOnClickListener {
                it.startAnimation(
                    AnimationUtils.loadAnimation(
                        it.context,
                        com.google.android.material.R.anim.abc_fade_in
                    )
                )
                val title = filteredAzkarList[position]
                title.fav = if (title.fav == 1) 0 else 1
                AzkarDB.getInstance(it.context).azkarsDAO().updateAzkarTitle(title)
                notifyItemChanged(position)
            }
            val scaleDownX = ObjectAnimator.ofFloat(holder.binding.root, "scaleX", 0.7f, 1f).apply {
                duration = 250
                interpolator = AnticipateOvershootInterpolator()
            }
            val scaleDownY = ObjectAnimator.ofFloat(holder.binding.root, "scaleY", 0.7f, 1f).apply {
                duration = 250
                interpolator = AnticipateOvershootInterpolator()
            }
            val scaleDown = AnimatorSet()
            scaleDown.play(scaleDownX).with(scaleDownY)
            scaleDownX.addUpdateListener {
                holder.binding.root.invalidate()
            }
            scaleDown.start()
        }

        private class AV(val binding: AzkarItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.txtAzkarTitle.typeface = getAppFont(binding.root.context)
            }

            @SuppressLint("SetTextI18n")
            fun bind(
                position: Int,
                title: AzkarTitels,
                filter: String,
                activity: AppCompatActivity
            ) {
                binding.root.setOnClickListener {
                    val intent = Intent(it.context.applicationContext, AzkarActivity::class.java)
                    intent.putExtra("id", title.id)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val options = ActivityOptions
                            .makeSceneTransitionAnimation(activity, binding.txtAzkarTitle, "azkarT")
                        it.context.startActivity(intent, options.toBundle())
                    } else
                        it.context.startActivity(intent)
                }
                binding.txtAzkarTitle.text =
                    when (language) {
                        "fa" -> "${formatNumber(position + 1)} : ${title.title_fa}"
                        "ckb" -> "${formatNumber(position + 1)} : ${title.title_ku}"
                        else -> "${formatNumber(position + 1)} : ${title.title_en}"
                    }
                if (title.fav == 1)
                    binding.btnAzkarFav.setImageResource(R.drawable.ic_favorite)
                else
                    binding.btnAzkarFav.setImageResource(R.drawable.ic_favorite_border)
                if (filter.isNotEmpty()) {
                    try {
                        val fColorSpan = ForegroundColorSpan(
                            getColorFromAttr(
                                itemView.context,
                                R.attr.colorHighlight
                            )
                        )
                        val spannableStringBuilder =
                            SpannableStringBuilder(binding.txtAzkarTitle.text)
                        spannableStringBuilder.setSpan(
                            fColorSpan,
                            spannableStringBuilder.indexOf(filter),
                            spannableStringBuilder.indexOf(filter) + filter.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.txtAzkarTitle.text = spannableStringBuilder
                    } catch (ex: Exception) {

                    }
                }
            }
        }
    }

    private class FirstCreateDB(
        val binding: FragmentAzkarBinding,
        val context: Context,
        val activity: AppCompatActivity
    ) :
        AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String {
            var result = "Success!!!"
            if (!copyDB(context))
                result = "DB Copy ERROR!!!"
            if (!unzip(context))
                result = "UNZIP Copy ERROR!!!"
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d("NAMOO", result!!)
            binding.recyclerAzkarTitle.layoutManager = LinearLayoutManager(context)
            val adb = AzkarDB.getInstance(context)
            val titleList = adb.azkarsDAO().getAzkarTitleFor()
            binding.recyclerAzkarTitle.adapter = AzkarAdapter(titleList, activity)
        }

        @SuppressLint("SdCardPath")
        private fun copyDB(context: Context): Boolean {
            return try {
                val dis = File("/data/data/${context.packageName}/databases")
                if (!dis.exists())
                    dis.mkdir()
                val outPutFile =
                    File("/data/data/${context.packageName}/databases/azkar.zip")
                val fileOutputStream = FileOutputStream(outPutFile)
                context.assets.open("azkar.zip").copyTo(fileOutputStream)
                fileOutputStream.close()
                true
            } catch (ex: Exception) {
                Log.d("NAMOO", "Error copy db $ex")
                ex.printStackTrace()
                false
            }
        }

        @SuppressLint("SdCardPath")
        private fun unzip(context: Context): Boolean {
            return try {

                ZipFile(
                    "/data/data/${context.packageName}/databases/azkar.zip",
                    ("@zKa6").toCharArray()
                ).extractAll("/data/data/${context.packageName}/databases/")
                File("/data/data/${context.packageName}/databases/azkar.zip").delete()
                true
            } catch (ex: Exception) {
                Log.d("NAMOO", "Error unzip db $ex")
                false
            }
        }
    }
}//end of class
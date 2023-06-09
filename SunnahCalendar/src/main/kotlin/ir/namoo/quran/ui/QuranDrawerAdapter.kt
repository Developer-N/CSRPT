package ir.namoo.quran.ui

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.SeasonItemBinding
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.appPrefs


class QuranDrawerAdapter :
    RecyclerView.Adapter<QuranDrawerAdapter.QuranDrawerViewHolder>() {

    class QuranDrawerViewHolder(val binding: SeasonItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranDrawerViewHolder {
        val binding = SeasonItemBinding.inflate(parent.context.layoutInflater, parent, false)
        if (Theme.isDynamicColor(parent.context.appPrefs) && parent.context.isDynamicGrayscale) binding.image.colorFilter =
            grayScaleColorFilter
        return QuranDrawerViewHolder(binding)
    }


    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: QuranDrawerViewHolder, position: Int) {
        holder.binding.image.setImageResource(R.drawable.quran_drawer)
    }


    companion object {
        private val grayScaleColorFilter by lazy(LazyThreadSafetyMode.NONE) {
            // https://stackoverflow.com/q/10904690
            ColorMatrixColorFilter(ColorMatrix().also { it.setSaturation(0f) })
        }
    }
}

package ir.namoo.religiousprayers.ui.calendar

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.databinding.SeasonItemBinding
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.entities.Theme
import com.byagowi.persiancalendar.ui.utils.isDynamicGrayscale
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.appPrefs

class NavigationHeaderAdapter :
    RecyclerView.Adapter<NavigationHeaderAdapter.NavigationImageViewHolder>() {

    class NavigationImageViewHolder(val binding: SeasonItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationImageViewHolder {
        val binding = SeasonItemBinding.inflate(parent.context.layoutInflater, parent, false)
        if (Theme.isDynamicColor(parent.context.appPrefs) && parent.context.isDynamicGrayscale) binding.image.colorFilter =
            grayScaleColorFilter
        return NavigationImageViewHolder(binding)
    }

    override fun getItemCount(): Int = toActualIndex(0) * 4

    private val navigationImage = enumValues<NavigationImage>()

    override fun onBindViewHolder(holder: NavigationImageViewHolder, position: Int) {
        holder.binding.image.setImageResource(navigationImage[position % 4].imageID)
    }

    companion object {
        private val grayScaleColorFilter by lazy(LazyThreadSafetyMode.NONE) {
            // https://stackoverflow.com/q/10904690
            ColorMatrixColorFilter(ColorMatrix().also { it.setSaturation(0f) })
        }

        private fun toActualIndex(index: Int): Int = 4 * 100 + index

        fun getCurrentIndex(): Int =
            toActualIndex(NavigationImage.fromDate(Jdn.today()).ordinal)
    }

}

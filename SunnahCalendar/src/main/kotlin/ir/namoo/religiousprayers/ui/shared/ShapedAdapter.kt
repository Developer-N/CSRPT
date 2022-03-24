package ir.namoo.religiousprayers.ui.shared

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.byagowi.persiancalendar.ui.utils.dp


class ShapedAdapter<T>(context: Context, resource: Int, textView: Int, objects: Array<T>) :
    ArrayAdapter<T>(context, resource, textView, objects) {
    var padding = 0

    init {
        padding = 24.dp.toInt()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = super.getView(position, convertView, parent)
        if (view is TextView) setFontShapeAndGravity(view)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = super.getDropDownView(position, convertView, parent)
        if (view is TextView) setFontShapeAndGravity(view)
        view.setPadding(padding, 0, padding, 0)
        return view
    }

    private fun setFontShapeAndGravity(view: TextView) {
//        view.typeface = getAppFont(context)
        view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
    }
}

package ir.namoo.religiousprayers.ui.edit

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.utils.getAppFont


class ShapedAdapter<T>(context: Context, resource: Int, objects: Array<T>) :
    ArrayAdapter<T>(context, resource, objects) {
    // preferred drop down list item padding, used for spinners and such
    var padding = 0

    init {
        padding = getContext().resources.getDimension(R.dimen.listPreferredItemPadding).toInt()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = super.getView(position, convertView, parent)
        if (view is TextView) setFontShapeAndGravity(view as TextView)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View = super.getDropDownView(position, convertView, parent)
        if (view is TextView) setFontShapeAndGravity(view as TextView)
        view.setPadding(padding, 0, padding, 0)
        return view
    }

    private fun setFontShapeAndGravity(view: TextView) {
        view.typeface = getAppFont(context)
        view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
    }
}
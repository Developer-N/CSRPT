package ir.namoo.religiousprayers.ui

import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner

interface DrawerHost {
    fun setupToolbarWithDrawer(viewLifecycleOwner: LifecycleOwner, toolbar: Toolbar)
}

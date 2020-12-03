package ir.namoo.religiousprayers.service

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.*

/**
 * Created by Alireza Afkar on 19/6/2018AD.
 */
@TargetApi(Build.VERSION_CODES.N)
class PersianCalendarTileService : TileService() {

    override fun onClick() = try {
        startActivityAndCollapse(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }

    override fun onStartListening() = try {
        val today = getTodayOfCalendar(mainCalendar)
        qsTile?.apply {
            icon = Icon.createWithResource(
                this@PersianCalendarTileService, getDayIconResource(today.dayOfMonth)
            )
            label = getWeekDayName(today)
            contentDescription = getMonthName(today)
            // explicitly set Tile state to Active, fixes tile not being lit on some Samsung devices
            state = Tile.STATE_ACTIVE
        }?.updateTile() ?: Unit
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

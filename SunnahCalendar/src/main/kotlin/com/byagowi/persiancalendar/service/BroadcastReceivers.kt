package com.byagowi.persiancalendar.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast
import com.byagowi.persiancalendar.ADD_EVENT
import com.byagowi.persiancalendar.BROADCAST_ALARM
import com.byagowi.persiancalendar.BROADCAST_RESTART_APP
import com.byagowi.persiancalendar.BROADCAST_UPDATE_APP
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER
import com.byagowi.persiancalendar.KEY_EXTRA_PRAYER_TIME
import com.byagowi.persiancalendar.MONTH_NEXT_COMMAND
import com.byagowi.persiancalendar.MONTH_PREV_COMMAND
import com.byagowi.persiancalendar.MONTH_RESET_COMMAND
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.PrayTime
import com.byagowi.persiancalendar.ui.calendar.AddEventData
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.startAthan
import com.byagowi.persiancalendar.utils.startWorker
import com.byagowi.persiancalendar.utils.update
import com.byagowi.persiancalendar.utils.updateMonthWidget
import com.byagowi.persiancalendar.variants.debugLog
import ir.namoo.commons.BROADCAST_AZKAR
import ir.namoo.commons.KEY_AZKAR_EXTRA_NAME
import ir.namoo.religiousprayers.ui.azkar.startAzkar

class BroadcastReceivers : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        when (val action = intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            TelephonyManager.ACTION_PHONE_STATE_CHANGED,
            BROADCAST_RESTART_APP -> startWorker(context)

            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> update(context, true)
            Intent.ACTION_TIME_CHANGED, Intent.ACTION_SCREEN_ON, BROADCAST_UPDATE_APP ->
                update(context, false)

            ADD_EVENT -> runCatching {
                val addEventIntent = AddEventData.upcoming().asIntent()
                context.startActivity(addEventIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.onFailure(logException).onFailure {
                Toast.makeText(context, R.string.device_does_not_support, Toast.LENGTH_SHORT).show()
            }

            BROADCAST_ALARM -> {
                val key = intent.getStringExtra(KEY_EXTRA_PRAYER) ?: return
                val intendedTime = intent.getLongExtra(KEY_EXTRA_PRAYER_TIME, 0).takeIf { it != 0L }
                debugLog("Alarms: AlarmManager for $key")
                startAthan(context, key, intendedTime)
                update(context, false)
            }

            BROADCAST_AZKAR -> {
                val name = intent.getStringExtra(KEY_AZKAR_EXTRA_NAME) ?: return
                val intendedTime = intent.getLongExtra(KEY_EXTRA_PRAYER_TIME, 0).takeIf { it != 0L }
                debugLog("Azkar: AlarmManager for $name")
                startAzkar(context, name, intendedTime)
            }

            null -> Unit
            else -> {
                if (action.startsWith(MONTH_PREV_COMMAND)) {
                    action.replace(MONTH_PREV_COMMAND, "").toIntOrNull()?.let { id ->
                        updateMonthWidget(context, id, -1)
                    }
                } else if (action.startsWith(MONTH_NEXT_COMMAND)) {
                    action.replace(MONTH_NEXT_COMMAND, "").toIntOrNull()?.let { id ->
                        updateMonthWidget(context, id, 1)
                    }
                } else if (action.startsWith(MONTH_RESET_COMMAND)) {
                    action.replace(MONTH_RESET_COMMAND, "").toIntOrNull()?.let { id ->
                        updateMonthWidget(context, id, 0)
                    }
                }
            }
        }
    }
}

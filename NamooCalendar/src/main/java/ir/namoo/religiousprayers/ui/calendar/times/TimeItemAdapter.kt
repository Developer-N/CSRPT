package ir.namoo.religiousprayers.ui.calendar.times

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.religiousprayers.PREF_ATHAN_ALARM
import ir.namoo.religiousprayers.R
import ir.namoo.religiousprayers.databinding.TimeItemBinding
import ir.namoo.religiousprayers.utils.*
import java.util.*

class TimeItemAdapter : RecyclerView.Adapter<TimeItemAdapter.ViewHolder>() {

    @StringRes
    private val timeNames = listOf(
        R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr,
        R.string.maghrib, R.string.isha
    )

    var prayTimes: PrayTimes? = null
        set(prayTimes) {
            field = prayTimes
            timeNames.indices.forEach(::notifyItemChanged)
        }
    var isExpanded = false
        set(expanded) {
            field = expanded
            timeNames.indices.forEach(::notifyItemChanged)
        }

    var isToday = false
        set(today) {
            field = today
            timeNames.indices.forEach(::notifyItemChanged)
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        TimeItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = timeNames.size

    inner class ViewHolder(val binding: TimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "PrivateResource")
        fun bind(position: Int) {
            val timeName = timeNames[position]
            binding.root.setOnClickListener { isExpanded = true }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                binding.root.setBackgroundResource(R.drawable.pt_background)
            val now = Clock(makeCalendarFromDate(Date())).toInt()
            when (timeName) {
                R.string.imsak, R.string.fajr -> {
                    if (now < prayTimes!!.fajrClock.toInt())
                        animateDrawable()
                }
                R.string.sunrise -> {
                    if (now >= prayTimes!!.fajrClock.toInt() && now < prayTimes!!.sunriseClock.toInt())
                        animateDrawable()
                }
                R.string.dhuhr -> {
                    if (now >= prayTimes!!.sunriseClock.toInt() && now < prayTimes!!.dhuhrClock.toInt())
                        animateDrawable()
                }
                R.string.asr -> {
                    if (now >= prayTimes!!.dhuhrClock.toInt() && now < prayTimes!!.asrClock.toInt())
                        animateDrawable()
                }
                R.string.sunset, R.string.maghrib -> {
                    if (now >= prayTimes!!.asrClock.toInt() && now < prayTimes!!.maghribClock.toInt())
                        animateDrawable()
                }
                R.string.isha -> {
                    if (now >= prayTimes!!.maghribClock.toInt() && now < prayTimes!!.ishaClock.toInt())
                        animateDrawable()
                }
                else -> {//next fajr

                }
            }
            binding.name.text = "   ${binding.root.context.resources.getString(timeName)}"
            binding.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                when (timeName) {
                    R.string.imsak -> R.drawable.ic_morning_isha
                    R.string.fajr -> R.drawable.ic_morning_isha
                    R.string.sunrise -> R.drawable.ic_sunrise
                    R.string.dhuhr -> R.drawable.ic_noon_evening
                    R.string.asr -> R.drawable.ic_noon_evening
                    R.string.sunset -> R.drawable.ic_sunset
                    R.string.maghrib -> R.drawable.ic_sunset
                    R.string.isha -> R.drawable.ic_morning_isha
                    R.string.midnight -> R.drawable.ic_morning_isha
                    else -> R.drawable.ic_morning_isha
                }, 0, 0, 0
            )
            binding.time.text = prayTimes?.run {
                when (timeName) {
                    R.string.imsak -> imsakClock
                    R.string.fajr -> fajrClock
                    R.string.sunrise -> sunriseClock
                    R.string.dhuhr -> dhuhrClock
                    R.string.asr -> asrClock
                    R.string.sunset -> sunsetClock
                    R.string.maghrib -> maghribClock
                    R.string.isha -> ishaClock
                    R.string.midnight -> midnightClock
                    else -> midnightClock
                }.toFormattedString()
            } ?: ""

            val alarms = binding.root.context.appPrefs.getString(PREF_ATHAN_ALARM, "")
            if (alarms != null) {
                val existAlarms = alarms.splitIgnoreEmpty(",")
                if (notificationAthan)
                    binding.btnAthanState.setImageResource(
                        when (timeName) {
                            R.string.imsak, R.string.fajr -> if (existAlarms.contains("FAJR"))
                                R.drawable.ic_notifications
                            else
                                R.drawable.ic_notifications_off
                            R.string.sunrise -> if (existAlarms.contains("SUNRISE"))
                                R.drawable.ic_notifications
                            else
                                R.drawable.ic_notifications_off
                            R.string.dhuhr -> if (existAlarms.contains("DHUHR"))
                                R.drawable.ic_notifications
                            else
                                R.drawable.ic_notifications_off
                            R.string.asr -> if (existAlarms.contains("ASR"))
                                R.drawable.ic_notifications
                            else
                                R.drawable.ic_notifications_off
                            R.string.sunset, R.string.maghrib -> if (existAlarms.contains("MAGHRIB"))
                                R.drawable.ic_notifications
                            else
                                R.drawable.ic_notifications_off
                            R.string.isha -> if (existAlarms.contains("ISHA"))
                                R.drawable.ic_notifications
                            else
                                R.drawable.ic_notifications_off
                            else -> R.drawable.ic_notifications_off
                        }
                    )
                else
                    binding.btnAthanState.setImageResource(
                        when (timeName) {
                            R.string.imsak, R.string.fajr -> if (existAlarms.contains("FAJR"))
                                R.drawable.ic_speaker
                            else
                                R.drawable.ic_silent
                            R.string.sunrise -> if (existAlarms.contains("SUNRISE"))
                                R.drawable.ic_speaker
                            else
                                R.drawable.ic_silent
                            R.string.dhuhr -> if (existAlarms.contains("DHUHR"))
                                R.drawable.ic_speaker
                            else
                                R.drawable.ic_silent
                            R.string.asr -> if (existAlarms.contains("ASR"))
                                R.drawable.ic_speaker
                            else
                                R.drawable.ic_silent
                            R.string.sunset, R.string.maghrib -> if (existAlarms.contains("MAGHRIB"))
                                R.drawable.ic_speaker
                            else
                                R.drawable.ic_silent
                            R.string.isha -> if (existAlarms.contains("ISHA"))
                                R.drawable.ic_speaker
                            else
                                R.drawable.ic_silent
                            else -> R.drawable.ic_silent
                        }
                    )
                binding.btnAthanState.setOnClickListener {
                    it as AppCompatImageView
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            binding.root.context,
                            androidx.appcompat.R.anim.abc_fade_in
                        )
                    )
                    val name = when (timeName) {
                        R.string.imsak, R.string.fajr -> "FAJR"
                        R.string.sunrise -> "SUNRISE"
                        R.string.dhuhr -> "DHUHR"
                        R.string.asr -> "ASR"
                        R.string.sunset, R.string.maghrib -> "MAGHRIB"
                        R.string.isha -> "ISHA"
                        else -> ""
                    }
                    updateAthanInPref(binding.root.context, name)
                    notifyItemChanged(position)
                }
            }

        }//end of bind

        @SuppressLint("PrivateResource")
        private fun animateDrawable() {
            if (!isToday)
                return
            val anim = AnimationUtils.loadAnimation(
                binding.root.context,
                com.google.android.material.R.anim.abc_fade_in
            )
            anim.duration = 1000
            anim.interpolator = DecelerateInterpolator()
            anim.repeatCount = ValueAnimator.INFINITE
            binding.name.startAnimation(anim)
        }
    }// end of class ViewHolder

}//end of class TimeItemAdapter

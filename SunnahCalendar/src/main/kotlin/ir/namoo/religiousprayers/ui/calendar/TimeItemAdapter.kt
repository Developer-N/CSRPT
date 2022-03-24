package ir.namoo.religiousprayers.ui.calendar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.NtimeItemBinding
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.ui.utils.layoutInflater
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getNextOwghatTimeId
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.update
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.persiancalendar.praytimes.PrayTimes
import ir.namoo.commons.model.AthanSetting
import ir.namoo.commons.model.AthanSettingsDB
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
        NtimeItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = timeNames.size
    override fun getItemViewType(position: Int): Int = position

    inner class ViewHolder(val binding: NtimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var settings: List<AthanSetting>? =
            AthanSettingsDB.getInstance(binding.root.context.applicationContext).athanSettingsDAO()
                .getAllAthanSettings()

        @SuppressLint("SetTextI18n", "PrivateResource", "NotifyDataSetChanged")
        fun bind(position: Int) {
            val timeName = timeNames[position]

            binding.root.setOnClickListener { isExpanded = true }
            val nowClock = Clock(Calendar.getInstance(Locale.getDefault()))
            val next = prayTimes?.getNextOwghatTimeId(nowClock) ?: R.string.fajr
            if (timeName == next)
                animateDrawable(prayTimes?.getFromStringId(next))
            if (next == R.string.sunset && timeName == R.string.maghrib)
                animateDrawable(prayTimes?.getFromStringId(R.string.maghrib))
            binding.name.text = "   ${binding.root.context.resources.getString(timeName)}"
            binding.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                when (timeName) {
                    R.string.imsak -> R.drawable.ic_fajr_isha
                    R.string.fajr -> R.drawable.ic_fajr_isha
                    R.string.sunrise -> R.drawable.ic_sunrise
                    R.string.dhuhr -> R.drawable.ic_dhuhr_asr
                    R.string.asr -> R.drawable.ic_dhuhr_asr
                    R.string.sunset -> R.drawable.ic_maghrib
                    R.string.maghrib -> R.drawable.ic_maghrib
                    R.string.isha -> R.drawable.ic_fajr_isha
                    R.string.midnight -> R.drawable.ic_fajr_isha
                    else -> R.drawable.ic_fajr_isha
                }, 0, 0, 0
            )
            binding.time.text = prayTimes?.getFromStringId(timeName)?.toFormattedString()
            settings?.get(position)?.also { setting ->
                if (setting.state)
                    binding.btnAthanState.setImageResource(R.drawable.ic_baseline_volume_up_24)
                else
                    binding.btnAthanState.setImageResource(R.drawable.ic_baseline_volume_off_24)
                binding.btnAthanState.setOnClickListener {
                    it as AppCompatImageView
                    it.startAnimation(
                        AnimationUtils.loadAnimation(
                            binding.root.context,
                            androidx.appcompat.R.anim.abc_fade_in
                        )
                    )
                    setting.state = !setting.state
                    AthanSettingsDB.getInstance(binding.root.context.applicationContext)
                        .athanSettingsDAO().update(setting)
                    // request overly permission
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            !Settings.canDrawOverlays(binding.root.context)
                        ) {
                            MaterialAlertDialogBuilder(binding.root.context).apply {
                                setTitle(binding.root.context.getString(R.string.requset_permision))
                                setMessage(binding.root.context.getString(R.string.need_full_screen_permision))
                                setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                                    binding.root.context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:" + binding.root.context.packageName)
                                        )
                                    )
                                }
                                setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                                    dialog.cancel()
                                }
                                show()
                            }
                        }
                    }.onFailure(logException)
                    notifyDataSetChanged()
                    update(binding.root.context, true)
                }

                binding.btnAthanState.setOnLongClickListener {
//                    binding.root.context.startActivity(
//                        Intent(binding.root.context, AthanSettingActivity::class.java)
//                            .apply { putExtra(ATHAN_ID, setting.id) })
                    Toast.makeText(binding.root.context, "(*^▽^*)", Toast.LENGTH_SHORT).show()
                    true
                }

            }

        }//end of bind

        @SuppressLint("PrivateResource", "SetTextI18n")
        private fun animateDrawable(clock: Clock?) {
            clock ?: return
            if (!isToday) {
                binding.remainToNext.visibility = View.INVISIBLE
                return
            }
            val cal = Calendar.getInstance()
            val current = Clock(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            if (clock.toMinutes() - current.toMinutes() < 0) {
                binding.remainToNext.visibility = View.INVISIBLE
                return
            }
            val difference = Clock.fromMinutesCount(clock.toMinutes() - current.toMinutes())
            binding.remainToNext.text = if (difference.toMinutes() > 0)
                binding.root.context.getString(R.string.remaining_daylight) + spacedColon +
                        difference.asRemainingTime(binding.root.resources, short = true)
            else ""
            val anim = AnimationUtils.loadAnimation(
                binding.root.context,
                com.google.android.material.R.anim.abc_fade_in
            )
            anim.duration = 1000
            anim.interpolator = AccelerateDecelerateInterpolator()
            anim.repeatCount = ValueAnimator.INFINITE
            binding.remainToNext.clearAnimation()
            binding.remainToNext.startAnimation(anim)
            Handler(Looper.getMainLooper()).postDelayed((60 - cal.get(Calendar.SECOND)) * 1000L) {
                animateDrawable(clock)
            }
        }
    }// end of class ViewHolder

}//end of class TimeItemAdapter

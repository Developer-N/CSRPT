package ir.namoo.religiousprayers.ui.preferences.interfacecalendar.calendarsorder

/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import ir.namoo.religiousprayers.databinding.CalendarTypeItemBinding
import ir.namoo.religiousprayers.ui.MainActivity
import ir.namoo.religiousprayers.utils.layoutInflater
import java.util.*

class RecyclerListAdapter(
    private val calendarPreferenceDialog: CalendarPreferenceDialog,
    private val context: Context,
    titles: List<String>, values: List<String>, enabled: List<Boolean>
) : RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>() {

    private val titles: MutableList<String>
    private val values: MutableList<String>
    private val enabled: MutableList<Boolean>

    val result: List<String>
        get() = values.filterIndexed { i, _ -> enabled[i] }

    init {
        this.titles = ArrayList(titles)
        this.values = ArrayList(values)
        this.enabled = ArrayList(enabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        CalendarTypeItemBinding.inflate(parent.context.layoutInflater, parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)

        // Start a drag whenever the handle view it touched
        holder.itemView.setOnTouchListener { _, event ->
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                calendarPreferenceDialog.onStartDrag(holder)
            }
            false
        }
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(titles, fromPosition, toPosition)
        Collections.swap(values, fromPosition, toPosition)
        Collections.swap(enabled, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun onItemDismissed(position: Int) {
        titles.removeAt(position)
        values.removeAt(position)
        enabled.removeAt(position)
        notifyItemRemoved(position)

        // Easter egg when all are swiped
        if (titles.size == 0) {
            try {
                val view = (context as? MainActivity)?.coordinator ?: return
                ValueAnimator.ofFloat(0f, 360f).apply {
                    duration = 3000L
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { value -> view.rotation = value.animatedValue as Float }
                }.start()
                //                Context context = calendarPreferenceDialog.getContext();
                //                MediaPlayer mediaPlayer = MediaPlayer.create(context,
                //                        R.raw.bach_invention_01);
                //                if (!mediaPlayer.isPlaying()) {
                //                    mediaPlayer.start();
                //                }
                //                AppCompatImageButton imageButton = new AppCompatImageButton(context);
                //                imageButton.setImageResource(R.drawable.ic_stop);
                //                AlertDialog alertDialog = new AlertDialog.Builder(context)
                //                        .setView(imageButton).create();
                //                imageButton.setOnClickListener(v -> {
                //                    try {
                //                        mediaPlayer.stop();
                //                    } catch (Exception ignore) {
                //                    }
                //                    alertDialog.dismiss();
                //                });
                //                alertDialog.show();
            } catch (e: Exception) {
                e.printStackTrace()
            }

            calendarPreferenceDialog.dismiss()
        }
    }

    override fun getItemCount(): Int = titles.size

    inner class ItemViewHolder(private val binding: CalendarTypeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.checkTextView.setOnClickListener {
                val newState = !binding.checkTextView.isChecked
                binding.checkTextView.isChecked = newState
                enabled[layoutPosition] = newState
            }
        }

        fun bind(position: Int) = binding.apply {
            checkTextView.text = titles[position]
            checkTextView.isChecked = enabled[position]
        }

        fun onItemSelected() = binding.root.setBackgroundColor(Color.LTGRAY)

        fun onItemCleared() = binding.root.setBackgroundColor(0)
    }
}

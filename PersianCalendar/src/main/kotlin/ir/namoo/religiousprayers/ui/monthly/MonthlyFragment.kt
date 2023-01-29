package ir.namoo.religiousprayers.ui.monthly

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatSpinner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentMonthlyBinding
import com.byagowi.persiancalendar.global.persianMonths
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.namoo.commons.PREF_APP_FONT
import ir.namoo.commons.SYSTEM_DEFAULT_FONT
import ir.namoo.commons.utils.getAppFont
import ir.namoo.commons.utils.ndp
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MonthlyFragment : Fragment(R.layout.fragment_monthly) {

    private val viewModel: MonthlyViewModel = get()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMonthlyBinding.bind(view)

        binding.appBar.toolbar.setupMenuNavigation()
        binding.appBar.root.hideToolbarBottomShadow()

        //#############################################
        val spinner = run {
            val spinnerFrameLayout = FrameLayout(view.context)
            spinnerFrameLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 36.ndp.toInt()
            )
            spinnerFrameLayout.background = MaterialShapeDrawable().also {
                it.shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(32.ndp)
                it.fillColor = ColorStateList.valueOf(
                    ColorUtils.setAlphaComponent(
                        view.context.resolveColor(R.attr.menuIconColor), 16
                    )
                )
                it.setPadding(16, 0, 16, 0)
            }
            val spinner = AppCompatSpinner(binding.appBar.toolbar.context)
            spinnerFrameLayout.addView(spinner)
            binding.appBar.toolbar.addView(spinnerFrameLayout)
            spinner
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            spinner.background =
                view.context.getCompatDrawable(R.drawable.conveter_spinner_background)
            spinner.setPaddingRelative(0, 0, 24.ndp.toInt(), 0)
        }
        spinner.adapter = ArrayAdapter(
            spinner.context, R.layout.toolbar_dropdown_item, persianMonths.toList()
        )
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = viewModel.loadTimeFor(requireContext(), persianMonths.toList()[position])
        }
        spinner.setSelection(0)
        //#############################################

        lifecycleScope.launch {
            viewModel.loadTimeFor(requireContext(), persianMonths[0])
        }
        binding.monthlyView.setContent {
            Mdc3Theme {
                val appFont = remember { getAppFont(requireContext()) }
                val normalTextColor =
                    remember { Color(requireContext().resolveColor(R.attr.colorTextNormal)) }
                val cardColor = remember { Color(requireContext().resolveColor(R.attr.colorCard)) }
                val fontSize = if (requireContext().appPrefs.getString(
                        PREF_APP_FONT, SYSTEM_DEFAULT_FONT
                    )?.contains("Vazir") == true
                ) 12.sp else 14.sp
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = CenterVertically
                    ) {
//                        NSpinner(options = persianMonths.toList(), itemClick = {
//                            viewModel.loadTimeFor(requireContext(), it)
//                        }, typeface = appFont)
                        AnimatedVisibility(visible = viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(6.dp, 2.dp)
                                    .size(32.dp)
                            )
                        }
                    }

                    val listState = rememberLazyListState()
                    LazyColumn(state = listState) {
                        if (viewModel.times.isNotEmpty()) itemsIndexed(
                            items = viewModel.times,
                            key = { index, _ ->
                                index
                            }) { index, prayTime ->
                            Box(modifier = Modifier.animateItemPlacement()) {
                                MonthlyItemUIElement(
                                    prayTime = prayTime,
                                    monthDaySummary = formatNumber("${index + 1} ${viewModel.selectedMonthName}"),
                                    typeface = appFont,
                                    fontSize = fontSize,
                                    cardColor = cardColor,
                                    textColor = normalTextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }//end of onCreateView
}//end of EditFragment

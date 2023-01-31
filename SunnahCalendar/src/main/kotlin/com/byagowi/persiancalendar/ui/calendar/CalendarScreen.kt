package com.byagowi.persiancalendar.ui.calendar

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.byagowi.persiancalendar.ATHANS_LIST
import com.byagowi.persiancalendar.BuildConfig
import com.byagowi.persiancalendar.DEFAULT_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_DISABLE_OWGHAT
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_LAST_APP_VISIT_VERSION
import com.byagowi.persiancalendar.PREF_NOTIFY_DATE
import com.byagowi.persiancalendar.PREF_OTHER_CALENDARS_KEY
import com.byagowi.persiancalendar.PREF_SECONDARY_CALENDAR_IN_TABLE
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.TIME_NAMES
import com.byagowi.persiancalendar.databinding.EventsTabContentBinding
import com.byagowi.persiancalendar.databinding.FragmentCalendarBinding
import com.byagowi.persiancalendar.databinding.OwghatTabContentBinding
import com.byagowi.persiancalendar.databinding.OwghatTabPlaceholderBinding
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Clock
import com.byagowi.persiancalendar.entities.EventsRepository
import com.byagowi.persiancalendar.entities.EventsStore
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.global.calculationMethod
import com.byagowi.persiancalendar.global.coordinates
import com.byagowi.persiancalendar.global.enabledCalendars
import com.byagowi.persiancalendar.global.eventsRepository
import com.byagowi.persiancalendar.global.isHighTextContrastEnabled
import com.byagowi.persiancalendar.global.isShowDeviceCalendarEvents
import com.byagowi.persiancalendar.global.isTalkBackEnabled
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.global.mainCalendar
import com.byagowi.persiancalendar.global.secondaryCalendar
import com.byagowi.persiancalendar.global.spacedColon
import com.byagowi.persiancalendar.global.spacedComma
import com.byagowi.persiancalendar.global.updateStoredPreference
import com.byagowi.persiancalendar.ui.calendar.calendarpager.CalendarPager
import com.byagowi.persiancalendar.ui.calendar.dialogs.showDayPickerDialog
import com.byagowi.persiancalendar.ui.calendar.dialogs.showMonthOverviewDialog
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsAdapter
import com.byagowi.persiancalendar.ui.calendar.shiftwork.showShiftWorkDialog
import com.byagowi.persiancalendar.ui.common.CalendarsView
import com.byagowi.persiancalendar.ui.settings.SettingsScreen
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.ui.utils.askForPostNotificationPermission
import com.byagowi.persiancalendar.ui.utils.getCompatDrawable
import com.byagowi.persiancalendar.ui.utils.hideToolbarBottomShadow
import com.byagowi.persiancalendar.ui.utils.navigateSafe
import com.byagowi.persiancalendar.ui.utils.onClick
import com.byagowi.persiancalendar.ui.utils.openHtmlInBrowser
import com.byagowi.persiancalendar.ui.utils.resolveColor
import com.byagowi.persiancalendar.ui.utils.setupExpandableAccessibilityDescription
import com.byagowi.persiancalendar.ui.utils.setupLayoutTransition
import com.byagowi.persiancalendar.ui.utils.setupMenuNavigation
import com.byagowi.persiancalendar.utils.TWO_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.calculatePrayTimes
import com.byagowi.persiancalendar.utils.calendarType
import com.byagowi.persiancalendar.utils.cityName
import com.byagowi.persiancalendar.utils.dayTitleSummary
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.formatTitle
import com.byagowi.persiancalendar.utils.getA11yDaySummary
import com.byagowi.persiancalendar.utils.getEventsTitle
import com.byagowi.persiancalendar.utils.getFromStringId
import com.byagowi.persiancalendar.utils.getShiftWorkTitle
import com.byagowi.persiancalendar.utils.isRtl
import com.byagowi.persiancalendar.utils.logException
import com.byagowi.persiancalendar.utils.monthFormatForSecondaryCalendar
import com.byagowi.persiancalendar.utils.monthName
import com.byagowi.persiancalendar.utils.readDayDeviceEvents
import com.byagowi.persiancalendar.utils.startAthan
import com.byagowi.persiancalendar.utils.titleStringId
import com.byagowi.persiancalendar.variants.debugAssertNotNull
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.github.persiancalendar.calendar.AbstractDate
import ir.namoo.commons.NAVIGATE_TO_DOWNLOAD_FRAGMENT
import ir.namoo.commons.appLink
import ir.namoo.commons.model.CityModel
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.repository.asDataState
import ir.namoo.commons.utils.createBitmapFromView2
import ir.namoo.commons.utils.isNetworkConnected
import ir.namoo.commons.utils.openUrlInCustomTab
import ir.namoo.commons.utils.snackMessage
import ir.namoo.religiousprayers.praytimeprovider.PrayTimeProvider
import ir.namoo.religiousprayers.ui.calendar.TimeItemAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import org.koin.android.ext.android.get
import java.io.File
import java.io.FileOutputStream

class CalendarScreen : Fragment(R.layout.fragment_calendar) {

    private var mainBinding: FragmentCalendarBinding? = null
    private var searchView: SearchView? = null

    override fun onDestroyView() {
        super.onDestroyView()
        mainBinding = null
        searchView = null
    }

    private val onBackPressedCloseSearchCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            searchView?.takeIf { !it.isIconified }?.onActionViewCollapsed()
            isEnabled = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.onBackPressedDispatcher?.addCallback(this, onBackPressedCloseSearchCallback)
    }

    private fun enableOwghatTab(context: Context): Boolean {
        val appPrefs = context.appPrefs
        return coordinates != null || // if coordinates is set, should be shown
                (language.isPersian && // The placeholder isn't translated to other languages
                        // The user is already dismissed the third tab
                        !appPrefs.getBoolean(PREF_DISABLE_OWGHAT, false) &&
                        // Try to not show the placeholder to established users
                        PREF_APP_LANGUAGE !in appPrefs)
    }

    private fun createEventsTab(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding = EventsTabContentBinding.inflate(inflater, container, false)
        binding.eventsContent.setupLayoutTransition()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDayChangeEvent
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { showEvent(binding, it) }
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun createOwghatTab(inflater: LayoutInflater, container: ViewGroup?): View {
        coordinates ?: return createOwghatTabPlaceholder(inflater, container)
        val binding = OwghatTabContentBinding.inflate(inflater, container, false)

        binding.root.setOnClickListener {
            (binding.timesRecyclerView.adapter as TimeItemAdapter).isExpanded = true
            TransitionManager.beginDelayedTransition(binding.root, ChangeBounds())
        }
        binding.root.setupExpandableAccessibilityDescription()
        binding.cityName.run {
            text = requireContext().appPrefs.cityName + "( ${
                when (PrayTimeProvider.ptFrom) {
                    0 -> getString(R.string.calculated_time)
                    1 -> getString(R.string.exact_time)
                    2 -> getString(R.string.edited_time)
                    else -> getString(R.string.calculated_asr)
                }
            })"
            this.setTextColor(
                when (PrayTimeProvider.ptFrom) {
                    2 -> requireContext().resolveColor(R.attr.colorTextNormal)
                    0 -> requireContext().resolveColor(R.attr.colorTextHoliday)
                    else -> requireContext().resolveColor(R.attr.colorTextPrimary)
                }
            )
            setOnLongClickListener {
                startAthan(requireContext(), ATHANS_LIST.random()/*SUNRISE_KRY*/, null)
//                startAthan(requireContext(), "BFAJR", null)
                true
            }
        }
        binding.times.layoutTransition = LayoutTransition().also {
            it.enableTransitionType(LayoutTransition.APPEARING)
            it.setAnimateParentHierarchy(false)
        }
        binding.timesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = TimeItemAdapter()
        binding.timesRecyclerView.adapter = adapter
        // Follows https://developer.android.com/topic/libraries/architecture/coroutines#lifecycle-aware
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch(Dispatchers.Main) {
                    if (viewModel.owqatTabeSelected) adapter.isExpanded = !adapter.isExpanded
                }
                launch {
                    viewModel.selectedDayChangeEvent.collectLatest { jdn ->
                        setOwghat(binding, jdn, jdn == Jdn.today())
                    }
                }
                launch {
                    viewModel.selectedTabIndex.collectLatest {
                        if (it == OWGHAT_TAB) binding.sunView.startAnimate()
                        else binding.sunView.clear()
                    }
                }
            }
        }

        binding.btnOwghatShare.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(
                    requireContext(),
                    com.google.android.material.R.anim.abc_fade_in
                )
            )
            shareOwghat()
        }
        binding.btnOwghatTackPhoto.setOnClickListener {
            takePhoto(binding)

        }
        checkForAvailableTimes()

        return binding.root
    }

    private fun createOwghatTabPlaceholder(inflater: LayoutInflater, container: ViewGroup?): View {
        val binding = OwghatTabPlaceholderBinding.inflate(inflater, container, false)
        binding.buttonsBar.header.setText(R.string.ask_user_to_set_location)
        binding.buttonsBar.settings.setOnClickListener {
            findNavController().navigateSafe(
                CalendarScreenDirections.navigateToSettings(SettingsScreen.LOCATION_ATHAN_TAB)
            )
        }
        binding.buttonsBar.discard.setOnClickListener {
            context?.appPrefs?.edit { putBoolean(PREF_DISABLE_OWGHAT, true) }
            findNavController().navigateSafe(
                CalendarScreenDirections.navigateToSelf()
            )
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCalendarBinding.bind(view)
        mainBinding = binding

        val tabs = listOfNotNull(
            R.string.calendar to createCalendarsTab(view.context),
            R.string.events to createEventsTab(layoutInflater, view.parent as ViewGroup),
            if (enableOwghatTab(view.context)) // The optional third tab
                R.string.owghat to createOwghatTab(layoutInflater, view.parent as ViewGroup)
            else null
        )

        // tabs should fill their parent otherwise view pager can't handle it
        tabs.forEach { (_: Int, tabView: View) ->
            tabView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        binding.calendarPager.also {
            it.onDayClicked = { jdn -> bringDate(jdn, monthChange = false) }
            it.onDayLongClicked = ::addEventOnCalendar
            it.setSelectedDay(
                Jdn(viewModel.selectedMonth.value), highlight = false, smoothScroll = false
            )
            it.onMonthSelected = { viewModel.changeSelectedMonth(it.selectedMonth) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedMonth
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { updateToolbar(binding, it) }
        }

        val tabsViewPager = binding.viewPager
        tabsViewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int = tabs.size
            override fun getItemViewType(position: Int) = position // set viewtype equal to position
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(tabs[viewType].second) {}
        }
        TabLayoutMediator(binding.tabLayout, tabsViewPager) { tab, i ->
            tab.setText(tabs[i].first)
        }.attach()
        tabsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.changeSelectedTabIndex(position)

                // Make sure view pager's height at least matches with the shown tab
                binding.viewPager.width.takeIf { it != 0 }?.let { width ->
                    tabs[position].second.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    binding.viewPager.minimumHeight = tabs[position].second.measuredHeight
                }
            }
        })

        tabsViewPager.setCurrentItem(
            viewModel.selectedTabIndex.value.coerceAtMost(tabs.size - 1),
            false
        )
        setupMenu(binding.appBar.toolbar, binding.calendarPager)

        binding.root.post {
            binding.root.context.appPrefs.edit {
                putInt(PREF_LAST_APP_VISIT_VERSION, BuildConfig.VERSION_CODE)
            }
        }

        if (viewModel.selectedDay.value != Jdn.today()) {
            bringDate(viewModel.selectedDay.value, monthChange = false, smoothScroll = false)
        } else {
            bringDate(Jdn.today(), monthChange = false, highlight = false)
        }

        binding.appBar.let { appBar ->
            appBar.toolbar.setupMenuNavigation()
            appBar.root.hideToolbarBottomShadow()
        }
    }

    private fun createCalendarsTab(context: Context): View {
        val calendarsView = CalendarsView(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDayChangeEvent
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collectLatest { jdn ->
                    calendarsView.showCalendars(jdn, mainCalendar, enabledCalendars)
                }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            (PREF_NOTIFY_DATE !in context.appPrefs ||
                    !context.appPrefs.getBoolean(PREF_NOTIFY_DATE, DEFAULT_NOTIFY_DATE))
        ) {
            calendarsView.buttonsBar.settings.setOnClickListener {
                calendarsView.buttonsBar.root.isVisible = false
                activity?.askForPostNotificationPermission()
            }
            calendarsView.buttonsBar.discard.setOnClickListener {
                calendarsView.buttonsBar.root.isVisible = false
                context.appPrefs.edit { putBoolean(PREF_NOTIFY_DATE, false) }
            }
            calendarsView.buttonsBar.header.text = getString(R.string.enable_notification)
            calendarsView.buttonsBar.root.isVisible = true
            calendarsView.buttonsBar.settings.setText(R.string.notify_date)
        }

        return calendarsView
    }

    private fun addEventOnCalendar(jdn: Jdn) {
        val activity = activity ?: return
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) activity.askForCalendarPermission() else {
            runCatching { addEvent.launch(jdn) }.onFailure(logException).onFailure {
                Snackbar.make(
                    view ?: return, R.string.device_calendar_does_not_support,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateToolbar(binding: FragmentCalendarBinding, date: AbstractDate) {
        val toolbar = binding.appBar.toolbar
        val secondaryCalendar = secondaryCalendar
        if (secondaryCalendar == null) {
            toolbar.title = date.monthName
            toolbar.subtitle = formatNumber(date.year)
        } else {
            toolbar.title = language.my.format(date.monthName, formatNumber(date.year))
            toolbar.subtitle = monthFormatForSecondaryCalendar(date, secondaryCalendar)
        }
    }

    private val addEvent =
        registerForActivityResult(object : ActivityResultContract<Jdn, Void?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
            override fun createIntent(context: Context, input: Jdn): Intent {
                val time = input.toJavaCalendar().timeInMillis
                return Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(
                        CalendarContract.Events.DESCRIPTION, dayTitleSummary(
                            input, input.toCalendar(mainCalendar)
                        )
                    )
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, time)
                    .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            }
        }) { mainBinding?.calendarPager?.refresh(isEventsModified = true) }

    private val viewEvent =
        registerForActivityResult(object : ActivityResultContract<Long, Void?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
            override fun createIntent(context: Context, input: Long): Intent =
                Intent(Intent.ACTION_VIEW).setData(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, input)
                )
        }) { mainBinding?.calendarPager?.refresh(isEventsModified = true) }

    override fun onResume() {
        super.onResume()
        // If events are enabled refresh the pager events on resumes anyway
        if (isShowDeviceCalendarEvents) mainBinding?.calendarPager?.refresh(isEventsModified = true)
    }

    private fun getDeviceEventsTitle(dayEvents: List<CalendarEvent<*>>) = buildSpannedString {
        dayEvents.filterIsInstance<CalendarEvent.DeviceCalendarEvent>().forEachIndexed { i, event ->
            if (i != 0) appendLine()
            inSpans(object : ClickableSpan() {
                override fun onClick(textView: View) = runCatching {
                    viewEvent.launch(event.id.toLong())
                }.onFailure(logException).onFailure {
                    Snackbar.make(
                        textView,
                        R.string.device_calendar_does_not_support,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }.let {}

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    runCatching {
                        // should be turned to long then int otherwise gets stupid alpha
                        if (event.color.isNotEmpty()) ds.color = event.color.toLong().toInt()
                    }.onFailure(logException)
                }
            }) { append(event.formatTitle()) }
        }
    }

    private val viewModel by viewModels<CalendarViewModel>()
    private fun bringDate(
        jdn: Jdn, highlight: Boolean = true, monthChange: Boolean = true,
        smoothScroll: Boolean = true
    ) {
        mainBinding?.calendarPager?.setSelectedDay(jdn, highlight, monthChange, smoothScroll)

        val isToday = Jdn.today() == jdn
        viewModel.changeSelectedDay(jdn)

        // a11y
        if (isTalkBackEnabled && !isToday && monthChange) Snackbar.make(
            mainBinding?.root ?: return,
            getA11yDaySummary(
                context ?: return, jdn, false, EventsStore.empty(),
                withZodiac = true, withOtherCalendars = true, withTitle = true
            ),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEvent(eventsBinding: EventsTabContentBinding, jdn: Jdn) {
        val activity = activity ?: return

        eventsBinding.shiftWorkTitle.text = getShiftWorkTitle(jdn, false)
        val events =
            eventsRepository?.getEvents(jdn, activity.readDayDeviceEvents(jdn)) ?: emptyList()
        val holidays = getEventsTitle(
            events,
            holiday = true, compact = false, showDeviceCalendarEvents = false, insertRLM = false,
            addIsHoliday = isHighTextContrastEnabled
        )
        val nonHolidays = getEventsTitle(
            events,
            holiday = false, compact = false, showDeviceCalendarEvents = false, insertRLM = false,
            addIsHoliday = false
        )
        val deviceEvents = getDeviceEventsTitle(events)
        val contentDescription = StringBuilder()

        eventsBinding.noEvent.isVisible =
            listOf(holidays, deviceEvents, nonHolidays).all { it.isEmpty() }

        if (holidays.isNotEmpty()) {
            eventsBinding.holidayTitle.text = holidays
            val holidayContent = getString(R.string.holiday_reason, holidays)
            eventsBinding.holidayTitle.contentDescription = holidayContent
            contentDescription.append(holidayContent)
            eventsBinding.holidayTitle.isVisible = true
        } else {
            eventsBinding.holidayTitle.isVisible = false
        }

        if (deviceEvents.isNotEmpty()) {
            eventsBinding.deviceEventTitle.text = deviceEvents
            contentDescription
                .appendLine()
                .appendLine(getString(R.string.show_device_calendar_events))
                .append(deviceEvents)

            eventsBinding.deviceEventTitle.let {
                it.movementMethod = LinkMovementMethod.getInstance()
                it.isVisible = true
            }
        } else {
            eventsBinding.deviceEventTitle.isVisible = false
        }

        if (nonHolidays.isNotEmpty()) {
            eventsBinding.eventTitle.text = nonHolidays
            contentDescription
                .appendLine()
                .appendLine(getString(R.string.events))
                .append(nonHolidays)

            eventsBinding.eventTitle.isVisible = true
        } else {
            eventsBinding.eventTitle.isVisible = false
        }

        if (PREF_HOLIDAY_TYPES !in activity.appPrefs && language.isIranExclusive) {
            eventsBinding.buttonsBar.header.setText(R.string.warn_if_events_not_set)
            eventsBinding.buttonsBar.settings.setOnClickListener {
                findNavController().navigateSafe(
                    CalendarScreenDirections.navigateToSettings(
                        SettingsScreen.INTERFACE_CALENDAR_TAB, PREF_HOLIDAY_TYPES
                    )
                )
            }
            eventsBinding.buttonsBar.discard.setOnClickListener {
                activity.appPrefs.edit {
                    putStringSet(PREF_HOLIDAY_TYPES, EventsRepository.iranDefault)
                }
                eventsBinding.buttonsBar.root.isVisible = false
            }
        } else eventsBinding.buttonsBar.root.isVisible = false

        eventsBinding.root.contentDescription = contentDescription
    }

    private fun setOwghat(owghatBinding: OwghatTabContentBinding, jdn: Jdn, isToday: Boolean) {
        val coordinates = coordinates ?: return

        val date = jdn.toJavaCalendar()
        var prayTimes = coordinates.calculatePrayTimes(date)
        prayTimes = PrayTimeProvider(requireContext()).nReplace(prayTimes, jdn)!!
        (owghatBinding.timesRecyclerView.adapter as? TimeItemAdapter)?.isToday = isToday
        (owghatBinding.timesRecyclerView.adapter as? TimeItemAdapter)?.prayTimes = prayTimes
        owghatBinding.moonView.isVisible = !isToday
        owghatBinding.moonView.setOnClickListener {
            findNavController().navigateSafe(
                CalendarScreenDirections.actionCalendarToAstronomy(jdn - Jdn.today())
            )
        }
        if (!isToday) owghatBinding.moonView.jdn = jdn.value.toFloat()
        owghatBinding.sunView.let { sunView ->
            sunView.isVisible = if (isToday) {
                sunView.prayTimes = prayTimes
                sunView.setTime(date)
                true
            } else false
            if (isToday && mainBinding?.viewPager?.currentItem == OWGHAT_TAB) sunView.startAnimate()
        }
    }

    private fun setupMenu(toolbar: Toolbar, calendarPager: CalendarPager) {
        val toolbarContext = toolbar.context // context wrapped with toolbar related theme
        val context = calendarPager.context // context usable for normal dialogs

        val searchView = SearchView(toolbarContext).also { searchView = it }
        searchView.setOnCloseListener {
            onBackPressedCloseSearchCallback.isEnabled = false
            false // don't prevent the event cascade
        }
        searchView.setOnSearchClickListener {
            onBackPressedCloseSearchCallback.isEnabled = true
            viewLifecycleOwner.lifecycleScope.launch {
                // 2s timeout, give up if took too much time
                withTimeoutOrNull(TWO_SECONDS_IN_MILLIS) { viewModel.initializeEventsRepository() }
            }
        }
        // Remove search edit view below bar
        searchView.findViewById<View?>(androidx.appcompat.R.id.search_plate).debugAssertNotNull
            ?.setBackgroundColor(Color.TRANSPARENT)
        searchView.findViewById<SearchAutoComplete?>(
            androidx.appcompat.R.id.search_src_text
        ).debugAssertNotNull?.let {
            it.setHint(R.string.search_in_events)
            it.setOnItemClickListener { parent, _, position, _ ->
                val date = (parent.getItemAtPosition(position) as CalendarEvent<*>).date
                val type = date.calendarType
                val today = Jdn.today().toCalendar(type)
                bringDate(
                    Jdn(
                        type,
                        if (date.year == -1)
                            (today.year + if (date.month < today.month) 1 else 0)
                        else date.year,
                        date.month,
                        date.dayOfMonth
                    )
                )
                searchView.onActionViewCollapsed()
            }
            val eventsAdapter =
                SearchEventsAdapter(context, onQueryChanged = viewModel::searchEvent)
            it.setAdapter(eventsAdapter)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.eventsFlow
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .collectLatest(eventsAdapter::setData)
            }
        }

        toolbar.menu.add(R.string.return_to_today).also {
            it.icon = toolbarContext.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.onClick { bringDate(Jdn.today(), highlight = false) }
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.todayButtonVisibilityEvent
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .distinctUntilChanged()
                    .collectLatest(it::setVisible)
            }
        }
        toolbar.menu.add(R.string.search_in_events).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.actionView = searchView
        }
        toolbar.menu.add(R.string.support).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_support)
            it.onClick {
                requireActivity().openUrlInCustomTab("https://namoodev.ir/faq")
            }
        }
        toolbar.menu.add(R.string.goto_date).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showDayPickerDialog(
                    activity ?: return@onClick, viewModel.selectedDay.value, R.string.go
                ) { jdn -> bringDate(jdn) }
            }
        }
        toolbar.menu.add(R.string.add_event).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick { addEventOnCalendar(viewModel.selectedDay.value) }
        }
        toolbar.menu.add(R.string.shift_work_settings).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showShiftWorkDialog(activity ?: return@onClick, viewModel.selectedDay.value)
            }
        }
        toolbar.menu.add(R.string.month_overview).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick {
                showMonthOverviewDialog(activity ?: return@onClick, viewModel.selectedMonth.value)
            }
        }
        if (coordinates != null) {
            toolbar.menu.add(R.string.month_pray_times).also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                it.onClick {
                    context.openHtmlInBrowser(createOwghatHtmlReport(viewModel.selectedMonth.value))
                }
            }
        }
        toolbar.menu.addSubMenu(R.string.show_secondary_calendar).also { menu ->
            val groupId = Menu.FIRST
            val prefs = context.appPrefs
            (listOf(null) + enabledCalendars.drop(1)).forEach {
                val item = menu.add(groupId, Menu.NONE, Menu.NONE, it?.title ?: R.string.none)
                item.isChecked = it == secondaryCalendar
                item.onClick {
                    prefs.edit {
                        if (it == null) remove(PREF_SECONDARY_CALENDAR_IN_TABLE)
                        else {
                            putBoolean(PREF_SECONDARY_CALENDAR_IN_TABLE, true)
                            putString(
                                PREF_OTHER_CALENDARS_KEY,
                                // Put the chosen calendars at the first of calendars priorities
                                (listOf(it) + (enabledCalendars.drop(1) - it)).joinToString(",")
                            )
                        }
                    }
                    updateStoredPreference(context)
                    findNavController().navigateSafe(CalendarScreenDirections.navigateToSelf())
                }
            }
            menu.setGroupCheckable(groupId, true, true)
        }
    }

    private fun createOwghatHtmlReport(date: AbstractDate): String = createHTML().html {
        val coordinates = coordinates ?: return@html
        attributes["lang"] = language.language
        attributes["dir"] = if (resources.isRtl) "rtl" else "ltr"
        head {
            meta(charset = "utf8")
            style {
                unsafe {
                    +"""
                        body { font-family: system-ui }
                        th, td { padding: 0 .5em; text-align: center }
                        td { border-top: 1px solid lightgray; font-size: 95% }
                        h1 { text-align: center; font-size: 110% }
                        table { margin: 0 auto; }
                    """.trimIndent()
                }
            }
        }
        body {
            h1 {
                +listOfNotNull(
                    context?.appPrefs?.cityName,
                    language.my.format(date.monthName, formatNumber(date.year))
                ).joinToString(spacedComma)
            }
            table {
                thead {
                    tr {
                        th { +getString(R.string.day) }
                        TIME_NAMES.forEach { th { +getString(it) } }
                    }
                }
                tbody {
                    (0 until mainCalendar.getMonthLength(date.year, date.month)).forEach { day ->
                        tr {
                            var prayTimes = coordinates.calculatePrayTimes(
                                Jdn(mainCalendar.createDate(date.year, date.month, day))
                                    .toJavaCalendar()
                            )
                            prayTimes = PrayTimeProvider(requireContext()).nReplace(
                                prayTimes,
                                Jdn(mainCalendar.createDate(date.year, date.month, day))
                            )!!
                            th { +formatNumber(day + 1) }
                            TIME_NAMES.forEach {
                                td { +prayTimes.getFromStringId(it).toBasicFormatString() }
                            }
                        }
                    }
                }
                if (calculationMethod != language.preferredCalculationMethod) {
                    tfoot {
                        tr { td { colSpan = "10"; +getString(calculationMethod.titleStringId) } }
                    }
                }
            }
            script { unsafe { +"print()" } }
        }
    }

    companion object {
        private const val CALENDARS_TAB = 0
        private const val EVENTS_TAB = 1
        private const val OWGHAT_TAB = 2
    }

    private fun shareOwghat() {
        val jdn = viewModel.selectedDay
        var prayTimes = coordinates?.calculatePrayTimes(jdn.value.toJavaCalendar())
        prayTimes = PrayTimeProvider(requireContext()).nReplace(prayTimes, jdn.value) ?: return
        val cityName = requireContext().appPrefs.cityName
        val dayLength = Clock.fromMinutesCount(
            prayTimes.getFromStringId(R.string.maghrib)
                .toMinutes() - prayTimes.getFromStringId(R.string.fajr).toMinutes()
        )
        val text =
            "\uD83D\uDD4C ${getString(R.string.owghat)} $cityName  \uD83D\uDD4C \r\n" +
                    "\uD83D\uDDD3 ${
                        dayTitleSummary(jdn.value, jdn.value.toCalendar(mainCalendar))
                    } \r\n" +
                    "${getString(R.string.fajr)} : ${
                        formatNumber(prayTimes.getFromStringId(R.string.fajr).toFormattedString())
                    }\r\n" +
                    "${getString(R.string.sunrise)} : ${
                        formatNumber(
                            prayTimes.getFromStringId(R.string.sunrise).toFormattedString()
                        )
                    }\r\n" +
                    "${getString(R.string.dhuhr)} : ${
                        formatNumber(prayTimes.getFromStringId(R.string.dhuhr).toFormattedString())
                    }\r\n" +
                    "${getString(R.string.asr)} : ${
                        formatNumber(prayTimes.getFromStringId(R.string.asr).toFormattedString())
                    }\r\n" +
                    "${getString(R.string.maghrib)} : ${
                        formatNumber(
                            prayTimes.getFromStringId(R.string.maghrib).toFormattedString()
                        )
                    }\r\n" +
                    "${getString(R.string.isha)} : ${
                        formatNumber(prayTimes.getFromStringId(R.string.isha).toFormattedString())
                    }\r\n" +
                    "${
                        getString(R.string.length_of_day) + spacedColon +
                                dayLength.asRemainingTime(resources, short = false)
                    } \r\n\r\n" +
                    appLink

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, resources.getString(R.string.share)))
    }//end of shareOwghat

    private fun takePhoto(owghatBinding: OwghatTabContentBinding) {
        runCatching {
            MediaPlayer().apply {
                setDataSource(
                    requireContext(),
                    (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                            resources.getResourcePackageName(R.raw.camera_shutter_click) + "/" +
                            resources.getResourceTypeName(R.raw.camera_shutter_click) + "/" +
                            resources.getResourceEntryName(R.raw.camera_shutter_click)).toUri()
                )
                setVolume(6f, 6f)
                prepare()
            }.start()
        }.onFailure(logException)
        runCatching {
            owghatBinding.let {

                owghatBinding.btnOwghatTackPhoto.isVisible = false
                owghatBinding.btnOwghatShare.isVisible = false
                owghatBinding.moonView.isVisible = false
                val prevBack = owghatBinding.root.background
                owghatBinding.root.setBackgroundColor(
                    requireContext().resolveColor(R.attr.colorBackground)
                )

                val photo = createBitmapFromView2(owghatBinding.root)

                owghatBinding.btnOwghatTackPhoto.isVisible = true
                owghatBinding.btnOwghatShare.isVisible = true
                if (viewModel.selectedDay.value != Jdn.today())
                    owghatBinding.moonView.isVisible = true
                owghatBinding.root.background = prevBack

                val path = requireContext().getExternalFilesDir("pic")?.absolutePath
                    ?: ""
                val f = File(path)
                if (!f.exists()) f.mkdirs()

                val file = File("$path/share.png")
                runCatching {
                    val out = FileOutputStream(file)
                    photo.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                }.onFailure(logException)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(
                        Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            requireContext(),
                            "${BuildConfig.APPLICATION_ID}.provider",
                            file
                        )
                    )
                    var text = "\uD83D\uDDD3 "
                    text += formatNumber(
                        dayTitleSummary(
                            viewModel.selectedDay.value,
                            viewModel.selectedDay.value.toCalendar(mainCalendar)
                        )
                    )
                    if (viewModel.selectedDay.value != Jdn.today()) {
                        val jdn = viewModel.selectedDay
                        var prayTimes = coordinates?.calculatePrayTimes(jdn.value.toJavaCalendar())
                        prayTimes =
                            PrayTimeProvider(requireContext()).nReplace(prayTimes, jdn.value)
                                ?: return
                        val dayLength = Clock.fromMinutesCount(
                            prayTimes.getFromStringId(R.string.maghrib)
                                .toMinutes() - prayTimes.getFromStringId(R.string.fajr)
                                .toMinutes()
                        )
                        text += "\r\n" + getString(R.string.length_of_day) + spacedColon +
                                dayLength.asRemainingTime(resources, short = false)
                    }

                    text += "\n\n${getString(R.string.app_name)}\n$appLink"
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "image/png"
                }
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        getString(R.string.share)
                    )
                )
            }
        }.onFailure(logException)
    }//end of takePhoto

    private val prayTimeRepository: PrayTimeRepository = get()
    private fun checkForAvailableTimes() {
        if (!isNetworkConnected(requireContext()) || PrayTimeProvider.ptFrom != 0) return
        runCatching {
            lifecycleScope.launch(Dispatchers.IO) {
                prayTimeRepository.getAddedCities().collect {
                    when (it.asDataState()) {
                        is DataState.Error -> {}
                        DataState.Loading -> {}
                        is DataState.Success -> {
                            val list = (it.asDataState() as DataState.Success<List<CityModel>>).data
                            val currentCity = requireContext().appPrefs.cityName
                            currentCity?.let { cityName ->
                                list.find { city -> city.name == cityName }?.let {
                                    withContext(Dispatchers.Main) {
                                        snackMessage(
                                            view = mainBinding?.root,
                                            message = getString(R.string.available_exact_times),
                                            clickAction = {
                                                requireContext().sendBroadcast(
                                                    Intent(NAVIGATE_TO_DOWNLOAD_FRAGMENT)
                                                )
                                            },
                                            snackAction = getString(R.string.download),
                                            snackTime = Snackbar.LENGTH_LONG
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

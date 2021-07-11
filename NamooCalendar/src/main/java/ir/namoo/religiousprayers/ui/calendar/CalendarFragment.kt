package ir.namoo.religiousprayers.ui.calendar

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
import android.net.Uri
import android.os.*
import android.provider.CalendarContract
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.github.persiancalendar.praytimes.Clock
import io.github.persiancalendar.praytimes.Coordinate
import ir.cepmuvakkit.times.posAlgo.SunMoonPosition
import ir.namoo.religiousprayers.*
import ir.namoo.religiousprayers.databinding.EventsTabContentBinding
import ir.namoo.religiousprayers.databinding.FragmentCalendarBinding
import ir.namoo.religiousprayers.databinding.OwghatTabContentBinding
import ir.namoo.religiousprayers.databinding.OwghatTabPlaceholderBinding
import ir.namoo.religiousprayers.entities.CalendarEvent
import ir.namoo.religiousprayers.praytimes.PrayTimeProvider
import ir.namoo.religiousprayers.ui.DrawerHost
import ir.namoo.religiousprayers.ui.calendar.calendarpager.CalendarPager
import ir.namoo.religiousprayers.ui.calendar.dialogs.showDayPickerDialog
import ir.namoo.religiousprayers.ui.calendar.dialogs.showMonthOverviewDialog
import ir.namoo.religiousprayers.ui.calendar.dialogs.showShiftWorkDialog
import ir.namoo.religiousprayers.ui.calendar.searchevent.SearchEventsAdapter
import ir.namoo.religiousprayers.ui.calendar.times.TimeItemAdapter
import ir.namoo.religiousprayers.ui.downup.CityList
import ir.namoo.religiousprayers.ui.preferences.INTERFACE_CALENDAR_TAB
import ir.namoo.religiousprayers.ui.preferences.LOCATION_ATHAN_TAB
import ir.namoo.religiousprayers.ui.shared.CalendarsView
import ir.namoo.religiousprayers.utils.Jdn
import ir.namoo.religiousprayers.utils.allEnabledEvents
import ir.namoo.religiousprayers.utils.appPrefs
import ir.namoo.religiousprayers.utils.appPrefsLite
import ir.namoo.religiousprayers.utils.askForCalendarPermission
import ir.namoo.religiousprayers.utils.calculationMethod
import ir.namoo.religiousprayers.utils.calendarType
import ir.namoo.religiousprayers.utils.createBitmapFromView2
import ir.namoo.religiousprayers.utils.dayTitleSummary
import ir.namoo.religiousprayers.utils.debugAssertNotNull
import ir.namoo.religiousprayers.utils.emptyEventsStore
import ir.namoo.religiousprayers.utils.formatDeviceCalendarEventTitle
import ir.namoo.religiousprayers.utils.formatNumber
import ir.namoo.religiousprayers.utils.getA11yDaySummary
import ir.namoo.religiousprayers.utils.getAllEnabledAppointments
import ir.namoo.religiousprayers.utils.getAppFont
import ir.namoo.religiousprayers.utils.getCityName
import ir.namoo.religiousprayers.utils.getCompatDrawable
import ir.namoo.religiousprayers.utils.getCoordinate
import ir.namoo.religiousprayers.utils.getEnabledCalendarTypes
import ir.namoo.religiousprayers.utils.getEvents
import ir.namoo.religiousprayers.utils.getEventsTitle
import ir.namoo.religiousprayers.utils.getShiftWorkTitle
import ir.namoo.religiousprayers.utils.isHighTextContrastEnabled
import ir.namoo.religiousprayers.utils.isNetworkConnected
import ir.namoo.religiousprayers.utils.isPackageInstalled
import ir.namoo.religiousprayers.utils.isShowDeviceCalendarEvents
import ir.namoo.religiousprayers.utils.isTalkBackEnabled
import ir.namoo.religiousprayers.utils.language
import ir.namoo.religiousprayers.utils.logException
import ir.namoo.religiousprayers.utils.mainCalendar
import ir.namoo.religiousprayers.utils.monthName
import ir.namoo.religiousprayers.utils.navigateSafe
import ir.namoo.religiousprayers.utils.onClick
import ir.namoo.religiousprayers.utils.readDayDeviceEvents
import ir.namoo.religiousprayers.utils.resolveColor
import ir.namoo.religiousprayers.utils.startAthan
import ir.namoo.religiousprayers.utils.toFormattedString
import ir.namoo.religiousprayers.utils.toJavaCalendar
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileOutputStream
import java.util.*

private const val CALENDARS_TAB = 0
private const val EVENTS_TAB = 1
private const val OWGHAT_TAB = 2

class CalendarFragment : Fragment() {

    private var coordinate: Coordinate? = null
    private var mainBinding: FragmentCalendarBinding? = null
    private var calendarsView: CalendarsView? = null
    private var owghatBinding: OwghatTabContentBinding? = null
    private var eventsBinding: EventsTabContentBinding? = null
    private var searchView: SearchView? = null
    private var todayButton: MenuItem? = null
    private val initialDate = Jdn.today.toCalendar(mainCalendar)

    override fun onDestroyView() {
        super.onDestroyView()
        coordinate = null
        mainBinding = null
        calendarsView = null
        owghatBinding = null
        eventsBinding = null
        searchView = null
        todayButton = null
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FragmentCalendarBinding.inflate(inflater, container, false).also { binding ->
        mainBinding = binding

        val coordinate = getCoordinate(inflater.context)
        this.coordinate = coordinate

        val appPrefs = inflater.context.appPrefs
        val shouldDisableOwghatTab = coordinate == null && // if coordinates is set, should be shown
                (language != LANG_FA || // The placeholder isn't translated to other languages
                        // The user is already dismissed the third tab
                        appPrefs.getBoolean(PREF_DISABLE_OWGHAT, false) ||
                        // Try to not show the placeholder to established users
                        PREF_APP_LANGUAGE in appPrefs)

        val tabs = listOf(
            // First tab
            R.string.calendar to CalendarsView(inflater.context).also { this.calendarsView = it },

            // Second tab
            R.string.events to EventsTabContentBinding.inflate(
                inflater, container, false
            ).also { eventsBinding ->
                this.eventsBinding = eventsBinding
                eventsBinding.eventsContent.layoutTransition = LayoutTransition().also {
                    it.enableTransitionType(LayoutTransition.CHANGING)
                    it.setAnimateParentHierarchy(false)
                }
            }.root
        ) + if (shouldDisableOwghatTab) emptyList() else listOf(
            // The optional third tab
            R.string.owghat to if (coordinate == null) {
                OwghatTabPlaceholderBinding.inflate(
                    inflater, container, false
                ).also { owghatBindingPlaceholder ->
                    owghatBindingPlaceholder.activate.setOnClickListener {
                        findNavController().navigateSafe(
                            CalendarFragmentDirections.navigateToSettings(LOCATION_ATHAN_TAB)
                        )
                    }
                    owghatBindingPlaceholder.discard.setOnClickListener {
                        context?.appPrefs?.edit { putBoolean(PREF_DISABLE_OWGHAT, true) }
                        findNavController().navigateSafe(CalendarFragmentDirections.navigateToSelf())
                    }
                }.root
            } else {
                OwghatTabContentBinding.inflate(
                    inflater, container, false
                ).also { owghatBinding ->
                    this.owghatBinding = owghatBinding

                    owghatBinding.root.setOnClickListener { onOwghatClick() }

                    owghatBinding.cityName.run {
                        setOnClickListener { onOwghatClick() }
                        // Easter egg to test AthanActivity
                        setOnLongClickListener {
                            startAthan(
                                requireContext(),
                                listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA").random(),
                                Clock(Calendar.getInstance(Locale.getDefault())).toFormattedString()
                            )
                            true
                        }
                        var cityName = getCityName(context, false)
                        cityName += "( ${
                            when (PrayTimeProvider.ptFrom) {
                                0 -> context.resources.getString(R.string.calculated_time)
                                1 -> context.resources.getString(R.string.exact_time)
                                else -> context.resources.getString(R.string.edited_time)
                            }
                        })"
                        if (cityName.isNotEmpty()) text = cityName

                        this.setTextColor(
                            when (PrayTimeProvider.ptFrom) {
                                2 -> requireContext().resolveColor(R.attr.colorWarning)
                                0 -> requireContext().resolveColor(R.attr.colorTextHoliday)
                                else -> requireContext().resolveColor(R.attr.colorTextPrimary)
                            }
                        )

                    }
                    owghatBinding.btnOwghatShare.setOnClickListener {
                        it.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                com.google.android.material.R.anim.abc_fade_in
                            )
                        )
                        shareOwghat()
                    }
                    owghatBinding.btnOwghatTackPhoto.setOnClickListener {
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
                                val prevBack = owghatBinding.root.background
                                owghatBinding.root.setBackgroundColor(
                                    requireContext().resolveColor(R.attr.colorBackground)
                                )

                                val photo = createBitmapFromView2(owghatBinding.root)

                                owghatBinding.btnOwghatTackPhoto.isVisible = true
                                owghatBinding.btnOwghatShare.isVisible = true
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
                                            selectedJdn,
                                            selectedJdn.toCalendar(mainCalendar)
                                        )
                                    )
                                    if (selectedJdn != Jdn.today) {
                                        val times = PrayTimeProvider.calculate(
                                            calculationMethod,
                                            selectedJdn,
                                            coordinate,
                                            requireContext()
                                        )
                                        val length =
                                            Clock.fromInt(times.maghribClock.toInt() - times.fajrClock.toInt())
                                        text += "\n${
                                            formatNumber(
                                                String.format(
                                                    getString(R.string.length_of_day),
                                                    length.hour, length.minute
                                                )
                                            )
                                        }"
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
                    }
                    owghatBinding.timesRecyclerView.run {
                        layoutManager = LinearLayoutManager(requireContext())
//                            FlexboxLayoutManager(context).apply {
//                            flexWrap = FlexWrap.NOWRAP
//                            justifyContent = JustifyContent.CENTER
//                        }
                        adapter = TimeItemAdapter(childFragmentManager)
                    }
                }.root
            }
        )

        // tabs should fill their parent otherwise view pager can't handle it
        tabs.forEach { (_: Int, tabView: ViewGroup) ->
            tabView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        binding.calendarPager.also {
            it.onDayClicked = fun(jdn: Jdn) { bringDate(jdn, monthChange = false) }
            it.onDayLongClicked = fun(jdn: Jdn) { addEventOnCalendar(jdn) }
            it.onMonthSelected = fun() {
                it.selectedMonth.let { date ->
                    updateToolbar(date.monthName, formatNumber(date.year))
                    todayButton?.isVisible =
                        date.year != initialDate.year || date.month != initialDate.month
                }
            }
        }

        binding.viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount(): Int = tabs.size
            override fun getItemViewType(position: Int) = position // set viewtype equal to position
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(tabs[viewType].second) {}
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, i ->
            tab.setText(tabs[i].first)
        }.attach()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == OWGHAT_TAB) {
                    owghatBinding?.sunView?.startAnimate()
                    Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
                        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter)?.run {
                            isExpanded = true
                        }
                    }, 1500)
                } else owghatBinding?.sunView?.clear()
                context?.appPrefsLite?.edit { putInt(LAST_CHOSEN_TAB_KEY, position) }
            }
        })

        var lastTab = inflater.context.appPrefsLite.getInt(LAST_CHOSEN_TAB_KEY, CALENDARS_TAB)
        if (lastTab >= tabs.size) lastTab = CALENDARS_TAB
        binding.viewPager.setCurrentItem(lastTab, true)
        setupMenu(binding.appBar.toolbar, binding.calendarPager)
        //################################### notify for generated and edited time if exact is available
        runCatching {
            if (isNetworkConnected(requireContext()) && PrayTimeProvider.ptFrom != 1)
                GetAvailableCitiesTask(
                    requireContext().appPrefs.getString(
                        PREF_GEOCODED_CITYNAME,
                        DEFAULT_CITY
                    ) ?: DEFAULT_CITY
                ).execute()
        }.onFailure(logException)
    }.root

    private fun shareOwghat() {
        val times = PrayTimeProvider.calculate(
            calculationMethod,
            selectedJdn,
            coordinate!!,
            requireContext()
        )
        val cityName = requireContext().appPrefs.getString(PREF_GEOCODED_CITYNAME, DEFAULT_CITY)
            ?: DEFAULT_CITY
        val dayLength = Clock.fromInt((times.sunsetClock.toInt() - times.fajrClock.toInt()))
        val text =
            "\uD83D\uDD4C ${resources.getString(R.string.owghat)} $cityName  \uD83D\uDD4C \r\n" +
                    "\uD83D\uDDD3 ${
                        dayTitleSummary(
                            selectedJdn,
                            selectedJdn.toCalendar(mainCalendar)
                        )
                    } \r\n" +
                    "${resources.getString(R.string.fajr)} : ${formatNumber(times.fajrClock.toFormattedString())}\r\n" +
                    "${resources.getString(R.string.sunrise)} : ${formatNumber(times.sunriseClock.toFormattedString())}\r\n" +
                    "${resources.getString(R.string.dhuhr)} : ${formatNumber(times.dhuhrClock.toFormattedString())}\r\n" +
                    "${resources.getString(R.string.asr)} : ${formatNumber(times.asrClock.toFormattedString())}\r\n" +
                    "${resources.getString(R.string.maghrib)} : ${formatNumber(times.maghribClock.toFormattedString())}\r\n" +
                    "${resources.getString(R.string.isha)} : ${formatNumber(times.ishaClock.toFormattedString())}\r\n" +
                    "${
                        formatNumber(
                            String.format(
                                resources.getString(R.string.length_of_day),
                                dayLength.hour,
                                dayLength.minute
                            )
                        )
                    } \r\n\r\n" +
                    appLink

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, resources.getString(R.string.share)))
    }//end of shareOwghat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bringDate(Jdn.today, monthChange = false, highlight = false)

        mainBinding?.let {
            (activity as? DrawerHost)?.setupToolbarWithDrawer(viewLifecycleOwner, it.appBar.toolbar)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                it.appBar.appbarLayout.outlineProvider = null
        }

        Jdn.today.toCalendar(mainCalendar).let { today ->
            updateToolbar(today.monthName, formatNumber(today.year))
        }
    }

    private fun addEventOnCalendar(jdn: Jdn) {
        val activity = activity ?: return
        if (ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) askForCalendarPermission(activity) else {
            runCatching { addEvent.launch(jdn) }.onFailure(logException).onFailure {
                Snackbar.make(
                    mainBinding?.root ?: return, R.string.device_calendar_does_not_support,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateToolbar(title: String, subTitle: String) {
        mainBinding?.appBar?.toolbar?.let {
            it.title = title
            it.subtitle = subTitle
        }
    }

    private val addEvent =
        registerForActivityResult(object : ActivityResultContract<Jdn, Void>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
            override fun createIntent(context: Context, jdn: Jdn): Intent {
                val time = jdn.toJavaCalendar().timeInMillis
                return Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(
                        CalendarContract.Events.DESCRIPTION, dayTitleSummary(
                            jdn, jdn.toCalendar(mainCalendar)
                        )
                    )
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, time)
                    .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            }
        }) { mainBinding?.calendarPager?.refresh(isEventsModified = true) }

    private val viewEvent =
        registerForActivityResult(object : ActivityResultContract<Long, Void>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
            override fun createIntent(context: Context, id: Long): Intent =
                Intent(Intent.ACTION_VIEW).setData(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
                )
        }) { mainBinding?.calendarPager?.refresh(isEventsModified = true) }

    override fun onResume() {
        super.onResume()
        // If events are enabled refresh the pager events on resumes anyway
        if (isShowDeviceCalendarEvents) mainBinding?.calendarPager?.refresh(isEventsModified = true)
    }

    private fun getDeviceEventsTitle(dayEvents: List<CalendarEvent<*>>) = buildSpannedString {
        dayEvents.filterIsInstance<CalendarEvent.DeviceCalendarEvent>().forEachIndexed { i, event ->
            if (i != 0) append("\n")
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
            }) { append(formatDeviceCalendarEventTitle(event)) }
        }
    }

    private var selectedJdn = Jdn.today

    private fun bringDate(jdn: Jdn, highlight: Boolean = true, monthChange: Boolean = true) {
        selectedJdn = jdn

        mainBinding?.calendarPager?.setSelectedDay(jdn, highlight, monthChange)

        val isToday = Jdn.today == jdn

        // Show/Hide bring today menu button
        todayButton?.isVisible = !isToday

        // Update tabs
        calendarsView?.showCalendars(jdn, mainCalendar, getEnabledCalendarTypes())
        showEvent(jdn)
        setOwghat(jdn, isToday)

        // a11y
        if (isTalkBackEnabled && !isToday && monthChange) Snackbar.make(
            mainBinding?.root ?: return,
            getA11yDaySummary(
                context ?: return, jdn, false, emptyEventsStore(),
                withZodiac = true, withOtherCalendars = true, withTitle = true
            ),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEvent(jdn: Jdn) {
        val activity = activity ?: return
        val eventsBinding = eventsBinding ?: return

        eventsBinding.shiftWorkTitle.text = getShiftWorkTitle(jdn, false)
        val events = jdn.getEvents(jdn.readDayDeviceEvents(activity))
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

        eventsBinding.eventMessage.isVisible = false
        eventsBinding.noEvent.isVisible = true

        if (holidays.isNotEmpty()) {
            eventsBinding.noEvent.isVisible = false
            eventsBinding.holidayTitle.text = holidays
            val holidayContent = getString(R.string.holiday_reason) + "\n" + holidays
            eventsBinding.holidayTitle.contentDescription = holidayContent
            contentDescription.append(holidayContent)
            eventsBinding.holidayTitle.isVisible = true
        } else {
            eventsBinding.holidayTitle.isVisible = false
        }

        if (deviceEvents.isNotEmpty()) {
            eventsBinding.noEvent.isVisible = false
            eventsBinding.deviceEventTitle.text = deviceEvents
            contentDescription
                .append("\n")
                .append(getString(R.string.show_device_calendar_events))
                .append("\n")
                .append(deviceEvents)

            eventsBinding.deviceEventTitle.let {
                it.movementMethod = LinkMovementMethod.getInstance()
                it.isVisible = true
            }
        } else {
            eventsBinding.deviceEventTitle.isVisible = false
        }

        if (nonHolidays.isNotEmpty()) {
            eventsBinding.noEvent.isVisible = false
            eventsBinding.eventTitle.text = nonHolidays
            contentDescription
                .append("\n")
                .append(getString(R.string.events))
                .append("\n")
                .append(nonHolidays)

            eventsBinding.eventTitle.isVisible = true
        } else {
            eventsBinding.eventTitle.isVisible = false
        }

        val messageToShow = SpannableStringBuilder()

        val enabledTypes = activity.appPrefs
            .getStringSet(PREF_HOLIDAY_TYPES, null) ?: emptySet()
        if (enabledTypes.isEmpty()) {
            eventsBinding.noEvent.isVisible = false
            if (messageToShow.isNotEmpty()) messageToShow.append("\n")

            val title = getString(R.string.warn_if_events_not_set)
            messageToShow.append(buildSpannedString {
                inSpans(object : ClickableSpan() {
                    override fun onClick(textView: View) = findNavController().navigateSafe(
                        CalendarFragmentDirections.navigateToSettings(
                            INTERFACE_CALENDAR_TAB, PREF_HOLIDAY_TYPES
                        )
                    )
                }) { append(title) }
            })

            contentDescription
                .append("\n")
                .append(title)
        }

        if (messageToShow.isNotEmpty()) {
            eventsBinding.eventMessage.let {
                it.text = messageToShow
                it.movementMethod = LinkMovementMethod.getInstance()
                it.isVisible = true
            }
        }

        eventsBinding.root.contentDescription = contentDescription
    }

    private fun setOwghat(jdn: Jdn, isToday: Boolean) {
        if (coordinate == null) return

        val prayTimes = PrayTimeProvider.calculate(
            calculationMethod, jdn, coordinate!!, requireContext()
        )
        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter)?.isToday = isToday
        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter)?.prayTimes = prayTimes
        owghatBinding?.sunView?.run {
            setSunriseSunsetMoonPhase(prayTimes, runCatching {
                coordinate?.run {
                    SunMoonPosition(
                        jdn.value.toDouble(), latitude,
                        longitude, 0.0, 0.0
                    ).moonPhase
                }
            }.onFailure(logException).getOrNull() ?: 1.0)
            isVisible = isToday
            if (isToday && mainBinding?.viewPager?.currentItem == OWGHAT_TAB) startAnimate()
        }
    }

    private fun onOwghatClick() {
        (owghatBinding?.timesRecyclerView?.adapter as? TimeItemAdapter)?.apply {
            isExpanded = true
//            owghatBinding?.moreOwghat?.animate()
//                ?.rotation(if (isExpanded) 180f else 0f)
//                ?.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
//                ?.start()
        }
    }

    private fun setupMenu(toolbar: Toolbar, calendarPager: CalendarPager) {
        val context = toolbar.context

        val searchView = SearchView(context)
        searchView.setOnCloseListener {
            onBackPressedCloseSearchCallback.isEnabled = false
            false // don't prevent the event cascade
        }
        searchView.setOnSearchClickListener {
            onBackPressedCloseSearchCallback.isEnabled = true
            // Remove search edit view below bar
            searchView.findViewById<View?>(androidx.appcompat.R.id.search_plate)
                ?.setBackgroundColor(Color.TRANSPARENT)

            val searchAutoComplete = searchView.findViewById<SearchAutoComplete?>(
                androidx.appcompat.R.id.search_src_text
            ).debugAssertNotNull
            searchAutoComplete?.setHint(R.string.search_in_events)
            val events = allEnabledEvents + getAllEnabledAppointments(context)
            searchAutoComplete?.setAdapter(SearchEventsAdapter(context, events))
            searchAutoComplete?.setOnItemClickListener { parent, _, position, _ ->
                val date = (parent.getItemAtPosition(position) as CalendarEvent<*>).date
                val type = date.calendarType
                val today = Jdn.today.toCalendar(type)
                bringDate(
                    Jdn(
                        type, if (date.year == -1)
                            (today.year + if (date.month < today.month) 1 else 0)
                        else date.year, date.month, date.dayOfMonth
                    )
                )
                searchView.onActionViewCollapsed()
            }
        }
        this.searchView = searchView

        toolbar.menu.add(R.string.return_to_today).also {
            it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_restore_modified)
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.isVisible = false
            it.onClick { bringDate(Jdn.today, highlight = false) }
            todayButton = it
        }
        toolbar.menu.add(R.string.search_in_events).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.actionView = searchView
        }
        toolbar.menu.add(R.string.support).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            it.icon = toolbar.context.getCompatDrawable(R.drawable.ic_support)
            it.onClick {
                val url = "http://www.namoo.ir/FAQ"
                CustomTabsIntent.Builder().build().apply {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (isPackageInstalled(
                            "com.android.chrome",
                            requireContext().packageManager
                        )
                    )
                        intent.setPackage("com.android.chrome")
                }.launchUrl(requireActivity(), Uri.parse(url))
            }
        }
        toolbar.menu.add(R.string.goto_date).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick { showDayPickerDialog(selectedJdn, R.string.go) { jdn -> bringDate(jdn) } }
        }
        toolbar.menu.add(R.string.add_event).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick { addEventOnCalendar(selectedJdn) }
        }
        toolbar.menu.add(R.string.shift_work_settings).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick { showShiftWorkDialog(selectedJdn) }
        }
        toolbar.menu.add(R.string.month_overview).also {
            it.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            it.onClick { showMonthOverviewDialog(calendarPager.selectedMonth) }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetAvailableCitiesTask(val cityName: String) :
        AsyncTask<String?, Int?, String?>() {
        private val list = arrayListOf<CityList>()

        override fun doInBackground(vararg strings: String?): String = runCatching {
            val httpclient: HttpClient = DefaultHttpClient()
            val httpGet = HttpGet("http://www.namoo.ir/Home/GetAddedCities")
            val response: HttpResponse = httpclient.execute(httpGet)
            if (response.statusLine.statusCode == 200) {
                val serverResponse = EntityUtils.toString(response.entity)
                val parser = JSONParser()
                val jsonArray: JSONArray = parser.parse(serverResponse) as JSONArray
                val jsonObjectIterator: MutableIterator<Any?> = jsonArray.iterator()
                list.clear()
                while (jsonObjectIterator.hasNext()) {
                    val jt: JSONObject = jsonObjectIterator.next() as JSONObject
                    val t = CityList()
                    t.id = jt["id"].toString().toInt()
                    t.name = jt["cityName"].toString()
                    t.setInsertDate(jt["lastUpdate"].toString())
                    list.add(t)
                }
                list.sortBy { it.name }
            } else return "error"
        }.onFailure(logException).getOrDefault("OK").toString()

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            runCatching {
                val city = list.find { it.name == cityName.trimStart().trimEnd() }
                if (city != null) {
                    val snackBar =
                        Snackbar.make(
                            mainBinding?.root ?: return,
                            R.string.exact_time_is_available,
                            Snackbar.LENGTH_INDEFINITE
                        ).apply {
                            (view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView)
                                .typeface = getAppFont(requireContext())
                            setAction(R.string.download) { _ ->
                                val intent = Intent(NAVIGATE_TO_UD)
                                requireActivity().sendBroadcast(intent)
                            }
                            view.setBackgroundColor(
                                requireContext().resolveColor(R.attr.colorSnack)
                            )
                            view.setOnClickListener {
                                dismiss()
                                val intent = Intent(NAVIGATE_TO_UD)
                                requireActivity().sendBroadcast(intent)
                            }
                        }.show()
                }
            }.onFailure(logException)
        }

    } //end of class GetAvailableCitiesTask
}//end of class CalendarFragment

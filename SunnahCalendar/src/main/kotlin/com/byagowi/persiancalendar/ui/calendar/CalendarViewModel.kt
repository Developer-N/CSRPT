package com.byagowi.persiancalendar.ui.calendar

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byagowi.persiancalendar.LAST_CHOSEN_TAB_KEY
import com.byagowi.persiancalendar.entities.CalendarEvent
import com.byagowi.persiancalendar.entities.Jdn
import com.byagowi.persiancalendar.ui.calendar.searchevent.SearchEventsRepository
import com.byagowi.persiancalendar.ui.calendar.shiftwork.ShiftWorkViewModel
import com.byagowi.persiancalendar.ui.calendar.yearview.YearViewCommand
import com.byagowi.persiancalendar.ui.resumeToken
import com.byagowi.persiancalendar.utils.HALF_SECOND_IN_MILLIS
import com.byagowi.persiancalendar.utils.THIRTY_SECONDS_IN_MILLIS
import com.byagowi.persiancalendar.utils.preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val _selectedDay = MutableStateFlow(Jdn.today())
    val selectedDay: StateFlow<Jdn> get() = _selectedDay

    private val _selectedMonthOffset = MutableStateFlow(0)
    val selectedMonthOffset: StateFlow<Int> get() = _selectedMonthOffset

    private val _selectedMonthOffsetCommand = MutableStateFlow<Int?>(null)
    val selectedMonthOffsetCommand: StateFlow<Int?> get() = _selectedMonthOffsetCommand

    private val _selectedTabIndex = MutableStateFlow(CALENDARS_TAB)
    val selectedTabIndex: StateFlow<Int> get() = _selectedTabIndex

    private val _isSearchOpenFlow = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> get() = _isSearchOpenFlow

    private val _eventsFlow = MutableStateFlow<List<CalendarEvent<*>>>(emptyList())
    val eventsFlow: StateFlow<List<CalendarEvent<*>>> get() = _eventsFlow

    private val _refreshToken = MutableStateFlow(0)
    val refreshToken: StateFlow<Int> get() = _refreshToken

    private val _isHighlighted = MutableStateFlow(false)
    val isHighlighted: StateFlow<Boolean> get() = _isHighlighted

    private val _removedThirdTab = MutableStateFlow(false)
    val removedThirdTab: StateFlow<Boolean> get() = _removedThirdTab

    private val _shiftWorkViewModel = MutableStateFlow<ShiftWorkViewModel?>(null)
    val shiftWorkViewModel: StateFlow<ShiftWorkViewModel?> get() = _shiftWorkViewModel

    private val _sunViewNeedAnimation = MutableStateFlow(false)
    val sunViewNeedsAnimation: StateFlow<Boolean> get() = _sunViewNeedAnimation

    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> get() = _now

    private val _todayButtonVisibility = MutableStateFlow(false)
    val todayButtonVisibility: StateFlow<Boolean> get() = _todayButtonVisibility

    private val _today = MutableStateFlow(Jdn.today())
    val today: StateFlow<Jdn> get() = _today

    private val _isYearView = MutableStateFlow(false)
    val isYearView: StateFlow<Boolean> get() = _isYearView

    private val _yearViewCommand = MutableStateFlow<YearViewCommand?>(null)
    val yearViewCommand: StateFlow<YearViewCommand?> get() = _yearViewCommand

    private val _yearViewOffset = MutableStateFlow(0)
    val yearViewOffset: StateFlow<Int> get() = _yearViewOffset

    private val _yearViewIsInYearSelection = MutableStateFlow(false)
    val yearViewIsInYearSelection: StateFlow<Boolean> get() = _yearViewIsInYearSelection

    private val _daysScreenSelectedDay = MutableStateFlow<Jdn?>(null)
    val daysScreenSelectedDay: StateFlow<Jdn?> get() = _daysScreenSelectedDay

    // Commands
    fun changeDaysScreenSelectedDay(jdn: Jdn?) {
        jdn?.let { changeSelectedDay(it) }
        _daysScreenSelectedDay.value = jdn
    }

    fun changeSelectedMonthOffsetCommand(offset: Int?) {
        _selectedMonthOffsetCommand.value = offset
    }

    fun notifyYearViewOffset(value: Int) {
        _yearViewOffset.value = value
    }

    /**
     * This is just to notify readers of selectedMonthOffset,
     * use [changeSelectedMonthOffsetCommand] for actually altering viewpager's state
     */
    fun notifySelectedMonthOffset(offset: Int) {
        _selectedMonthOffset.value = offset
    }

    fun changeSelectedDay(jdn: Jdn) {
        _isHighlighted.value = true
        _selectedDay.value = jdn
    }

    fun clearHighlightedDay() {
        _isHighlighted.value = false
    }

    fun changeSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    fun refreshCalendar() {
        ++_refreshToken.value
    }

    fun removeThirdTab() {
        _removedThirdTab.value = true
    }

    fun openSearch() {
        _isSearchOpenFlow.value = true
    }

    fun closeSearch() {
        _isSearchOpenFlow.value = false
    }

    fun setShiftWorkViewModel(shiftWorkViewModel: ShiftWorkViewModel?) {
        _shiftWorkViewModel.value = shiftWorkViewModel
    }

    fun clearNeedsAnimation() {
        _sunViewNeedAnimation.value = false
    }

    fun astronomicalOverviewLaunched() {
        _sunViewNeedAnimation.value = true
    }

    fun openYearView() {
        _isYearView.value = true
    }

    fun closeYearView() {
        _isYearView.value = false
    }

    private var repository: SearchEventsRepository? = null

    fun searchEvent(query: CharSequence) {
        viewModelScope.launch { _eventsFlow.value = repository?.findEvent(query) ?: emptyList() }
    }

    // Events store cache needs to be invalidated as preferences of enabled events can be changed
    // or user has added an appointment on their calendar outside the app.
    fun initializeEventsRepository() {
        repository = SearchEventsRepository(getApplication())
    }

    fun commandYearView(command: YearViewCommand) {
        _yearViewCommand.value = command
    }

    fun onYearViewBackPressed() {
        if (yearViewIsInYearSelection.value) commandYearView(YearViewCommand.ToggleYearSelection)
        else closeYearView()
    }

    fun clearYearViewCommand() {
        _yearViewCommand.value = null
    }

    fun yearViewIsInYearSelection(value: Boolean) {
        _yearViewIsInYearSelection.value = value
    }

    init {
        viewModelScope.launch {
            val preferences = application.preferences
            changeSelectedTabIndex(preferences.getInt(LAST_CHOSEN_TAB_KEY, 0))
            selectedTabIndex.collectLatest { preferences.edit { putInt(LAST_CHOSEN_TAB_KEY, it) } }
        }
        viewModelScope.launch {
            selectedTabIndex.combine(selectedDay) { tabIndex, day ->
                tabIndex == TIMES_TAB && day == today.value
            }.collect { if (it) _sunViewNeedAnimation.value = true }
        }
        viewModelScope.launch {
            while (true) {
                delay(THIRTY_SECONDS_IN_MILLIS)
                _now.value = System.currentTimeMillis()
                val today = Jdn.today()
                if (_today.value != today) {
                    refreshCalendar()
                    _today.value = today
                    if (!isHighlighted.value) _selectedDay.value = today
                }
            }
        }
        viewModelScope.launch {
            resumeToken.collect {
                delay(HALF_SECOND_IN_MILLIS)
                refreshCalendar()
                delay(HALF_SECOND_IN_MILLIS)
                refreshCalendar()
            }
        }
        viewModelScope.launch {
            merge(selectedMonthOffset, isHighlighted, isYearView).collectLatest {
                _todayButtonVisibility.value = if (isYearView.value)
                    true
                else selectedMonthOffset.value != 0 || isHighlighted.value
            }
        }
    }
}

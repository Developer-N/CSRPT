package ir.namoo.quran.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.namoo.quran.chapters.data.ChapterEntity
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.sura.data.QuranEntity
import ir.namoo.quran.sura.data.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val quranRepository: QuranRepository, private val chapterRepository: ChapterRepository
) : ViewModel() {
    private val _notes = MutableStateFlow(mutableListOf<QuranEntity>())
    val notes = _notes.asStateFlow()

    private val _chapters = MutableStateFlow(listOf<ChapterEntity>())
    val chapters = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _chapters.value = chapterRepository.getAllChapters()
            _notes.value = quranRepository.getNotes().toMutableList()
            _isLoading.value = false
        }
    }

    fun updateNoteInDB(quranEntity: QuranEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            quranRepository.updateQuran(quranEntity)
            _isLoading.value = false
        }
    }

    fun deleteNote(quranEntity: QuranEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            quranEntity.note = null
            quranRepository.updateQuran(quranEntity)
            _notes.value.remove(quranEntity)
            _isLoading.value = false
        }
    }

}

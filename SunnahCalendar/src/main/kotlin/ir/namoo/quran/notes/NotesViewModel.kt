package ir.namoo.quran.notes

import androidx.compose.runtime.mutableStateListOf
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
    private val _notes = mutableStateListOf<QuranEntity>()
    val notes = _notes

    private val _chapters = mutableStateListOf<ChapterEntity>()
    val chapters = _chapters

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _chapters.clear()
            _chapters.addAll(chapterRepository.getAllChapters())
            _notes.clear()
            _notes.addAll(quranRepository.getNotes().toMutableList())
            _isLoading.value = false
        }
    }

    fun updateNote(quranEntity: QuranEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            quranRepository.updateQuran(quranEntity)
            val index = _notes.indexOfFirst { it.id == quranEntity.id }
            if (index >= 0)
                _notes[index] = quranEntity
            _isLoading.value = false
        }
    }

    fun deleteNote(quranEntity: QuranEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            quranRepository.updateQuran(quranEntity.copy(note = null))
            _notes.remove(quranEntity)
            _isLoading.value = false
        }
    }

}

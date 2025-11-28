package ir.namoo.quran.mushaf

import androidx.lifecycle.ViewModel
import ir.namoo.quran.chapters.data.ChapterRepository
import ir.namoo.quran.sura.data.QuranRepository

class MushafPageViewModel(
    private val quranRepository: QuranRepository,
    private val chapterRepository: ChapterRepository
) : ViewModel() {

}

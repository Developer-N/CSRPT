package ir.namoo.quran.sura.data

data class TranslateItem(
    val ayaID: Int, val name: String, val text: String, val translateType: TranslateType
)

enum class TranslateType {
    KURDISH, FARSI, ENGLISH
}

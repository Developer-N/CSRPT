package ir.namoo.hadeeth.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =========================================== Language
@Serializable
data class Language(
    val code: String, val native: String
)

// =========================================== Category
@Serializable
data class Category(
    val id: String,
    val title: String,
    @SerialName("hadeeths_count")
    val hadeethsCount: String,
    @SerialName("parent_id")
    val parentID: String?
)

// =========================================== Hadeeth
@Serializable
data class HadeethChapter(
    val id: String,
    val title: String,
    val translations: List<String>
)

@Serializable
data class HadeethList(
    val data: List<HadeethChapter>,
    val meta: Meta
)

@Serializable
data class Meta(
    @SerialName("current_page")
    val currentPage: Int,
    @SerialName("last_page")
    val lastPage: Int,
    @SerialName("total_items")
    val totalItems: Int,
    @SerialName("per_page")
    val perPage: Int
)

@Serializable
data class Hadeeth(
    val id: String,
    val title: String,
    val hadeeth: String,
    val attribution: String,
    val grade: String,
    val explanation: String,
    val hints: List<String>,
    val categories: List<String>,
    val translations: List<String>,
    @SerialName("hadeeth_intro")
    val hadeethIntro: String? = null,
    @SerialName("hadeeth_ar")
    val hadeethAr: String? = null,
    @SerialName("hadeeth_intro_ar")
    val hadeethIntroAr: String? = null,
    @SerialName("explanation_ar")
    val explanationAr: String? = null,
    @SerialName("hints_ar")
    val hintsAr: List<String>? = emptyList(),
    @SerialName("words_meanings_ar")
    val wordsMeaningsAr: List<WordsMeaning>? = emptyList(),
    @SerialName("attribution_ar")
    val attributionAr: String? = null,
    @SerialName("grade_ar")
    val gradeAr: String? = null,
    @SerialName("words_meanings")
    val wordsMeanings: List<WordsMeaning>? = emptyList(),
    @SerialName("reference")
    val reference: String? = null
)

@Serializable
data class WordsMeaning(
    val word: String,
    val meaning: String
) {
    override fun toString(): String {
        return "$word: $meaning"
    }
}


// ++++++++++++++++++++++++++++++++++++++++++++++ Utils

fun List<Language>.toLanguageEntities(): List<LanguageEntity> {
    val result = mutableListOf<LanguageEntity>()
    this.forEach {
        result.add(LanguageEntity(0, it.code, it.native))
    }
    return result
}

fun List<Category>.toCategoryEntities(language: String): List<CategoryEntity> {
    val result = mutableListOf<CategoryEntity>()
    this.forEach {
        result.add(CategoryEntity(0, it.id, it.title, it.hadeethsCount, it.parentID, language))
    }
    return result
}

package ir.namoo.hadeeth.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val code: String,
    val native: String
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val categoryID: String,
    val title: String,
    val hadeethsCount: String,
    val parentID: String?,
    val language: String
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val id: Int,
    val language: String
)

package ir.namoo.quran.settings.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translate_setting")
data class TranslateSetting(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "is_active") var isActive: Boolean,
    @ColumnInfo(name = "priority") var priority: Int
)

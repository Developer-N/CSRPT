package ir.namoo.religiousprayers.ui.downloadtimes

data class CityItemState(
    val id: Int = 0,
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastUpdate: String = "",
    val isDownloading: Boolean = false,
    val isDownloaded: Boolean = false,
    val isSelected: Boolean = false
)

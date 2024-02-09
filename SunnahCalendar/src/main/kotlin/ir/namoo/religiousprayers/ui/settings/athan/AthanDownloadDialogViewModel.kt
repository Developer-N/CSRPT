package ir.namoo.religiousprayers.ui.settings.athan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.commons.model.Athan
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.ServerAthanModel
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.getAthansDirectoryPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AthanDownloadDialogViewModel(
    private val prayTimeRepository: PrayTimeRepository, private val athanDB: AthanDB
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _athansList = MutableStateFlow(emptyList<ServerAthanModel>())
    val athansList = _athansList.asStateFlow()

    private val _athansState = MutableStateFlow(emptyList<AthanState>())
    val athanState = _athansState.asStateFlow()

    fun loadData(type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _isLoading.value = true }
            _athansList.value =
                if (type == 1) prayTimeRepository.getAthans() else prayTimeRepository.getAlarms()
            val tmpStateList = mutableListOf<AthanState>()
            for (athan in athansList.value) {
                tmpStateList.add(AthanState())
                val athanInDB = athanDB.athanDAO().getAthan(athan.name)
                if (athanInDB == null) athanDB.athanDAO().insert(
                    Athan(athan.name, "online/${athan.fileTitle}", type, athan.fileTitle)
                )
                else {
                    athanInDB.name = athan.name
                    athanInDB.link = "online/${athan.fileTitle}"
                    athanInDB.type = type
                    athanInDB.fileName = athan.fileTitle
                    athanDB.athanDAO().update(athanInDB)
                }
            }
            _athansState.value = tmpStateList
            withContext(Dispatchers.Main) { _isLoading.value = false }
        }
    }

    fun checkAthansState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            athansList.value.zip(athanState.value).forEach { pair ->
                val athan = pair.first
                val state = pair.second
                withContext(Dispatchers.Default) { state.isLoading = true }
                val athanFile =
                    File(getAthansDirectoryPath(context) + File.separator + athan.fileTitle)
                val isExist = athanFile.exists()
                withContext(Dispatchers.Default) {
                    state.isLoading = false
                    state.isDownloaded = isExist
                }
            }
        }
    }

    fun download(context: Context, pair: Pair<ServerAthanModel, AthanState>) {
        viewModelScope.launch {
            val file = File("${getAthansDirectoryPath(context)}/${pair.first.fileTitle}")
            val url = "https://namoodev.ir/api/v1/app/downloadAthan/${pair.first.id}"
            pair.second.isDownloading = true
            val ktor = HttpClient(Android)
            ktor.downloadFile(file, url).collect {
                when (it) {
                    is DownloadResult.Error -> {
                        pair.second.isDownloading = false
                    }

                    is DownloadResult.Progress -> {
                        pair.second.progress = it.progress / 100f
                    }

                    is DownloadResult.Success -> {
                        pair.second.isDownloading = false
                        pair.second.progress = 0f
                        val athan = pair.first
                        val state = pair.second
                        withContext(Dispatchers.Default) { state.isLoading = true }
                        val athanFile =
                            File(getAthansDirectoryPath(context) + File.separator + athan.fileTitle)
                        val isExist = athanFile.exists()
                        withContext(Dispatchers.Default) {
                            state.isLoading = false
                            state.isDownloaded = isExist
                        }
                    }

                    is DownloadResult.TotalSize -> {
                        pair.second.totalSize = it.totalSize
                    }
                }
            }
        }
    }
}

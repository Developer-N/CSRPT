package ir.namoo.religiousprayers.ui.settings.athan

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import ir.namoo.commons.downloader.DownloadResult
import ir.namoo.commons.downloader.downloadFile
import ir.namoo.commons.model.Athan
import ir.namoo.commons.model.AthanDB
import ir.namoo.commons.model.ServerAthanModel
import ir.namoo.commons.repository.DataState
import ir.namoo.commons.repository.PrayTimeRepository
import ir.namoo.commons.utils.getAthansDirectoryPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class AthanDownloadDialogViewModel(
    private val prayTimeRepository: PrayTimeRepository, private val athanDB: AthanDB
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _athansState = mutableStateListOf<AthanState>()
    val athanState = _athansState

    fun loadData(type: Int, context: Context) {
        viewModelScope.launch {
            prayTimeRepository.getAthansOrAlarms(type).collectLatest { state ->
                when (state) {
                    is DataState.Error -> {
                        _isLoading.value = false
                    }

                    DataState.Loading -> {
                        _isLoading.value = true
                        _athansState.clear()
                    }

                    is DataState.Success -> {
                        val athans = state.data as List<ServerAthanModel>
                        _athansState.addAll(athans.map { athan ->
                            AthanState(
                                id = athan.id,
                                name = athan.name,
                                fileTitle = athan.fileTitle,
                                githubLink = athan.githubLink
                            )
                        })
                        checkAthansState(context)
                        for (athan in athans) {
                            val athanInDB = athanDB.athanDAO().getAthan(athan.name)
                            if (athanInDB == null) athanDB.athanDAO().insert(
                                Athan(
                                    name = athan.name,
                                    link = "online/${athan.fileTitle}",
                                    type = type,
                                    fileName = athan.fileTitle
                                )
                            )
                            else {
                                athanDB.athanDAO().update(
                                    athanInDB.copy(
                                        name = athan.name,
                                        link = "online/${athan.fileTitle}",
                                        type = type,
                                        fileName = athan.fileTitle
                                    )
                                )
                            }
                        }
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    private fun checkAthansState(context: Context) {
        _athansState.forEachIndexed { index, athan ->
            _athansState[index] = _athansState[index].copy(isLoading = true)
            val athanFile =
                File(getAthansDirectoryPath(context) + File.separator + athan.fileTitle)
            val isExist = athanFile.exists()
            _athansState[index] =
                _athansState[index].copy(isLoading = false, isDownloaded = isExist)
        }
    }

    fun download(context: Context, athan: AthanState) {
        viewModelScope.launch {
            val file = File("${getAthansDirectoryPath(context)}/${athan.fileTitle}")
            val url = athan.githubLink
            val index = _athansState.indexOf(athan)
            _athansState[index] = _athansState[index].copy(isDownloading = true)
            val ktor = HttpClient(Android)
            ktor.downloadFile(file, url).collect {
                when (it) {
                    is DownloadResult.Error -> {
                        _athansState[index] = _athansState[index].copy(isDownloading = false)
                    }

                    is DownloadResult.Progress -> {
                        _athansState[index] =
                            _athansState[index].copy(progress = it.progress / 100f)
                    }

                    is DownloadResult.Success -> {
                        _athansState[index] = _athansState[index].copy(
                            isLoading = true,
                            isDownloading = false,
                            progress = 0f
                        )
                        val athanFile =
                            File(getAthansDirectoryPath(context) + File.separator + athan.fileTitle)
                        val isExist = athanFile.exists()
                        _athansState[index] =
                            _athansState[index].copy(isLoading = false, isDownloaded = isExist)
                    }

                    is DownloadResult.TotalSize -> {
                        _athansState[index] = _athansState[index].copy(totalSize = it.totalSize)
                    }

                    is DownloadResult.DownloadedByte -> {}
                }
            }
        }
    }
}

package ir.namoo.hadeeth.repository

import android.util.Log
import ir.namoo.commons.repository.DataResult
import ir.namoo.commons.repository.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class HadeethRepository(
    private val onlineRepository: HadeethOnlineRepository,
    private val localRepository: HadeethLocalRepository
) {
    fun getLanguages() = flow {
        runCatching {
            emit(DataState.Loading)
            val localData = localRepository.getAllLanguages()
            if (localData.isEmpty()) {
                when (val result = onlineRepository.getLanguages()) {
                    is DataResult.Success -> {
                        result.data as List<Language>
                        localRepository.deleteAllLanguages()
                        localRepository.insertLanguages(result.data.toLanguageEntities())
                        emit(DataState.Success(localRepository.getAllLanguages()))
                    }

                    is DataResult.Error -> emit(DataState.Error(result.message))
                }
            } else emit(DataState.Success(localData))

        }.onFailure {
            emit(DataState.Error(it.message ?: "Unknown error!"))
        }
    }.flowOn(Dispatchers.IO)

    fun getCategories(language: String) = flow {
        runCatching {
            emit(DataState.Loading)
            val localData = localRepository.getCategories(language)
            if (localData.isEmpty()) {
                when (val result = onlineRepository.getCategories(language)) {
                    is DataResult.Success -> {
                        result.data as List<Category>
                        localRepository.deleteAllCategories(language)
                        localRepository.insertCategories(result.data.toCategoryEntities(language))
                        emit(DataState.Success(localRepository.getCategories(language)))
                    }

                    is DataResult.Error -> emit(DataState.Error(result.message))
                }
            } else emit(DataState.Success(localData))
        }.onFailure {
            emit(DataState.Error(it.message ?: "Unknown error!"))
        }
    }.flowOn(Dispatchers.IO)

    fun getHadeethList(language: String, categoryId: Int, page: Int, perPage: Int) = flow {
        runCatching {
            emit(DataState.Loading)
            when (val result =
                onlineRepository.getHadeethList(language, categoryId, page, perPage)) {
                is DataResult.Success -> {
                    result.data as HadeethList
                    emit(DataState.Success(result.data))
                }

                is DataResult.Error -> emit(DataState.Error(result.message))
            }
        }.onFailure {
            emit(DataState.Error(it.message ?: "Unknown error!"))
        }
    }.flowOn(Dispatchers.IO)

    fun getHadeeth(language: String, hadeethID: Int) = flow {
        runCatching {
            emit(DataState.Loading)
            when (val result = onlineRepository.getHadeeth(language, hadeethID)) {
                is DataResult.Success -> {
                    result.data as Hadeeth
                    emit(DataState.Success(result.data))
                }

                is DataResult.Error -> emit(DataState.Error(result.message))
            }
        }.onFailure {
            emit(DataState.Error(it.message ?: "Unknown error!"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun updateLocalLanguage(language: String) {
        runCatching {
            val settings = localRepository.getSettings()
            localRepository.updateSettings(settings.copy(language = language))
        }.onFailure {
            Log.e("HadeethRepository", "updateSettings: ${it.message}")
        }
    }

    suspend fun getSettings() = localRepository.getSettings()

    suspend fun clearCachedData(){
        localRepository.deleteAllCategories()
        localRepository.deleteAllLanguages()
    }
}

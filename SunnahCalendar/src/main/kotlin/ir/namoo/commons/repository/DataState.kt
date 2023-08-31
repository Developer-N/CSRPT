package ir.namoo.commons.repository

sealed class DataState<out T> {
    data object Loading : DataState<Nothing>()
    class Success<T>(val data: T) : DataState<T>()
    class Error(val message: String = "Unknown Error!") : DataState<Nothing>()
}

package ir.namoo.commons.repository


sealed class DataResult<out T> {
    open class Success<T>(val data: T) : DataResult<T>()
    open class Error(val message: String = "Unknown Error!") : DataResult<Nothing>()
}

fun <T> DataResult<T>.asDataState(): DataState<T> {
    return when (this) {
        is DataResult.Success -> DataState.Success(data)
        is DataResult.Error -> DataState.Error(message)
    }
}

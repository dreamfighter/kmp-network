package id.dreamfighter.multiplatform.api.model

data class RequestUiState<T>(
    var data: T? = null,
    var error: String = "",
    var loading: Boolean = false
)
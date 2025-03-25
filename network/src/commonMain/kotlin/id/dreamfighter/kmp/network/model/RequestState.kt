package id.dreamfighter.kmp.network.model

data class RequestUiState<T>(
    var data: T? = null,
    var error: String = "",
    var loading: Boolean = false
)
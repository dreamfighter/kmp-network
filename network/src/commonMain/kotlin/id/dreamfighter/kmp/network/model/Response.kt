package id.dreamfighter.kmp.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val expiresIin: Int,
    val scope: String,
    val tokenType: String
)
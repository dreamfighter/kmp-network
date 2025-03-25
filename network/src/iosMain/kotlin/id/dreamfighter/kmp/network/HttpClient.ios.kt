package id.dreamfighter.kmp.network

import id.dreamfighter.kmp.network.model.Request
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual val client: HttpClient = HttpClient(Darwin) {
    install(HttpTimeout) {
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 60_000
    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    defaultRequest {
        header("Content-Type", "application/json")
        url("https://pokeapi.co/api/v2/")
    }
    install(ContentNegotiation){
        json(Json{
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
}


actual inline fun <reified T : Any> getRequest(obj: T): Request {
   return obj as Request
}
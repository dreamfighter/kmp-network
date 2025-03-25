package id.dreamfighter.kmp.network

import id.dreamfighter.kmp.network.model.Request
import io.ktor.client.HttpClient
import kotlin.reflect.KClass

actual val client: HttpClient
    get() = TODO("Not yet implemented")

actual inline fun <reified T : Any> getRequest(obj: T): Request {
    TODO("Not yet implemented")
}
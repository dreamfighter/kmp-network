package id.dreamfighter.kmp.network

import id.dreamfighter.kmp.annotation.Get
import id.dreamfighter.kmp.annotation.Path
import id.dreamfighter.kmp.annotation.Post
import id.dreamfighter.kmp.annotation.Query
import id.dreamfighter.kmp.network.model.Request
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
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
import kotlin.reflect.full.memberProperties

actual val client: HttpClient = HttpClient(OkHttp) {
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
        url(BASE_URL)
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

    val methods = obj::class.annotations.filter {
        it is Get || it is Post
    }
    if(methods.isNotEmpty()){
        var method = ""
        var url = ""
        val path: MutableMap<String, Any?> = mutableMapOf()
        val query:MutableMap<String,Any?> = mutableMapOf()
        when(val annotation = methods.first()){
            is Get -> {
                method = HttpMethod.GET
                url = annotation.url
            }
            is Post -> {
                method = HttpMethod.POST
                url = annotation.url
            }
        }

        obj::class.memberProperties.forEach { prop ->
            prop.annotations.forEach {
                when(it){
                    is Path -> {
                        val name = if(it.name == ""){
                            prop.name
                        }else{
                            it.name
                        }
                        path[name] = prop.getter.call(obj)
                    }
                    is Query -> {
                        val name = if(it.name == ""){
                            prop.name
                        }else{
                            it.name
                        }
                        query[name] = prop.getter.call(obj)
                    }
                }
            }
        }
        return Request(url = url, method = method, path = path, query = query)
    }else{
        throw Exception("Method not found")
    }
}
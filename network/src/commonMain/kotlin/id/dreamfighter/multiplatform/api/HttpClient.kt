package id.dreamfighter.multiplatform.api

import id.dreamfighter.multiplatform.annotation.Body
import id.dreamfighter.multiplatform.annotation.Get
import id.dreamfighter.multiplatform.annotation.Header
import id.dreamfighter.multiplatform.annotation.Multipart
import id.dreamfighter.multiplatform.annotation.Path
import id.dreamfighter.multiplatform.annotation.Post
import id.dreamfighter.multiplatform.annotation.Query
import id.dreamfighter.multiplatform.api.model.Request
import id.dreamfighter.multiplatform.api.model.Resource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import io.ktor.http.parameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object HttpMethod {
    val GET = "GET"
    val POST = "POST"
}
var BASE_URL = "https://localhost"
expect val client: HttpClient

fun Any.toJson(): String {
    return """{"name":"${this::class.simpleName}"}"""
}

expect inline fun <reified T:Any> getRequest(obj: T):Request

suspend inline fun <reified T> flowReq(request: Request, crossinline interceptor: HttpRequestBuilder.() -> Unit = {}): Flow<Resource<T>> {
    return flow {
        emit(Resource.Loading(true))
        emit(req(request, interceptor))
    }
}

suspend inline fun <reified T> req(request: Request, interceptor: HttpRequestBuilder.() -> Unit = {}): Resource<T>{
    println(request.toJson())

    if(request.path.isNotEmpty()){
        request.path.forEach {
            request.url = request.url.replace("{${it.key}}",it.value.toString().encodeURLPathPart())
        }
    }
    try {
        val params = parameters {
            request.query.forEach {
                append(it.key, it.value.toString())
            }
        }
        val contentType = request.requestHeaders["Content-Type"]
        val response = when(request.method) {
            HttpMethod.POST -> {
                println("request.body ${request.body}")
                when(contentType){
                    "application/json" -> client.post(request.url) {
                        interceptor(this)
                        contentType(ContentType.Application.Json)

                        if(request.body!=null) {
                            setBody(request.body)
                        }
                    }
                    "mutlipart/form-data" -> client.submitFormWithBinaryData(url = request.url, formData = request.formData){
                        interceptor(this)
                    }
                    else -> {
                        val formParams = parameters {
                            if(request.body != null && request.body is Map<*, *>){
                                request.body.forEach {
                                    append("${it.key}", it.value.toString())
                                }
                            }
                        }

                        client.submitForm(
                            url = request.url,
                            formParameters = formParams
                        )
                    }
                }
            }
            else -> client.get(request.url){
                request.requestHeaders.filter { it.key != "Content-Type" }.map { h ->
                    headers {
                        append(h.key, "${h.value}")
                    }
                }
                interceptor(this)
                url {
                    parameters.appendAll(params)
                }
            }
        }

        return when(response.status){
            HttpStatusCode.OK -> Resource.Success(response.body())
            else -> Resource.Error(response.body())
        }
    } catch (e: RedirectResponseException) {
        // handle 3xx codes
        return(Resource.Error(e.response.status.description))

    } catch (e: ClientRequestException) {
        //handle 4xx error codes
        return(Resource.Error(e.response.status.description))

    } catch (e: ServerResponseException) {
        //handle 5xx error codes
        return(Resource.Error(e.response.status.description))
    } catch (e: Exception) {
        return (Resource.Error(e.message ?: "Something went wrong"))
    }
}
/*
@Multipart
@Post(url = "/auth/google")
data class AuthGoogle(@Body val map: Map<String,String>,
                      @Path val id:Int,
                      @Query val name:String)

@Get("/auth/google")
data class Profile(@Path val id:Int, @Query val name:String)
*/
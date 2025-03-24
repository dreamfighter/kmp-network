package id.dreamfighter.multiplatform.api.model

import io.ktor.http.content.PartData

data class Request(
    var url:String,
    val method:String,
    val path:Map<String,Any?> = mapOf(),
    val body:Any? = null,
    val requestHeaders:Map<String,Any?> = mapOf(),
    val query:Map<String,Any?> = mapOf(),
    var formData:List<PartData> = io.ktor.client.request.forms.formData { }
)

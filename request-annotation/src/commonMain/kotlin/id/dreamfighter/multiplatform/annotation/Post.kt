package id.dreamfighter.multiplatform.annotation

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Post(val url: String, val contentType: String = "application/json")
package id.dreamfighter.kmp.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Post(val url: String, val contentType: String = "application/json")
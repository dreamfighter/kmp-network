package id.dreamfighter.multiplatform.annotation

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class Get(val url: String)
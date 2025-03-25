package id.dreamfighter.multiplatform.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Get(val url: String)
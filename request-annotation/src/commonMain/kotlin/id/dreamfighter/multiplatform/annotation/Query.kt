package id.dreamfighter.multiplatform.annotation

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Query(val name: String = "")
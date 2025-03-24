package id.dreamfighter.multiplatform.annotation

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Path(val name: String = "")
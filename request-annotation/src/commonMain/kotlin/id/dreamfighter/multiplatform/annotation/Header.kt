package id.dreamfighter.multiplatform.annotation

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Header(val name: String = "")
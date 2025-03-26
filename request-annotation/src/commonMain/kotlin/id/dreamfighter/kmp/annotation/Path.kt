package id.dreamfighter.kmp.annotation

@Target(AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER
)
@MustBeDocumented
annotation class Path(val name: String = "")
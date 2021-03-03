package world.gregs.voidps.cache.format.definition

//@SerialInfo
//@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class Operation(val code: Int)

//@SerialInfo
//@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class Setter(val value: Long)

//@SerialInfo
//@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class Indexed(val operations: IntArray)
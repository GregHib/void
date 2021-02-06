package world.gregs.voidps.cache.format.definition

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier

@SerialInfo
@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class Operation(val code: Int)

@SerialInfo
@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class Setter(val value: Long)

@SerialInfo
@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class Indexed(val operations: IntArray)
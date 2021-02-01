package world.gregs.voidps.cache.format.definition

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

/**
 * Temporary solution until inline classes and kotlin's experimental unsigned types are added.
 * Would ideally have default values for annotations too.
 */
@SerialInfo
@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class MetaData(
    val type: DataType,
    val signed: Boolean,
    val modifier: Modifier,
    val endian: Endian
)
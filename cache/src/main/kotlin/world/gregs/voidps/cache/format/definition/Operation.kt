package world.gregs.voidps.cache.format.definition

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@SerialInfo
@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
annotation class Operation(val code: Int)

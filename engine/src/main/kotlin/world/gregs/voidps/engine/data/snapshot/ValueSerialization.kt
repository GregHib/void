package world.gregs.voidps.engine.data.snapshot

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.cache.serialization.AnyValueDecoder
import world.gregs.voidps.cache.serialization.AnyValueEncoder

/** Writes a value of any supported type, tagged with its type */
fun Encoder.encodeValue(value: Any?) = (this as AnyValueEncoder).encodeAnyValue(value)

fun Decoder.decodeValue(): Any? = (this as AnyValueDecoder).decodeAnyValue()

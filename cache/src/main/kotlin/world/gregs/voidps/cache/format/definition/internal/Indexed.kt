package world.gregs.voidps.cache.format.definition.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.internal.TaggedEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@InternalSerializationApi
abstract class IndexedDecoder : TaggedDecoder<Int>() {
    override fun SerialDescriptor.getTag(index: Int): Int = index
}

@InternalSerializationApi
abstract class IndexedEncoder : TaggedEncoder<Int>() {
    override fun SerialDescriptor.getTag(index: Int): Int = index
}
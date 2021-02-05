package world.gregs.voidps.cache.format.definition

import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import world.gregs.voidps.cache.format.definition.internal.DefinitionDecoder
import world.gregs.voidps.cache.format.definition.internal.DefinitionEncoder
import world.gregs.voidps.cache.format.definition.internal.MediumSerializer

@ExperimentalSerializationApi
sealed class Definition(
    internal val encodeDefaults: Boolean = false,
    override val serializersModule: SerializersModule = EmptySerializersModule
) : BinaryFormat {

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val m = DefinitionEncoder(encodeDefaults, serializer.descriptor)
        m.encodeSerializableValue(serializer, value)
        return m.toByteArray()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val m = DefinitionDecoder(bytes, deserializer.descriptor)
        return m.decodeSerializableValue(deserializer)
    }

    @ExperimentalSerializationApi
    companion object Default : Definition()
}

@ExperimentalSerializationApi
inline fun <reified T> Definition.encodeToByteArray(value: T): ByteArray =
    encodeToByteArray(serializersModule.serializer(), value)

@ExperimentalSerializationApi
inline fun <reified T> Definition.decodeFromByteArray(array: ByteArray): T =
    decodeFromByteArray(serializersModule.serializer(), array)
package world.gregs.voidps.cache.serialization

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition

/**
 * The components of an interface by their index.
 */
object InterfaceComponentsSerializer : KSerializer<MutableMap<Int, InterfaceComponentDefinition>> {
    private val component = InterfaceComponentDefinition.serializer()
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("kotlin.collections.Map<kotlin.Int, ${component.descriptor.serialName}>") {
        element("component", component.descriptor)
    }

    override fun serialize(encoder: Encoder, value: MutableMap<Int, InterfaceComponentDefinition>) {
        encoder.encodeInt(value.size)
        for ((index, definition) in value) {
            encoder.encodeInt(index)
            component.serialize(encoder, definition)
        }
    }

    override fun deserialize(decoder: Decoder): MutableMap<Int, InterfaceComponentDefinition> {
        val size = decoder.decodeInt()
        val map = Int2ObjectOpenHashMap<InterfaceComponentDefinition>(size)
        repeat(size) {
            val index = decoder.decodeInt()
            map[index] = component.deserialize(decoder)
        }
        return map
    }
}

package world.gregs.voidps.engine.data.snapshot.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

/**
 * Definitions by id, read straight into an array of the right size without the intermediate list the generic array serializer uses.
 */
@OptIn(ExperimentalSerializationApi::class)
class DefinitionArraySerializer<D : Any>(
    private val type: KClass<D>,
    private val element: KSerializer<D>,
) : KSerializer<Array<D>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("kotlin.Array", element.descriptor) {
        element("element", element.descriptor)
    }

    override fun serialize(encoder: Encoder, value: Array<D>) {
        val composite = encoder.beginCollection(descriptor, value.size)
        for (i in value.indices) {
            composite.encodeSerializableElement(element.descriptor, i, element, value[i])
        }
        composite.endStructure(descriptor)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): Array<D> {
        val composite = decoder.beginStructure(descriptor)
        val array = java.lang.reflect.Array.newInstance(type.java, composite.decodeCollectionSize(descriptor)) as Array<D>
        for (i in array.indices) {
            array[i] = composite.decodeSerializableElement(element.descriptor, i, element)
        }
        composite.endStructure(descriptor)
        return array
    }
}

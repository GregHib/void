package world.gregs.voidps.engine.data.snapshot.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.engine.data.load.DefinitionsState
import kotlin.reflect.KClass

/**
 * [DefinitionsState] of definitions serialized with [element].
 */
@OptIn(ExperimentalSerializationApi::class)
class DefinitionsStateSerializer<D : Any>(
    type: KClass<D>,
    element: KSerializer<D>,
    default: Int?,
) : KSerializer<DefinitionsState<D>> {
    private val definitions = DefinitionArraySerializer(type, element)
    private val ids = StringIdsSerializer(default)

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.load.DefinitionsState", element.descriptor) {
        element("definitions", definitions.descriptor)
        element("ids", ids.descriptor)
    }

    override fun serialize(encoder: Encoder, value: DefinitionsState<D>) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeSerializableElement(descriptor, 0, definitions, value.definitions)
        composite.encodeSerializableElement(descriptor, 1, ids, value.ids)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): DefinitionsState<D> {
        val composite = decoder.beginStructure(descriptor)
        val array = composite.decodeSerializableElement(descriptor, 0, definitions)
        val map = composite.decodeSerializableElement(descriptor, 1, ids)
        composite.endStructure(descriptor)
        return DefinitionsState(array, map)
    }
}

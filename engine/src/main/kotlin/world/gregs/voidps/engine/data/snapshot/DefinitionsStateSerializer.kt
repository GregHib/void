package world.gregs.voidps.engine.data.snapshot

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.engine.data.load.DefinitionsState
import kotlin.reflect.KClass

/**
 * String ids by number, restored as the [Object2IntOpenHashMap] the config parsers build.
 * @param default return value for unknown ids.
 */
class StringIdsSerializer(private val default: Int?) : KSerializer<Map<String, Int>> {
    override val descriptor: SerialDescriptor = MapSerializer(String.serializer(), Int.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: Map<String, Int>) {
        val composite = encoder.beginCollection(descriptor, value.size)
        var index = 0
        for ((stringId, id) in value) {
            composite.encodeStringElement(descriptor, index++, stringId)
            composite.encodeIntElement(descriptor, index++, id)
        }
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Map<String, Int> {
        val composite = decoder.beginStructure(descriptor)
        val size = composite.decodeCollectionSize(descriptor)
        val ids = Object2IntOpenHashMap<String>(size, Hash.VERY_FAST_LOAD_FACTOR)
        if (default != null) {
            ids.defaultReturnValue(default)
        }
        var index = 0
        repeat(size) {
            val stringId = composite.decodeStringElement(descriptor, index++)
            ids[stringId] = composite.decodeIntElement(descriptor, index++)
        }
        composite.endStructure(descriptor)
        return ids
    }
}

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

package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.cache.serialization.ParamsSerializer
import world.gregs.voidps.engine.data.snapshot.serializer.AreaSerializer
import java.util.IdentityHashMap

internal object AreasStateSerializer : KSerializer<AreasState> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.definition.AreasState") {
        element("name", String.serializer().descriptor)
        element("area", AreaSerializer.descriptor)
        element("tags", String.serializer().descriptor)
        element("stringId", String.serializer().descriptor)
        element("params", ParamsSerializer.descriptor)
        element("tagged", IntArraySerializer().descriptor)
        element("areas", IntArraySerializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: AreasState) {
        val definitions = value.named.values.toList()
        val indices = IdentityHashMap<AreaDefinition, Int>(definitions.size)
        encoder.encodeInt(definitions.size)
        for ((index, definition) in definitions.withIndex()) {
            indices[definition] = index
            encoder.encodeString(definition.name)
            AreaSerializer.serialize(encoder, definition.area)
            encoder.encodeInt(definition.tags.size)
            for (tag in definition.tags) {
                encoder.encodeString(tag)
            }
            encoder.encodeString(definition.stringId)
            ParamsSerializer.serialize(encoder, definition.params)
        }
        fun indices(encoder: Encoder, set: Set<AreaDefinition>) {
            encoder.encodeInt(set.size)
            for (definition in set) {
                encoder.encodeInt(indices[definition] ?: throw IllegalArgumentException("Area '${definition.name}' is not named."))
            }
        }
        encoder.encodeInt(value.tagged.size)
        for ((tag, set) in value.tagged) {
            encoder.encodeString(tag)
            indices(encoder, set)
        }
        encoder.encodeInt(value.areas.size)
        for ((zone, set) in value.areas) {
            encoder.encodeInt(zone)
            indices(encoder, set)
        }
    }

    override fun deserialize(decoder: Decoder): AreasState {
        val definitions = ArrayList<AreaDefinition>()
        val named = Object2ObjectOpenHashMap<String, AreaDefinition>()
        repeat(decoder.decodeInt()) {
            val name = decoder.decodeString()
            val area = AreaSerializer.deserialize(decoder)
            val tags = ObjectOpenHashSet<String>()
            repeat(decoder.decodeInt()) { tags.add(decoder.decodeString()) }
            val stringId = decoder.decodeString()
            val params = ParamsSerializer.deserialize(decoder)
            val definition = if (params == null) {
                AreaDefinition(name = name, area = area, tags = tags, stringId = stringId)
            } else {
                AreaDefinition(name = name, area = area, tags = tags, stringId = stringId, params = params)
            }
            definitions.add(definition)
            named[name] = definition
        }
        fun set(decoder: Decoder): ObjectOpenHashSet<AreaDefinition> {
            val size = decoder.decodeInt()
            val set = ObjectOpenHashSet<AreaDefinition>(size)
            repeat(size) { set.add(definitions[decoder.decodeInt()]) }
            return set
        }
        val tagged = Object2ObjectOpenHashMap<String, MutableSet<AreaDefinition>>()
        repeat(decoder.decodeInt()) {
            val tag = decoder.decodeString()
            tagged[tag] = set(decoder)
        }
        val areas = Int2ObjectOpenHashMap<MutableSet<AreaDefinition>>()
        repeat(decoder.decodeInt()) {
            val zone = decoder.decodeInt()
            areas[zone] = set(decoder)
        }
        return AreasState(named, tagged, areas)
    }
}

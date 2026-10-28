package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.snapshot.serializer.IntRangeSerializer
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import java.util.IdentityHashMap

internal object ItemOnItemStateSerializer : KSerializer<Map<String, List<ItemOnItemDefinition>>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("world.gregs.voidps.engine.data.definition.ItemOnItemState") {
        element("skill", String.serializer().nullable.descriptor)
        element("level", Int.serializer().descriptor)
        element("xp", Double.serializer().descriptor)
        element("items", String.serializer().descriptor)
        element("amount", Int.serializer().descriptor)
        element("delay", Int.serializer().descriptor)
        element("ticks", Int.serializer().descriptor)
        element("chance", IntRangeSerializer.descriptor)
        element("type", String.serializer().descriptor)
        element("animation", String.serializer().descriptor)
        element("graphic", String.serializer().descriptor)
        element("sound", String.serializer().descriptor)
        element("message", String.serializer().descriptor)
        element("failure", String.serializer().descriptor)
        element("requiresMessage", String.serializer().descriptor)
        element("question", String.serializer().descriptor)
        element("maximum", Int.serializer().descriptor)
        element("members", Boolean.serializer().descriptor)
        element("keys", String.serializer().descriptor)
    }

    override fun serialize(encoder: Encoder, value: Map<String, List<ItemOnItemDefinition>>) {
        val indices = IdentityHashMap<ItemOnItemDefinition, Int>()
        for (list in value.values) {
            for (definition in list) {
                indices.putIfAbsent(definition, indices.size)
            }
        }
        val unique = arrayOfNulls<ItemOnItemDefinition>(indices.size)
        for ((definition, index) in indices) {
            unique[index] = definition
        }
        encoder.encodeInt(unique.size)
        for (definition in unique) {
            definition!!
            val skill = definition.skill?.name
            encoder.encodeBoolean(skill != null)
            if (skill != null) {
                encoder.encodeString(skill)
            }
            encoder.encodeInt(definition.level)
            encoder.encodeDouble(definition.xp)
            items(encoder, definition.requires)
            items(encoder, definition.one)
            items(encoder, definition.remove)
            items(encoder, definition.add)
            items(encoder, definition.fail)
            encoder.encodeInt(definition.delay)
            encoder.encodeInt(definition.ticks)
            IntRangeSerializer.serialize(encoder, definition.chance)
            encoder.encodeString(definition.type)
            encoder.encodeString(definition.animation)
            encoder.encodeString(definition.graphic)
            encoder.encodeString(definition.sound)
            encoder.encodeString(definition.message)
            encoder.encodeString(definition.failure)
            encoder.encodeString(definition.requiresMessage)
            encoder.encodeString(definition.question)
            encoder.encodeInt(definition.maximum)
            encoder.encodeBoolean(definition.members)
        }
        encoder.encodeInt(value.size)
        for ((key, list) in value) {
            encoder.encodeString(key)
            encoder.encodeInt(list.size)
            for (definition in list) {
                encoder.encodeInt(indices.getValue(definition))
            }
        }
    }

    override fun deserialize(decoder: Decoder): Map<String, List<ItemOnItemDefinition>> {
        val unique = Array(decoder.decodeInt()) {
            ItemOnItemDefinition(
                skill = if (decoder.decodeBoolean()) Skill.valueOf(decoder.decodeString()) else null,
                level = decoder.decodeInt(),
                xp = decoder.decodeDouble(),
                requires = items(decoder),
                one = items(decoder),
                remove = items(decoder),
                add = items(decoder),
                fail = items(decoder),
                delay = decoder.decodeInt(),
                ticks = decoder.decodeInt(),
                chance = IntRangeSerializer.deserialize(decoder),
                type = decoder.decodeString(),
                animation = decoder.decodeString(),
                graphic = decoder.decodeString(),
                sound = decoder.decodeString(),
                message = decoder.decodeString(),
                failure = decoder.decodeString(),
                requiresMessage = decoder.decodeString(),
                question = decoder.decodeString(),
                maximum = decoder.decodeInt(),
                members = decoder.decodeBoolean(),
            )
        }
        val size = decoder.decodeInt()
        val definitions = Object2ObjectOpenHashMap<String, MutableList<ItemOnItemDefinition>>(size, Hash.VERY_FAST_LOAD_FACTOR)
        repeat(size) {
            val key = decoder.decodeString()
            val list = ObjectArrayList<ItemOnItemDefinition>(2)
            repeat(decoder.decodeInt()) { list.add(unique[decoder.decodeInt()]) }
            definitions[key] = list
        }
        return definitions
    }

    private fun items(encoder: Encoder, items: List<Item>) {
        encoder.encodeInt(items.size)
        for (item in items) {
            encoder.encodeString(item.id)
            encoder.encodeInt(item.rawValue())
        }
    }

    private fun items(decoder: Decoder): List<Item> {
        val size = decoder.decodeInt()
        val list = ArrayList<Item>(size)
        repeat(size) { list.add(Item(decoder.decodeString(), decoder.decodeInt())) }
        return list
    }
}

package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
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
import net.pearx.kasechange.toSentenceCase
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.data.snapshot.IntRangeSerializer
import world.gregs.voidps.engine.data.snapshot.SnapshotFormat
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import java.util.IdentityHashMap

class ItemOnItemDefinitions {

    private lateinit var definitions: Map<String, List<ItemOnItemDefinition>>

    fun get(one: Item, two: Item) = getOrNull(one, two) ?: emptyList()

    fun getOrNull(one: Item, two: Item) = definitions[id(one, two)] ?: definitions[id(two, one)]

    fun contains(one: Item, two: Item) = definitions.containsKey(id(one, two)) || definitions.containsKey(id(two, one))

    fun install(state: Map<String, List<ItemOnItemDefinition>>) {
        definitions = state
    }

    fun load(paths: List<String>): ItemOnItemDefinitions {
        install(parse(paths))
        return this
    }

    internal fun parse(paths: List<String>): Map<String, List<ItemOnItemDefinition>> {
        val definitions = Object2ObjectOpenHashMap<String, MutableList<ItemOnItemDefinition>>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    section() // ignored
                    var skill: Skill? = null
                    var level = 1
                    var xp = 0.0
                    val requires = ObjectArrayList<Item>()
                    val oneOf = ObjectArrayList<Item>()
                    val remove = ObjectArrayList<Item>()
                    val add = ObjectArrayList<Item>()
                    val fail = ObjectArrayList<Item>()
                    var delay = 1
                    var ticks = 0
                    var chance: IntRange = Level.SUCCESS
                    var type = "make"
                    var animation = ""
                    var graphic = ""
                    var sound = ""
                    var message = ""
                    var failure = ""
                    var requiresMessage = ""
                    var question: String? = null
                    var maximum: Int = -1
                    var members: Boolean = false

                    while (nextPair()) {
                        val key = key()
                        when (key) {
                            "skill" -> skill = Skill.valueOf(string().toSentenceCase())
                            "level" -> level = int()
                            "xp" -> xp = double()
                            "requires" -> itemList(requires)
                            "one" -> itemList(oneOf)
                            "remove" -> itemList(remove)
                            "add" -> itemList(add)
                            "fail" -> itemList(fail)
                            "delay" -> delay = int()
                            "ticks" -> ticks = int()
                            "chance" -> chance = string().toIntRange()
                            "type" -> type = string()
                            "animation" -> animation = string()
                            "graphic" -> graphic = string()
                            "sound" -> sound = string()
                            "message" -> message = string()
                            "failure" -> failure = string()
                            "requires_message" -> requiresMessage = string()
                            "question" -> question = string()
                            "maximum" -> maximum = int()
                            "members" -> members = boolean()
                            else -> throw IllegalArgumentException("Unexpected item on item key: '$key' ${exception()}")
                        }
                    }

                    val definition = ItemOnItemDefinition(
                        skill = skill,
                        level = level,
                        xp = xp,
                        requires = requires,
                        one = oneOf,
                        remove = remove,
                        add = add,
                        fail = fail,
                        delay = delay,
                        ticks = ticks,
                        chance = chance,
                        type = type,
                        animation = animation,
                        graphic = graphic,
                        sound = sound,
                        message = message,
                        failure = failure,
                        requiresMessage = requiresMessage,
                        question = question ?: "How many would you like to $type?",
                        maximum = maximum,
                        members = members,
                    )
                    requires.addAll(definition.one)
                    requires.addAll(definition.remove)
                    for (a in requires.indices) {
                        for (b in requires.indices) {
                            if (a != b) {
                                val one = requires[a]
                                val two = requires[b]
                                val list = definitions.getOrPut(id(one, two)) { ObjectArrayList(2) }
                                if (!list.contains(definition)) {
                                    list.add(definition)
                                }
                            }
                        }
                    }
                }
            }
        }
        return definitions
    }

    private fun ConfigReader.itemList(items: MutableList<Item>) {
        while (nextElement()) {
            if (peek == '{') {
                var id = ""
                var amount = 1
                while (nextEntry()) {
                    when (key()) {
                        "id" -> id = string()
                        "amount", "charges" -> amount = int()
                    }
                }
                if (!ItemDefinitions.contains(id)) {
                    logger.warn { "Invalid item-on-item id: $id" }
                }
                items.add(Item(id, amount))
            } else {
                val id = string()
                if (!ItemDefinitions.contains(id)) {
                    logger.warn { "Invalid item-on-item id: $id" }
                }
                items.add(Item(id))
            }
        }
    }

    companion object {
        private val logger = InlineLogger()
        private fun id(one: Item, two: Item): String = "${one.id}&${two.id}"
    }
}

object ItemOnItemLoader : Loader<Map<String, List<ItemOnItemDefinition>>> {
    val instance = ItemOnItemDefinitions()
    override val name = "item_on_item"
    override val files = listOf("definitions.itemOnItem")
    override val dependsOn: List<Loader<*>> get() = listOf(ItemLoader)
    override val codec = SnapshotFormat.codec(ItemOnItemStateSerializer)

    override fun load(context: LoadContext): Map<String, List<ItemOnItemDefinition>> = instance.parse(context.paths(this))

    override fun install(state: Map<String, List<ItemOnItemDefinition>>) = instance.install(state)
}

private object ItemOnItemStateSerializer : KSerializer<Map<String, List<ItemOnItemDefinition>>> {
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

package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.pearx.kasechange.toSentenceCase
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.ItemOnItemDefinition
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad

class ItemOnItemDefinitions {

    private lateinit var definitions: Map<String, List<ItemOnItemDefinition>>

    fun get(one: Item, two: Item) = getOrNull(one, two) ?: emptyList()

    fun getOrNull(one: Item, two: Item) = definitions[id(one, two)] ?: definitions[id(two, one)]

    fun contains(one: Item, two: Item) = definitions.containsKey(id(one, two)) || definitions.containsKey(id(two, one))

    fun load(path: String = Settings["definitions.itemOnItem"], itemDefinitions: ItemDefinitions = get()): ItemOnItemDefinitions {
        timedLoad("item on item definition") {
            val definitions = Object2ObjectOpenHashMap<String, MutableList<ItemOnItemDefinition>>()
            var count = 0
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
                    var question: String? = null
                    var maximum: Int = -1

                    while (nextPair()) {
                        val key = key()
                        when (key) {
                            "skill" -> skill = Skill.valueOf(string().toSentenceCase())
                            "level" -> level = int()
                            "xp" -> xp = double()
                            "requires" -> itemList(requires, itemDefinitions)
                            "one" -> itemList(oneOf, itemDefinitions)
                            "remove" -> itemList(remove, itemDefinitions)
                            "add" -> itemList(add, itemDefinitions)
                            "fail" -> itemList(fail, itemDefinitions)
                            "delay" -> delay = int()
                            "ticks" -> ticks = int()
                            "chance" -> chance = string().toIntRange()
                            "type" -> type = string()
                            "animation" -> animation = string()
                            "graphic" -> graphic = string()
                            "sound" -> sound = string()
                            "message" -> message = string()
                            "failure" -> failure = string()
                            "question" -> question = string()
                            "maximum" -> maximum = int()
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
                        question = question ?: "How many would you like to $type?",
                        maximum = maximum
                    )
                    val usable = definition.requires.toMutableList()
                    usable.addAll(definition.one)
                    usable.addAll(definition.remove)
                    for (a in usable.indices) {
                        for (b in usable.indices) {
                            if (a != b) {
                                val one = usable[a]
                                val two = usable[b]
                                val list = definitions.getOrPut(id(one, two)) { ObjectArrayList(2) }
                                if (!list.contains(definition)) {
                                    list.add(definition)
                                }
                            }
                        }
                    }
                    count++
                }
            }
            this.definitions = definitions
            count
        }
        return this
    }

    private fun ConfigReader.itemList(items: MutableList<Item>, itemDefinitions: ItemDefinitions?) {
        while (nextElement()) {
            if (peek == '{') {
                var id = ""
                var amount = 0
                while (nextEntry()) {
                    when (key()) {
                        "id" -> id = string()
                        "amount" -> amount = int()
                    }
                }
                if (itemDefinitions != null && !itemDefinitions.contains(id)) {
                    logger.warn { "Invalid item-on-item id: $id" }
                }
                items.add(Item(id, amount))
            } else {
                items.add(Item(string()))
            }
        }
    }

    companion object {
        private val logger = InlineLogger()
        private fun id(one: Item, two: Item): String = "${one.id}&${two.id}"
    }

}
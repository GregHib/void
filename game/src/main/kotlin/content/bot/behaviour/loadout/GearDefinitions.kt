package content.bot.behaviour.loadout

import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.timedLoad

/**
 * Named [Gear] lists from `*.gear.toml` files
 */
object GearDefinitions {
    private var definitions: Map<String, Gear> = emptyMap()

    fun getOrNull(id: String): Gear? = definitions[id]

    fun get(id: String): Gear = definitions[id] ?: error("Unknown gear '$id'.")

    fun load(paths: List<String>): GearDefinitions {
        timedLoad("bot gear") {
            val map = mutableMapOf<String, Gear>()
            for (path in paths) {
                Config.fileReader(path) { readGear(map) }
            }
            validate(map.values)
            definitions = map
            map.size
        }
        return this
    }

    internal fun ConfigReader.readGear(into: MutableMap<String, Gear>) {
        while (nextSection()) {
            val id = section()
            val items = mutableListOf<String>()
            while (nextPair()) {
                when (val key = key()) {
                    "items" -> while (nextElement()) {
                        items.add(string())
                    }
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                }
            }
            require(items.isNotEmpty()) { "Gear '$id' has no items ${exception()}" }
            require(!into.containsKey(id)) { "Duplicate gear '$id' ${exception()}" }
            into[id] = Gear(id, items)
        }
    }

    /**
     * Check all items exist and are for the same equipment slot (skipped if item definitions aren't loaded)
     */
    internal fun validate(gear: Collection<Gear>) {
        if (!ItemDefinitions.loaded) {
            return
        }
        for (entry in gear) {
            val slot = entry.slot
            for (item in entry.items) {
                require(item.slot == slot) { "Gear '${entry.id}' mixes ${slot.name.lowercase()} and ${item.slot.name.lowercase()} items ('${item.id}')." }
            }
        }
    }

    @TestOnly
    fun set(vararg gear: Gear) {
        definitions = gear.associateBy { it.id }
    }

    fun clear() {
        definitions = emptyMap()
    }
}

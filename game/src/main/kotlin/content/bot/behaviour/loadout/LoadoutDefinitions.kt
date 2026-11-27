package content.bot.behaviour.loadout

import org.jetbrains.annotations.TestOnly
import world.gregs.config.Config
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.timedLoad

/**
 * Named [Loadout]s from `*.loadouts.toml` files
 */
object LoadoutDefinitions {
    private var definitions: Map<String, Loadout> = emptyMap()

    fun getOrNull(id: String): Loadout? = definitions[id]

    fun get(id: String): Loadout = definitions[id] ?: error("Unknown loadout '$id'.")

    fun load(paths: List<String>): LoadoutDefinitions {
        timedLoad("bot loadout") {
            val map = mutableMapOf<String, Loadout>()
            for (path in paths) {
                Config.fileReader(path) { readLoadouts(map) }
            }
            for (loadout in map.values) {
                loadout.validate()
            }
            definitions = map
            map.size
        }
        return this
    }

    internal fun ConfigReader.readLoadouts(into: MutableMap<String, Loadout>) {
        while (nextSection()) {
            val id = section()
            val map = mutableMapOf<String, Any>()
            while (nextPair()) {
                val key = key()
                map[key] = value()
            }
            require(!into.containsKey(id)) { "Duplicate loadout '$id' ${exception()}" }
            into[id] = Loadout.parse(id, map, GearDefinitions::getOrNull)
        }
    }

    @TestOnly
    fun set(vararg loadouts: Loadout) {
        definitions = loadouts.associateBy { it.id }
    }

    fun clear() {
        definitions = emptyMap()
    }
}

package world.gregs.voidps.engine.map.area

import org.koin.dsl.module
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.map.spawn.ItemSpawns
import world.gregs.voidps.engine.map.spawn.NPCSpawns
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

val areasModule = module {
    single(createdAtStart = true) { Areas().load() }
    single(createdAtStart = true) { NPCSpawns(get()).load() }
    single(createdAtStart = true) { ItemSpawns(get()).load() }
}

class Areas {

    private var named: Map<String, MapArea> = mutableMapOf()
    private var tagged: Map<String, Set<MapArea>> = mutableMapOf()

    operator fun get(name: String): MapArea? {
        return named[name]
    }

    fun getValue(name: String): MapArea {
        return named[name] ?: MapArea.EMPTY
    }

    fun getTagged(tag: String): Set<MapArea> {
        return tagged[tag] ?: emptySet()
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("areaPath")): Areas {
        timedLoad("map area") {
            val data: Map<String, Map<String, Any>> = storage.load(path)
            val areas = data.mapValues { (key, value) -> MapArea.fromMap(key, value) }

            val tagged = mutableMapOf<String, MutableSet<MapArea>>()
            for (key in data.keys) {
                val area = areas.getValue(key)
                for (tag in area.tags) {
                    tagged.getOrPut(tag) { mutableSetOf() }.add(area)
                }
            }
            this.named = areas
            this.tagged = tagged
            areas.size
        }
        return this
    }

    fun getAll() = named.values

}
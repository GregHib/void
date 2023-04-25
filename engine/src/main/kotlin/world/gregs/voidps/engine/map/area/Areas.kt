package world.gregs.voidps.engine.map.area

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad

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
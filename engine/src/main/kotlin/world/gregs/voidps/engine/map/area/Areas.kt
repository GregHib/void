package world.gregs.voidps.engine.map.area

import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

val areasModule = module {
    single(createdAtStart = true) { Areas().load() }
}

class Areas {

    private var named: Map<String, MapArea> = mutableMapOf()
    private var tagged: Map<String, Set<MapArea>> = mutableMapOf()

    operator fun get(name: String): MapArea? {
        return named[name]
    }

    fun getValue(name: String): MapArea {
        return named[name] ?: empty
    }

    fun getTagged(tag: String): Set<MapArea> {
        return tagged[tag] ?: emptySet()
    }

    fun load(loader: FileLoader = get(), path: String = getProperty("areaPath")) : Areas {
        timedLoad("map area") {
            val data: Map<String, Map<String, Any>> = loader.load(path)
            val areas = data.mapValues { (key, value) -> toArea(key, value) }

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

    private fun toArea(name: String, map: Map<String, Any>): MapArea {
        val area = map["area"] as Map<String, Any>
        val x = area["x"] as List<Int>
        val y = area["y"] as List<Int>
        val plane = area["plane"] as? Int ?: 0
        val shape = if (x.size <= 2) {
            Rectangle(x.first(), y.first(), x.last(), y.last(), plane)
        } else {
            Polygon(x.toIntArray(), y.toIntArray(), plane)
        }
        return MapArea(name, shape, (map["tags"] as? List<String>)?.toSet() ?: emptySet())
    }

    companion object {
        private val empty = MapArea("", Rectangle(0, 0, 0, 0), emptySet())
    }
}
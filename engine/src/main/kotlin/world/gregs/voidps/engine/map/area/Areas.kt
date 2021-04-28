package world.gregs.voidps.engine.map.area

import org.koin.dsl.module

val areasModule = module {
    single(createdAtStart = true) { AreaLoader(get()).run("./data/areas.yml") }
}

class Areas(
    private val named: Map<String, MapArea> = mutableMapOf(),
    private val tagged: Map<String, Set<MapArea>> = mutableMapOf()
) {

    operator fun get(name: String): MapArea? {
        return named[name]
    }

    fun getValue(name: String): MapArea {
        return named[name] ?: empty
    }

    fun getTagged(tag: String): Set<MapArea> {
        return tagged[tag] ?: emptySet()
    }

    companion object {
        private val empty = MapArea("", Rectangle(0, 0, 0, 0), emptyMap())
    }
}
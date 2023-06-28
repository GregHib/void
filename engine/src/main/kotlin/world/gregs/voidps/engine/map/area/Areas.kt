package world.gregs.voidps.engine.map.area

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

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

    @Suppress("UNCHECKED_CAST")
    fun load(parser: Yaml = get(), path: String = getProperty("areaPath")): Areas {
        timedLoad("map area") {
            val config = object : YamlReaderConfiguration() {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "tags") {
                        super.set(map, key, (value as List<Any>).toSet(), indent, parentMap)
                    } else if (key == "area") {
                        value as Map<String, Any>
                        val area = Area.fromMap(value, 0)
                        super.set(map, key, area, indent, parentMap)
                    } else if (indent == 0) {
                        val mapArea = MapArea.fromMap(key, value as Map<String, Any>)
                        super.set(map, key, mapArea, indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            named = parser.load(path, config)
            val tagged = Object2ObjectOpenHashMap<String, MutableSet<MapArea>>()
            for (key in named.keys) {
                val area = named.getValue(key)
                for (tag in area.tags) {
                    tagged.getOrPut(tag) { mutableSetOf() }.add(area)
                }
            }
            this.tagged = tagged
            named.size
        }
        return this
    }

    fun getAll() = named.values

}
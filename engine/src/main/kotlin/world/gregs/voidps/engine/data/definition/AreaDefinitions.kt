package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.zone.Zone
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Area
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class AreaDefinitions {

    private var named: Map<String, AreaDefinition> = mutableMapOf()
    private var tagged: Map<String, Set<AreaDefinition>> = mutableMapOf()
    private var zones: Map<Int, List<AreaDefinition>> = Int2ObjectOpenHashMap()

    fun getOrNull(name: String): AreaDefinition? {
        return named[name]
    }

    operator fun get(name: String): Area {
        return named[name]?.area ?: AreaDefinition.EMPTY.area
    }

    fun get(zone: Zone): List<AreaDefinition> {
        return zones[zone.id] ?: emptyList()
    }

    fun getTagged(tag: String): Set<AreaDefinition> {
        return tagged[tag] ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = getProperty("areaPath")): AreaDefinitions {
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
                        val area = AreaDefinition.fromMap(key, value as Map<String, Any>)
                        super.set(map, key, area, indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            named = yaml.load(path, config)
            val tagged = Object2ObjectOpenHashMap<String, MutableSet<AreaDefinition>>()
            val zones = Int2ObjectOpenHashMap<MutableList<AreaDefinition>>()
            for (key in named.keys) {
                val area = named.getValue(key)
                for (tag in area.tags) {
                    tagged.getOrPut(tag) { mutableSetOf() }.add(area)
                }
                for (zone in area.area.toZones()) {
                    zones.getOrPut(zone.id) { mutableListOf() }.add(area)
                }
            }
            this.zones = zones
            this.tagged = tagged
            named.size
        }
        return this
    }

    fun getAll() = named.values

}
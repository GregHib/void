package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Zone
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

class AreaDefinitions(
    private var named: Map<String, AreaDefinition> = Object2ObjectOpenHashMap(),
    private var tagged: Map<String, Set<AreaDefinition>> = Object2ObjectOpenHashMap(),
    private var areas: Map<Int, Set<AreaDefinition>> = Int2ObjectOpenHashMap()
) {

    fun getOrNull(name: String): AreaDefinition? {
        return named[name]
    }

    operator fun get(name: String): Area {
        return named[name]?.area ?: AreaDefinition.EMPTY.area
    }

    fun get(zone: Zone): Set<AreaDefinition> {
        return areas[zone.id] ?: emptySet()
    }

    fun getTagged(tag: String): Set<AreaDefinition> {
        return tagged[tag] ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["areaPath"]): AreaDefinitions {
        timedLoad("map area") {
            val config = object : YamlReaderConfiguration(2, 2) {
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (key == "tags") {
                        super.set(map, key, ObjectOpenHashSet(value as List<Any>), indent, parentMap)
                    } else if (key == "area") {
                        value as Map<String, Any>
                        val area = Area.fromMap(value, 3)
                        super.set(map, key, area, indent, parentMap)
                    } else if (indent == 0) {
                        val area = AreaDefinition.fromMap(key, value as MutableMap<String, Any>)
                        super.set(map, key, area, indent, parentMap)
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            named = yaml.load(path, config)
            val tagged = Object2ObjectOpenHashMap<String, MutableSet<AreaDefinition>>()
            val areas = Int2ObjectOpenHashMap<MutableSet<AreaDefinition>>()
            for (key in named.keys) {
                val area = named.getValue(key)
                for (tag in area.tags) {
                    tagged.getOrPut(tag) { ObjectOpenHashSet(2) }.add(area)
                }
                for (zone in area.area.toZones()) {
                    areas.getOrPut(zone.id) { ObjectOpenHashSet(2) }.add(area)
                }
            }
            this.areas = areas
            this.tagged = tagged
            named.size
        }
        return this
    }

    fun getAll() = named.values

}
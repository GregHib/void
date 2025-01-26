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

/**
 * Represents a collection of area definitions, which can be accessed based on various keys such as
 * name, tags, or zones. This class centralizes the management and retrieval of area definitions,
 * allowing for efficient queries and lookups.
 *
 * @property named A mapping of area names to their respective area definitions.
 * @property tagged A mapping of tags to sets of area definitions that carry those tags.
 * @property areas A mapping of zone IDs to sets of area definitions that belong to those zones.
 */
class AreaDefinitions(
    private var named: Map<String, AreaDefinition> = Object2ObjectOpenHashMap(),
    private var tagged: Map<String, Set<AreaDefinition>> = Object2ObjectOpenHashMap(),
    private var areas: Map<Int, Set<AreaDefinition>> = Int2ObjectOpenHashMap()
) {

    /**
     * Retrieves the AreaDefinition associated with the given name, or returns null if no such entry exists.
     *
     * @param name The key used to look up the associated AreaDefinition.
     * @return The AreaDefinition corresponding to the given name, or null if the name is not found.
     */
    fun getOrNull(name: String): AreaDefinition? {
        return named[name]
    }

    /**
     * Retrieves the area associated with the given name.
     *
     * @param name The name of the area to retrieve.
     * @return The area corresponding to the provided name, or an empty area if the name is not found.
     */
    operator fun get(name: String): Area {
        return named[name]?.area ?: AreaDefinition.EMPTY.area
    }

    /**
     * Retrieves a set of area definitions associated with the given zone.
     *
     * @param zone the zone whose associated area definitions are to be retrieved
     * @return a set of area definitions associated with the specified zone,
     * or an empty set if no area definitions are found for the given zone
     */
    fun get(zone: Zone): Set<AreaDefinition> {
        return areas[zone.id] ?: emptySet()
    }

    /**
     * Retrieves a set of `AreaDefinition` objects associated with the specified tag.
     *
     * @param tag The tag used to filter and retrieve the associated `AreaDefinition` objects.
     * @return A set of `AreaDefinition` objects associated with the given tag. Returns an empty set if no associated objects are found.
     */
    fun getTagged(tag: String): Set<AreaDefinition> {
        return tagged[tag] ?: emptySet()
    }

    /**
     * Loads area definitions from the provided YAML configuration file.
     *
     * @param yaml An instance of the `Yaml` parser used to load the configuration. Default is the instance returned by `get()`.
     * @param path The file path to the YAML file containing the area definitions. Default is the value of `Settings["map.areas"]`.
     * @return A populated `AreaDefinitions` object containing named, tagged, and zoned area definitions.
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["map.areas"]): AreaDefinitions {
        timedLoad("map area") {
            val config = object : YamlReaderConfiguration(2, 2) {
                /**
                 * Sets a value in the provided map with considerations for specific keys and their associated types.
                 *
                 * @param map The mutable map where the key-value pair will be set.
                 * @param key The key to be added or updated in the map.
                 * @param value The value to be associated with the specified key.
                 * @param indent The indentation level influencing specific handling logic.
                 * @param parentMap The parent map's identifier, if applicable.
                 */
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

    /**
     * Retrieves a collection of all values stored in the `named` map.
     *
     * This method accesses the `values` property of the internal `named` map
     * and returns a view of all the values contained within.
     *
     * @return A collection containing all values from the `named` map.
     */
    fun getAll() = named.values

}
package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.PrayerDefinition
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

/**
 * Represents a collection of prayer definitions that include prayers, curses, and their associated metadata.
 * Provides methods to load and access prayer definitions, and organizes them by groups for logical categorization.
 */
class PrayerDefinitions {

    /**
     * A lazily initialized map that holds prayer definitions, where the key is a string identifier
     * and the value is an instance of `PrayerDefinition`.
     */
    private lateinit var definitions: Map<String, PrayerDefinition>
    /**
     * A variable holding an array of PrayerDefinition objects.
     * This variable is initialized later in the program and intended to store
     * the definitions or details of different prayers.
     * It is defined as `lateinit` to allow deferred initialization.
     */
    private lateinit var prayers: Array<PrayerDefinition>
    /**
     * A private, late-initialized variable that holds an array of `PrayerDefinition` objects.
     * This variable is intended to store a collection of prayer definitions
     * and must be initialized before usage to avoid runtime exceptions.
     */
    private lateinit var curses: Array<PrayerDefinition>
    /**
     * Represents categorized groups of related strings identified by an integer key.
     *
     * The map associates each integer key with a set of strings, where:
     * - The integer key serves as the identifier for a specific group.
     * - The set of strings contains related or grouped values.
     *
     * This property is used to manage or reference predefined groups of strings within a data structure
     * or process, such as categorizing items, settings, or elements.
     */
    private lateinit var groups: Map<Int, Set<String>>

    /**
     * Retrieves a value associated with the given key. If the key does not exist, returns an empty `PrayerDefinition`.
     *
     * @param key the key for which the value is to be retrieved
     * @return the value associated with the key or an empty `PrayerDefinition` if the key does not exist
     */
    fun get(key: String) = getOrNull(key) ?: PrayerDefinition.EMPTY

    /**
     * Retrieves the value associated with the given key from the definitions map,
     * or returns null if the key does not exist in the map.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the specified key, or null if the key is not present in the map.
     */
    fun getOrNull(key: String) = definitions[key]

    /**
     * Retrieves a prayer from the list of prayers based on the provided index.
     *
     * @param index The position in the prayers list to retrieve.
     * @return The prayer at the specified index.
     * @throws IndexOutOfBoundsException If the index is out of range of the prayers list.
     */
    fun getPrayer(index: Int) = prayers[index]

    /**
     * Retrieves a curse definition at the specified index.
     *
     * @param index The index of the curse to retrieve.
     * @return The curse located at the given index.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    fun getCurse(index: Int) = curses[index]

    /**
     * Retrieves a group from the `groups` collection using the provided index.
     *
     * @param group The index of the group to retrieve.
     * @return The group corresponding to the provided index.
     */
    fun getGroup(group: Int) = groups[group]

    /**
     * Loads prayer definitions from a YAML file and processes the data into structured definitions.
     *
     * @param yaml The YAML parser instance to use for loading data. Defaults to a newly created instance if not provided.
     * @param path The file path or key in the settings where the YAML data is stored. Defaults to the configured path for prayer definitions.
     * @return The processed prayer definitions structured into various categories (e.g., prayers, curses, groups).
     */
    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["definitions.prayers"]): PrayerDefinitions {
        timedLoad("prayer definition") {
            val definitions = Object2ObjectOpenHashMap<String, PrayerDefinition>()
            val prayers = Int2ObjectArrayMap<PrayerDefinition>()
            val curses = Int2ObjectArrayMap<PrayerDefinition>()
            val config = object : YamlReaderConfiguration() {
                /**
                 * Overrides the set method to process and store prayer or curse definitions based on the provided key and value.
                 *
                 * @param map The mutable map in which the provided key-value pair is to be processed and stored.
                 * @param key The key used to identify a prayer or curse, which may include specific suffixes like "_curse" or "_prayer".
                 * @param value The value associated with the key, which can be a Map or any other data type.
                 * @param indent The depth level used to determine the hierarchy of processing; special processing occurs when indent is 0.
                 * @param parentMap The parent map associated with the operation, which may be null.
                 */
                override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                    if (indent == 0) {
                        val id = key.removeSuffix("_curse").removeSuffix("_prayer")
                        val definition = if (value is Map<*, *>) {
                            PrayerDefinition(id, value as MutableMap<String, Any>)
                        } else {
                            PrayerDefinition(stringId = id)
                        }
                        if (key.endsWith("_curse")) {
                            curses[definition.index] = definition
                        } else {
                            prayers[definition.index] = definition
                        }
                        definitions[id] = definition
                    } else {
                        super.set(map, key, value, indent, parentMap)
                    }
                }
            }
            yaml.load<Any>(path, config)
            val groups = Int2ObjectOpenHashMap<MutableSet<String>>(16)
            for (prayer in definitions.values) {
                for (group in prayer.groups) {
                    groups.getOrPut(group) { mutableSetOf() }.add(prayer.stringId)
                }
            }
            this.prayers = Array(prayers.size) { prayers[it] }
            this.curses = Array(curses.size) { curses[it] }
            this.groups = groups
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

}
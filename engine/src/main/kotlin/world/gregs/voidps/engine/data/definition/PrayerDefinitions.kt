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

class PrayerDefinitions {

    private lateinit var definitions: Map<String, PrayerDefinition>
    private lateinit var prayers: Array<PrayerDefinition>
    private lateinit var curses: Array<PrayerDefinition>
    private lateinit var groups: Map<Int, Set<String>>

    fun get(key: String) = getOrNull(key) ?: PrayerDefinition.EMPTY

    fun getOrNull(key: String) = definitions[key]

    fun getPrayer(index: Int) = prayers[index]

    fun getCurse(index: Int) = curses[index]

    fun getGroup(group: Int) = groups[group]

    @Suppress("UNCHECKED_CAST")
    fun load(yaml: Yaml = get(), path: String = Settings["prayerDefinitionsPath"]): PrayerDefinitions {
        timedLoad("prayer definition") {
            val definitions = Object2ObjectOpenHashMap<String, PrayerDefinition>()
            val prayers = Int2ObjectArrayMap<PrayerDefinition>()
            val curses = Int2ObjectArrayMap<PrayerDefinition>()
            val config = object : YamlReaderConfiguration() {
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
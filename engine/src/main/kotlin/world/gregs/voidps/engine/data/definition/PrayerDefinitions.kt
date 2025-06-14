package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.PrayerDefinition
import world.gregs.voidps.engine.timedLoad

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

    fun load(path: String): PrayerDefinitions {
        timedLoad("prayer definition") {
            val definitions = Object2ObjectOpenHashMap<String, PrayerDefinition>()
            val prayers = Int2ObjectArrayMap<PrayerDefinition>()
            val curses = Int2ObjectArrayMap<PrayerDefinition>()
            Config.fileReader(path) {
                while (nextSection()) {
                    val stringId = section()
                    var index = -1
                    var level = 1
                    var drain = 0
                    val groups = IntArrayList()
                    val bonuses = Object2IntOpenHashMap<String>()
                    var members = false
                    while (nextPair()) {
                        when (val key = key()) {
                            "index" -> index = int()
                            "level" -> level = int()
                            "drain" -> drain = int()
                            "groups" -> while (nextElement()) {
                                groups.add(int())
                            }
                            "bonuses" -> while (nextEntry()) {
                                bonuses[key()] = int()
                            }
                            "members" -> members = boolean()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                        val id = stringId.substring(0, stringId.lastIndexOf('_'))
                        val definition = PrayerDefinition(index, level, drain, groups, bonuses, members, id)
                        definitions[id] = definition
                        if (stringId.endsWith("_curse")) {
                            curses[index] = definition
                        } else {
                            prayers[index] = definition
                        }
                    }
                }
            }
            val groups = Int2ObjectOpenHashMap<MutableSet<String>>(16)
            for (prayer in definitions.values) {
                for (group in prayer.groups) {
                    groups.getOrPut(group) { ObjectArraySet(5) }.add(prayer.stringId)
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

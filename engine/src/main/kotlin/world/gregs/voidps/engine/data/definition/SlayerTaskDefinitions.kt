package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.SlayerTaskDefinition
import world.gregs.voidps.engine.timedLoad

class SlayerTaskDefinitions {

    private lateinit var taskLists: Map<String, Map<String, SlayerTaskDefinition>>

    fun get(name: String): Map<String, SlayerTaskDefinition> = taskLists.getValue(name)

    fun load(paths: List<String>): SlayerTaskDefinitions {
        timedLoad("slayer task list") {
            val lists = Object2ObjectOpenHashMap<String, Map<String, SlayerTaskDefinition>>(10, Hash.VERY_FAST_LOAD_FACTOR)
            for (path in paths) {
                val fileName = path.substringAfterLast('\\').removeSuffix(".${Settings["definitions.slayerTasks"]}")
                val map = Object2ObjectOpenHashMap<String, SlayerTaskDefinition>()
                Config.fileReader(path, 200) {
                    while (nextSection()) {
                        val type = section()
                        var tip = ""
                        var min = 1
                        var max = 1
                        var weight = 1
                        var slayer = 1
                        var combat = 1
                        val quests = ObjectOpenHashSet<String>(1)
                        while (nextPair()) {
                            when (val key = key()) {
                                "tip" -> tip = string()
                                "min" -> min = int()
                                "max" -> max = int()
                                "weight" -> weight = int()
                                "slayer_level" -> slayer = int()
                                "combat_level" -> combat = int()
                                "quest" -> quests.add(string())
                                else -> throw IllegalArgumentException("Unknown slayer task key: $key")
                            }
                        }
                        map[type] = SlayerTaskDefinition(type, tip, min..max, weight, slayer, combat, quests)
                    }
                }
                lists[fileName] = map
            }
            this.taskLists = lists
            taskLists.size
        }
        return this
    }
}

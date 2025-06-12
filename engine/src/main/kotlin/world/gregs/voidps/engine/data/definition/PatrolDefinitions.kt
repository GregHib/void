package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.Config
import world.gregs.voidps.engine.data.config.PatrolDefinition
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Tile

/**
 * Also known as routes or paths
 */
class PatrolDefinitions {

    private lateinit var definitions: Map<String, PatrolDefinition>

    fun get(key: String) = definitions[key] ?: PatrolDefinition()

    fun load(paths: List<String>): PatrolDefinitions {
        timedLoad("patrol definition") {
            val definitions = Object2ObjectOpenHashMap<String, PatrolDefinition>()
            for (path in paths) {
                Config.fileReader(path) {
                    while (nextSection()) {
                        val stringId = section()
                        val points = ObjectArrayList<Pair<Tile, Int>>(10)
                        while (nextPair()) {
                            when (val key = key()) {
                                "points" -> while (nextElement()) {
                                    var x = 0
                                    var y = 0
                                    var level = 0
                                    var delay = 0
                                    while (nextEntry()) {
                                        when (val k = key()) {
                                            "x" -> x = int()
                                            "y" -> y = int()
                                            "level" -> level = int()
                                            "delay" -> delay = int()
                                            else -> throw IllegalArgumentException("Unexpected key: '$k' ${exception()}")
                                        }
                                    }
                                    points.add(Tile(x, y, level) to delay)
                                }
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        definitions[stringId] = PatrolDefinition(stringId = stringId, waypoints = points)
                    }
                }
            }
            this.definitions = definitions
            this.definitions.size
        }
        return this
    }

}
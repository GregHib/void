package content.area.karamja.tzhaar_city

import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Direction

class TzhaarFightCaveWaves {

    private val waves = Array<List<String>>(64) { emptyList() }
    private val rotations = Array<Array<List<Direction>>>(64) { Array(15) { listOf() } }

    fun npcs(wave: Int): List<String> = waves[wave - 1]

    fun spawns(wave: Int, rotation: Int): List<Direction> = rotations[wave - 1][rotation - 1]

    fun load(path: String) = timedLoad("fight cave wave rotation") {
        var count = 0
        Config.fileReader(path) {
            while (nextSection()) {
                val section = section()
                val wave = section.removePrefix("wave_").toInt() - 1
                while (nextPair()) {
                    val key = key()
                    if (key == "npcs") {
                        val npcs = mutableListOf<String>()
                        while (nextElement()) {
                            npcs.add(string())
                        }
                        waves[wave] = npcs
                        continue
                    }
                    val rotation = key.removePrefix("rotation_").toInt() - 1
                    val directions = mutableListOf<Direction>()
                    while (nextElement()) {
                        directions.add(Direction.valueOf(string().uppercase()))
                    }
                    rotations[wave][rotation] = directions
                    count++
                }
            }
        }
        count
    }
}

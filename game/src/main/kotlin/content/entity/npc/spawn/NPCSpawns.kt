package content.entity.npc.spawn

import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.type.Direction
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.timedLoad

fun loadNpcSpawns(
    npcs: NPCs,
    path: String = Settings["spawns.npcs"]
) {
    timedLoad("npc spawn") {
        npcs.clear()
        val membersWorld = World.members
        Config.fileReader(path) {
            while (nextPair()) {
                require(key() == "spawns")
                while (nextElement()) {
                    var id = ""
                    var direction = Direction.NONE
                    var x = 0
                    var y = 0
                    var level = 0
                    var delay: Int? = null
                    var members = false
                    while (nextEntry()) {
                        when (val key = key()) {
                            "id" -> id = string()
                            "x" -> x = int()
                            "y" -> y = int()
                            "level" -> level = int()
                            "direction" -> direction = Direction.valueOf(string())
                            "delay" -> delay = int()
                            "members" -> members = boolean()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    if (!membersWorld && members) {
                        continue
                    }
                    val tile = Tile(x, y, level)
                    npcs.add(id, tile, direction, delay)
                }
            }
        }
        npcs.size
    }
}
package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

private val logger = InlineLogger()

fun loadNpcSpawns(
    npcs: NPCs,
    paths: List<String>,
    npcDefinitions: NPCDefinitions,
) {
    timedLoad("npc spawn") {
        npcs.clear()
        val membersWorld = World.members
        var count = 0
        val hashes = mutableSetOf<Long>()
        val debug = !Settings["server.live", false]
        for (path in paths) {
            Config.fileReader(path) {
                while (nextPair()) {
                    require(key() == "spawns")
                    while (nextElement()) {
                        var id = ""
                        var direction = Direction.NONE
                        var x = 0
                        var y = 0
                        var level = 0
                        var members = false
                        while (nextEntry()) {
                            when (val key = key()) {
                                "id" -> id = string()
                                "x" -> x = int()
                                "y" -> y = int()
                                "level" -> level = int()
                                "direction" -> direction = Direction.valueOf(string())
                                "members" -> members = boolean()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        if (!membersWorld && members) {
                            continue
                        }
                        val tile = Tile(x, y, level)
                        val definition = npcDefinitions.getOrNull(id)
                        if (definition == null) {
                            logger.warn { "Invalid npc spawn id '$id' in $path." }
                        } else if (debug) {
                            val hash = (tile.id.toLong() shl 32) + definition.id.toLong()
                            if (hashes.contains(hash)) {
                                logger.warn { "Duplicate spawn id = \"$id\" x = ${tile.x}, y = ${tile.y}${if (tile.level != 0) ", level = ${tile.level}" else ""} in $path." }
                            }
                            hashes.add(hash)
                        }
                        npcs.add(id, tile, direction)
                        count++
                    }
                }
            }
        }
        count
    }
}

package world.gregs.voidps.engine.entity.obj

import com.github.michaelbull.logging.InlineLogger
import world.gregs.config.Config
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Tile

private val logger = InlineLogger()

data class ObjectSpawn(
    val id: String,
    val x: Int,
    val y: Int,
    val level: Int,
    val type: Int,
    val rotation: Int,
    val members: Boolean = false,
    val remove: Boolean = false,
)

fun readObjectSpawns(paths: List<String>): List<ObjectSpawn> {
    val spawns = mutableListOf<ObjectSpawn>()
    for (path in paths) {
        Config.fileReader(path) {
            while (nextPair()) {
                require(key() == "spawns")
                while (nextElement()) {
                    var id = ""
                    var rotation = 0
                    var x = 0
                    var y = 0
                    var level = 0
                    var type = 10
                    var members = false
                    var remove = false
                    while (nextEntry()) {
                        when (val key = key()) {
                            "id" -> id = string()
                            "x" -> x = int()
                            "y" -> y = int()
                            "level" -> level = int()
                            "rotation" -> rotation = int()
                            "type" -> type = int()
                            "members" -> members = boolean()
                            "remove" -> remove = boolean()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    spawns += ObjectSpawn(id, x, y, level, type, rotation, members, remove)
                }
            }
        }
    }
    return spawns
}

fun loadObjectSpawns(paths: List<String>) = timedLoad("object spawn") {
    GameObjects.reset()
    val membersWorld = World.members
    var count = 0
    for (spawn in readObjectSpawns(paths)) {
        if (!membersWorld && spawn.members) {
            continue
        }
        val tile = Tile(spawn.x, spawn.y, spawn.level)
        val definition = ObjectDefinitions.getOrNull(spawn.id)
        if (definition == null) {
            logger.warn { "Invalid object spawn id '${spawn.id}'." }
            continue
        }
        val gameObject = GameObject(definition.id, tile.x, tile.y, tile.level, spawn.type, spawn.rotation)
        if (spawn.remove) {
            GameObjects.remove(gameObject, collision = true)
        } else {
            GameObjects.add(gameObject)
            count++
        }
    }
    count
}

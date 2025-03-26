package world.gregs.voidps.engine.entity.obj

import com.github.michaelbull.logging.InlineLogger
import world.gregs.config.Config
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.timedLoad

private val logger = InlineLogger()

fun loadObjectSpawns(
    objects: GameObjects,
    paths: List<String>,
    definitions: ObjectDefinitions,
) = timedLoad("object spawn") {
    objects.reset()
    val membersWorld = World.members
    var count = 0
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
                    while (nextEntry()) {
                        when (val key = key()) {
                            "id" -> id = string()
                            "x" -> x = int()
                            "y" -> y = int()
                            "level" -> level = int()
                            "rotation" -> rotation = int()
                            "type" -> type = int()
                            "members" -> members = boolean()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    if (!membersWorld && members) {
                        continue
                    }
                    val tile = Tile(x, y, level)
                    val definition = definitions.getOrNull(id)
                    if (definition == null) {
                        logger.warn { "Invalid object spawn id '$id' in ${path}." }
                    } else {
                        objects.add(GameObject(definition.id, tile.x, tile.y, tile.level, type, rotation))
                        count++
                    }
                }
            }
        }
    }
    count
}
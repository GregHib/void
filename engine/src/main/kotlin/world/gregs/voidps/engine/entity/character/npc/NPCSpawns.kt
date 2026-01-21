package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.config.Config
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.io.File

private val logger = InlineLogger()

fun loadNpcSpawns(paths: List<String>, reload: Boolean = false) {
    timedLoad("npc spawn") {
        NPCs.clear()
        val file = File("${Settings["storage.caching.path"]}npc_spawns.dat")
        if (reload || !file.exists()) {
            loadNormal(paths, file, Settings["storage.caching.active", false])
        } else {
            loadFast(file)
        }
    }
}

private fun loadFast(file: File): Int {
    val reader = ArrayReader(file.readBytes())
    val count = reader.readInt()
    val membersWorld = World.members
    for (i in 0 until count) {
        val id = reader.readString()
        val tile = Tile(reader.readInt())
        val direction = Direction.entries[reader.readByte()]
        val members = reader.readBoolean()
        if (!membersWorld && members) {
            continue
        }
        NPCs.add(id, tile, direction)
    }
    return count
}

private fun loadNormal(paths: List<String>, file: File, save: Boolean): Int {
    val writer = ArrayWriter(1_000_000)
    writer.writeInt(0) // Placeholder
    val membersWorld = World.members
    var count = 0
    val hashes = mutableSetOf<Long>()
    for (path in paths) {
        Config.fileReader(path) {
            while (nextPair()) {
                require(key() == "spawns")
                while (nextElement()) {
                    var id = ""
                    var direction = Direction.SOUTH
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
                    val tile = Tile(x, y, level)
                    writer.writeString(id)
                    writer.writeInt(tile.id)
                    writer.writeByte(direction.ordinal)
                    writer.writeByte(members)
                    if (!membersWorld && members) {
                        continue
                    }
                    val definition = NPCDefinitions.getOrNull(id)
                    if (definition == null) {
                        logger.warn { "Invalid npc spawn id '$id' in $path." }
                    } else {
                        val hash = (tile.id.toLong() shl 32) + definition.id.toLong()
                        if (hashes.contains(hash)) {
                            logger.warn { "Duplicate spawn id = \"$id\" x = ${tile.x}, y = ${tile.y}${if (tile.level != 0) ", level = ${tile.level}" else ""} in $path." }
                        }
                        hashes.add(hash)
                    }
                    NPCs.add(id, tile, direction)
                    count++
                }
            }
        }
    }
    val end = writer.position()
    writer.position(0)
    writer.writeInt(count)
    writer.position(end)
    if (save) {
        file.writeBytes(writer.toArray())
    }
    return count
}

package world.gregs.voidps.engine.entity.character.npc

import com.github.michaelbull.logging.InlineLogger
import world.gregs.config.Config
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.io.File

private val logger = InlineLogger()
private data class SpawnRecord(
    val id: String,
    val x: Int,
    val y: Int,
    val level: Int,
    val direction: Direction,
    val members: Boolean,
    val remove: Boolean,
)

fun loadNpcSpawns(files: ConfigFiles, reload: Boolean = false) {
    timedLoad("npc spawn") {
        NPCs.clear()
        val file = File("${Settings["storage.caching.path"]}${Settings["storage.caching.npcSpawns"]}")
        val extension = Settings["spawns.npcs"]
        val editorPath = Settings["spawns.npcs.editor", ""]
        val editorFile = if (editorPath.isBlank()) null else File(Settings["storage.data"], editorPath)
        val paths = files.list(extension).toMutableList()
        if (editorFile?.isFile == true && editorFile.path !in paths) {
            paths += editorFile.path
        }
        val hasEditorSpawns = editorFile?.isFile == true
        if (reload || !file.exists() || files.extensions.contains(extension) || hasEditorSpawns) {
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
    val records = mutableListOf<SpawnRecord>()
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
                    var remove = false
                    while (nextEntry()) {
                        when (val key = key()) {
                            "id" -> id = string()
                            "x" -> x = int()
                            "y" -> y = int()
                            "level" -> level = int()
                            "direction" -> direction = Direction.valueOf(string())
                            "members" -> members = boolean()
                            "remove" -> remove = boolean()
                            else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                        }
                    }
                    records += SpawnRecord(id, x, y, level, direction, members, remove)
                }
            }
        }
    }
    val removals = records.asSequence()
        .filter { it.remove }
        .mapNotNull { record ->
            val definition = NPCDefinitions.getOrNull(record.id) ?: return@mapNotNull null
            (Tile(record.x, record.y, record.level).id.toLong() shl 32) + definition.id.toLong()
        }
        .toSet()
    var count = 0
    val hashes = mutableSetOf<Long>()
    for (record in records) {
        writer.writeString(record.id)
        writer.writeInt(Tile(record.x, record.y, record.level).id)
        writer.writeByte(record.direction.ordinal)
        writer.writeByte(record.members)
        if (record.remove || (!membersWorld && record.members)) {
            continue
        }
        val tile = Tile(record.x, record.y, record.level)
        val definition = NPCDefinitions.getOrNull(record.id)
        if (definition == null) {
            logger.warn { "Invalid npc spawn id '${record.id}'." }
        } else {
            val hash = (tile.id.toLong() shl 32) + definition.id.toLong()
            if (hash in removals) {
                continue
            }
            if (hashes.contains(hash)) {
                logger.warn { "Duplicate spawn id = \"${record.id}\" x = ${tile.x}, y = ${tile.y}${if (tile.level != 0) ", level = ${tile.level}" else ""}." }
            }
            hashes.add(hash)
        }
        NPCs.add(record.id, tile, record.direction)
        count++
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

package content.entity.player.command

import com.github.michaelbull.logging.InlineLogger
import world.gregs.config.Config
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.loadObjectSpawns
import world.gregs.voidps.engine.entity.obj.readObjectSpawns
import world.gregs.voidps.type.Tile
import java.io.File

object SceneEditorPersist {
    private val logger = InlineLogger()
    private val lock = Any()

    data class Entry(
        val objectId: Int,
        val x: Int,
        val y: Int,
        val plane: Int,
        val rotation: Int,
        val shape: Int,
        val remove: Boolean,
    )
    data class NpcEntry(
        val id: String,
        val x: Int,
        val y: Int,
        val plane: Int,
        val remove: Boolean = false,
    )

    fun place(objectId: Int, x: Int, y: Int, plane: Int, rotation: Int = 0, shape: Int = ObjectShape.CENTRE_PIECE_STRAIGHT): String = synchronized(lock) {
        val definition = ObjectDefinitions.getOrNull(objectId) ?: return "unknown object id $objectId"
        val entry = Entry(definition.id, x, y, plane, rotation and 3, shape, remove = false)
        update(entry)
        reload()
        "placed ${definition.stringId.ifBlank { definition.id.toString() }} @ $x,$y,$plane rot=${entry.rotation}"
    }

    fun remove(objectId: Int, x: Int, y: Int, plane: Int, rotation: Int = 0, shape: Int = ObjectShape.CENTRE_PIECE_STRAIGHT): String = synchronized(lock) {
        val tile = Tile(x, y, plane)
        val removed = removeLive(objectId, tile, shape, rotation and 3)
        update(Entry(objectId, x, y, plane, rotation and 3, shape, remove = true))
        reload()
        if (removed) {
            "removed $objectId @ $x,$y,$plane (collision cleared)"
        } else {
            "queued remove $objectId @ $x,$y,$plane (not in live map)"
        }
    }

    fun spawnNpc(definitionId: Int, x: Int, y: Int, plane: Int): String = synchronized(lock) {
        val definition = NPCDefinitions.getOrNull(definitionId)
            ?: return "unknown NPC id $definitionId"
        val id = definition.stringId.ifBlank { definitionId.toString() }
        val tile = Tile(x, y, plane)
        if (NPCs.findOrNull(tile, id) != null) {
            return "NPC already present: $id @ $x,$y,$plane"
        }
        val entries = readNpcEntries().filterNot { it.id == id && it.x == x && it.y == y && it.plane == plane }.toMutableList()
        entries += NpcEntry(id, x, y, plane)
        writeNpcSpawns(entries)
        NPCs.add(id, tile)
        "spawned $id (#$definitionId) @ $x,$y,$plane"
    }

    fun removeNpc(definitionId: Int, x: Int, y: Int, plane: Int): String = synchronized(lock) {
        val definition = NPCDefinitions.getOrNull(definitionId)
            ?: return "unknown NPC id $definitionId"
        val id = definition.stringId.ifBlank { definitionId.toString() }
        val tile = Tile(x, y, plane)
        val live = NPCs.findOrNull(tile, id)
            ?: NPCs.at(tile.regionLevel).firstOrNull { npc ->
                npc.id == id
                        && kotlin.math.abs(npc.tile.x - x) <= 1
                        && kotlin.math.abs(npc.tile.y - y) <= 1
                        && npc.tile.level == plane
            }
        if (live != null) {
            NPCs.remove(live)
        }
        // NPCs can wander after spawning. Persist the removal at their original
        // spawn_tile, not at the tile where the client happened to click them.
        val spawnTile = live?.get<Tile>("spawn_tile") ?: tile
        val entries = readNpcEntries()
        val matching = entries.indices
            .filter { entries[it].id == id && entries[it].plane == spawnTile.level }
            .minByOrNull { kotlin.math.abs(entries[it].x - spawnTile.x) + kotlin.math.abs(entries[it].y - spawnTile.y) }
            ?.takeIf {
                kotlin.math.abs(entries[it].x - spawnTile.x) <= 1
                        && kotlin.math.abs(entries[it].y - spawnTile.y) <= 1
            }
        val remaining = if (matching == null) {
            entries
        } else {
            entries.filterIndexed { index, _ -> index != matching }
        }.toMutableList()
        val persisted = matching != null
        if (remaining.none {
                it.remove && it.id == id && it.x == spawnTile.x
                        && it.y == spawnTile.y && it.plane == spawnTile.level
            }) {
            remaining += NpcEntry(id, spawnTile.x, spawnTile.y, spawnTile.level, remove = true)
        }
        writeNpcSpawns(remaining)
        when {
            live != null && persisted -> "removed $id (#$definitionId) @ $x,$y,$plane"
            live != null -> "removed live $id (#$definitionId) @ $x,$y,$plane"
            persisted -> "removed persisted $id (#$definitionId) @ $x,$y,$plane"
            else -> "queued removal $id (#$definitionId) @ $x,$y,$plane"
        }
    }

    fun flush(): String = synchronized(lock) {
        val entries = readEntries()
        reload()
        // Save is the object-editor flush. NPC commands update their live entity
        // and persistence file directly; reloading NPC spawns here would recreate
        // removed server NPCs while the object editor is being saved.
        logger.info { "scene editor reloaded ${entries.size} persisted changes" }
        "reloaded ${entries.size} scene-editor change(s) from ${file.name}"
    }

    fun status(): String = synchronized(lock) {
        val entries = readEntries()
        "stored=+${entries.count { !it.remove }}/-${entries.count { it.remove }}"
    }

    private fun update(entry: Entry) {
        val entries = readEntries().toMutableList()
        val index = entries.indexOfFirst { it.x == entry.x && it.y == entry.y && it.plane == entry.plane && it.shape == entry.shape }
        if (index == -1) {
            entries += entry
        } else {
            entries[index] = entry
        }
        writeToml(entries)
    }
    private fun reload() {
        val files = configFiles()
        loadObjectSpawns(files.list(Settings["spawns.objects"]))
    }

    private fun removeLive(objectId: Int, tile: Tile, shape: Int, rotation: Int): Boolean {
        val byShape = GameObjects.getShape(tile, shape)
        if (byShape != null && byShape.intId == objectId) {
            GameObjects.remove(byShape, collision = true)
            return true
        }
        val byId = GameObjects.findOrNull(tile, objectId)
        if (byId != null) {
            GameObjects.remove(byId, collision = true)
            return true
        }
        val probe = GameObject(objectId, tile, shape, rotation)
        if (GameObjects.contains(probe)) {
            GameObjects.remove(probe, collision = true)
            return true
        }
        return false
    }

    private fun readEntries(): List<Entry> {
        if (!file.exists()) {
            return emptyList()
        }
        return readObjectSpawns(listOf(file.path)).mapNotNull { spawn ->
            val objectId = spawn.id.toIntOrNull() ?: ObjectDefinitions.getOrNull(spawn.id)?.id ?: return@mapNotNull null
            Entry(objectId, spawn.x, spawn.y, spawn.level, spawn.rotation and 3, spawn.type, spawn.remove)
        }
    }

    private fun writeToml(entries: List<Entry>) {
        file.parentFile.mkdirs()
        file.writeText(
            buildString {
                appendLine("# Auto-generated by scene editor. Do not hand-edit.")
                appendLine("spawns = [")
                for (entry in entries.sortedWith(compareBy({ it.x }, { it.y }, { it.plane }, { it.shape }))) {
                    val id = ObjectDefinitions.getOrNull(entry.objectId)?.stringId?.takeIf { it.isNotBlank() }
                        ?: entry.objectId.toString()
                    append("    { id = \"").append(id).append("\", x = ").append(entry.x)
                        .append(", y = ").append(entry.y).append(", level = ").append(entry.plane)
                        .append(", type = ").append(entry.shape).append(", rotation = ").append(entry.rotation)
                    if (entry.remove) {
                        append(", remove = true")
                    }
                    appendLine(" },")
                }
                appendLine("]")
            },
        )
    }

    private fun readNpcEntries(): List<NpcEntry> {
        if (!npcFile.exists()) {
            return emptyList()
        }
        val entries = mutableListOf<NpcEntry>()
        Config.fileReader(npcFile.path, 150) {
            while (nextPair()) {
                require(key() == "spawns")
                while (nextElement()) {
                    var id = ""
                    var x = 0
                    var y = 0
                    var plane = 0
                    var remove = false
                    while (nextEntry()) {
                        when (key()) {
                            "id" -> id = string()
                            "x" -> x = int()
                            "y" -> y = int()
                            "level" -> plane = int()
                            "remove" -> remove = boolean()
                            else -> throw IllegalArgumentException("Unexpected key '${key()}' ${exception()}")
                        }
                    }
                    if (id.isNotBlank()) {
                        entries += NpcEntry(id, x, y, plane, remove)
                    }
                }
            }
        }
        return entries
    }

    private fun writeNpcSpawns(entries: List<NpcEntry>) {
        npcFile.parentFile.mkdirs()
        npcFile.writeText(
            buildString {
                appendLine("# Auto-generated by scene editor. Do not hand-edit.")
                appendLine("spawns = [")
                for (entry in entries.sortedWith(compareBy({ it.x }, { it.y }, { it.plane }, { it.id }, { it.remove }))) {
                    append("    { id = \"").append(entry.id).append("\", x = ").append(entry.x)
                        .append(", y = ").append(entry.y).append(", level = ").append(entry.plane)
                    if (entry.remove) {
                        append(", remove = true")
                    }
                    appendLine(" },")
                }
                appendLine("]")
            },
        )
    }


    private val npcFile: File
        get() = File(Settings["storage.data"], "area/scene/editor.npc-spawns.toml")

    private val file: File
        get() = File(Settings["storage.data"], "area/scene/editor.obj-spawns.toml")
}

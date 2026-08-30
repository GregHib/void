package content.entity.player.command

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.ObjectShape

/**
 * Admin hooks used by the void-client scene editor (`ed save`).
 *
 * - `scene_place <id> <x> <y> <plane> [rot] [shape]` — live GameObjects add
 * - `scene_remove <id> <x> <y> <plane> [rot] [shape]` — live remove + clear collision
 * - `scene_flush` — write obj-spawns.toml + JS5 `lX_Y` (adds + deletes) + drop map cache
 */
class SceneEditorCommands : Script {

    init {
        adminCommand(
            "scene_place",
            intArg("object-id"),
            intArg("x"),
            intArg("y"),
            intArg("plane"),
            intArg("rotation", optional = true),
            intArg("shape", optional = true),
            desc = "Place a scene-editor object into the live world (and queue for flush)",
            handler = ::place,
        )
        adminCommand(
            "scene_remove",
            intArg("object-id"),
            intArg("x"),
            intArg("y"),
            intArg("plane"),
            intArg("rotation", optional = true),
            intArg("shape", optional = true),
            desc = "Remove a world object and clear its collision (queue for JS5 flush)",
            handler = ::remove,
        )
        adminCommand(
            "scene_flush",
            desc = "Persist queued scene objects to obj-spawns.toml + JS5 map archives",
            handler = ::flush,
        )
        adminCommand(
            "scene_status",
            desc = "Show queued scene-editor placements / removals",
            handler = ::status,
        )
    }

    fun place(player: Player, args: List<String>) {
        val id = args[0].toInt()
        val x = args[1].toInt()
        val y = args[2].toInt()
        val plane = args[3].toInt()
        val rotation = args.getOrNull(4)?.toIntOrNull() ?: 0
        val shape = args.getOrNull(5)?.toIntOrNull() ?: ObjectShape.CENTRE_PIECE_STRAIGHT
        val result = SceneEditorPersist.place(id, x, y, plane, rotation, shape)
        player.message(result, ChatType.Console)
    }

    fun remove(player: Player, args: List<String>) {
        val id = args[0].toInt()
        val x = args[1].toInt()
        val y = args[2].toInt()
        val plane = args[3].toInt()
        val rotation = args.getOrNull(4)?.toIntOrNull() ?: 0
        val shape = args.getOrNull(5)?.toIntOrNull() ?: ObjectShape.CENTRE_PIECE_STRAIGHT
        val result = SceneEditorPersist.remove(id, x, y, plane, rotation, shape)
        player.message(result, ChatType.Console)
    }

    fun flush(player: Player, args: List<String>) {
        try {
            player.message(SceneEditorPersist.flush(), ChatType.Console)
        } catch (t: Throwable) {
            player.message("scene_flush failed: ${t.message}", ChatType.Console)
            t.printStackTrace()
        }
    }

    fun status(player: Player, args: List<String>) {
        player.message(SceneEditorPersist.status(), ChatType.Console)
    }
}

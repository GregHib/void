package content.entity.player.command.admin

import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.arg
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class ObjectCommands {

    val objects: GameObjects by inject()

    init {
        adminCommand(
            "obj",
            arg<String>("object-id"),
            arg<Int>("object-shape", optional = true),
            arg<Int>("object-rotation", optional = true),
            arg<Int>("ticks", optional = true),
            desc = "Spawn an object",
            handler = ::spawn
        )

    }

    fun spawn(player: Player, args: List<String>) {
        val id = args[0]
        val shape = args.getOrNull(1)?.toIntOrNull() ?: 10
        val rotation = args.getOrNull(2)?.toIntOrNull() ?: 0
        val ticks = args.getOrNull(3)?.toIntOrNull() ?: -1
        objects.add(id, player.tile, shape, rotation, ticks)
    }
}

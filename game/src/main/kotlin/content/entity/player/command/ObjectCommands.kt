package content.entity.player.command

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects

class ObjectCommands : Script {

    init {
        adminCommand(
            "obj",
            stringArg("id"),
            intArg("shape", optional = true),
            intArg("rotation", optional = true),
            intArg("ticks", optional = true),
            desc = "Spawn an object",
            handler = ::spawn,
        )
    }

    fun spawn(player: Player, args: List<String>) {
        val id = args[0]
        val shape = args.getOrNull(1)?.toIntOrNull() ?: 10
        val rotation = args.getOrNull(2)?.toIntOrNull() ?: 0
        val ticks = args.getOrNull(3)?.toIntOrNull() ?: -1
        GameObjects.add(id, player.tile, shape, rotation, ticks)
    }
}

package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.Log
import world.gregs.voidps.network.client.instruction.ExecuteCommand

class ExecuteCommandHandler : InstructionHandler<ExecuteCommand>() {

    override fun validate(player: Player, instruction: ExecuteCommand) {
        if (instruction.tab) {
            Commands.autofill(player, instruction.command)
            return
        }
        val parts = instruction.command.split(" ")
        val prefix = parts[0]
        val content = instruction.command.removePrefix(prefix).trim()
        if (instruction.automatic) {
            val params = content.split(",")
            val level = params[0].toInt()
            val x = params[1].toInt() shl 6 or params[3].toInt()
            val y = params[2].toInt() shl 6 or params[4].toInt()
            player.tele(x, y, level)
            player["world_map_centre"] = player.tile.id
            player["world_map_marker_player"] = player.tile.id
            return
        }
        Events.events.launch {
            Log.event(player, "command", "\"${instruction.command}\"")
            Commands.call(player, instruction.command)
        }
    }
}

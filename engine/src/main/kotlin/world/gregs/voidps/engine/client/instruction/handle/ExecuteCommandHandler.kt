package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.Commands
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.hasRights
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.network.client.instruction.ExecuteCommand

class ExecuteCommandHandler : InstructionHandler<ExecuteCommand>() {

    override fun validate(player: Player, instruction: ExecuteCommand): Boolean {
        if (instruction.tab) {
            Commands.autofill(player, instruction.command)
            return true
        }
        val parts = instruction.command.split(" ")
        val prefix = parts[0]
        val content = instruction.command.removePrefix(prefix).trim()
        if (instruction.automatic && player.hasRights(PlayerRights.Admin)) {
            val params = content.split(",").map { it.trim().toIntOrNull() }
            if (params.size < 5 || params.any { it == null }) {
                return false
            }
            val (level, regionX, regionY, localX, localY) = params
            if (localX!! !in 0..63 || localY!! !in 0..63) {
                return false
            }
            val x = regionX!! shl 6 or localX
            val y = regionY!! shl 6 or localY
            player.tele(x, y, level!!.coerceIn(0, 3))
            player["world_map_centre"] = player.tile.id
            player["world_map_marker_player"] = player.tile.id
            return true
        }
        Script.launch {
            AuditLog.event(player, "command", "\"${instruction.command}\"")
            Commands.call(player, instruction.command)
        }
        return true
    }
}

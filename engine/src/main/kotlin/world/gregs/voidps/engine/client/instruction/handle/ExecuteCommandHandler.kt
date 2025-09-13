package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.Command.Companion.adminHandlers
import world.gregs.voidps.engine.client.ui.event.Command.Companion.modHandlers
import world.gregs.voidps.engine.client.ui.event.Commands
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.isMod
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.client.instruction.ExecuteCommand

class ExecuteCommandHandler : InstructionHandler<ExecuteCommand>() {

    private val logger = InlineLogger()

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
            Commands.call(player, instruction.command)
        }
        val handler = if (player.isAdmin()) {
            adminHandlers[prefix]
        } else if (player.isMod()) {
            modHandlers[prefix]
        } else {
            return
        }
        if (handler != null) {
            Events.events.launch {
                try {
                    handler.invoke(Command(player, prefix, content), player)
                } catch (exception: Exception) {
                    logger.warn(exception) { "An error occurred while executing command." }
                }
            }
        }
    }
}

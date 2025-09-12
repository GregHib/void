package world.gregs.voidps.engine.client.instruction.handle

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.Command.Companion.adminHandlers
import world.gregs.voidps.engine.client.ui.event.Command.Companion.modHandlers
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.entity.character.player.isMod
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.client.instruction.ExecuteCommand

class ExecuteCommandHandler : InstructionHandler<ExecuteCommand>() {

    private val logger = InlineLogger()

    override fun validate(player: Player, instruction: ExecuteCommand) {
        if (instruction.tab) {
            player.message("${instruction.prefix} ${instruction.content}", ChatType.ConsoleSet)
            return
        }
        val handler = if (player.isAdmin()) {
            adminHandlers[instruction.prefix]
        } else if (player.isMod()) {
            modHandlers[instruction.prefix]
        } else {
            return
        }
        if (handler != null) {
            Events.events.launch {
                try {
                    handler.invoke(Command(player, instruction.prefix, instruction.content), player)
                } catch (exception: Exception) {
                    logger.warn(exception) { "An error occurred while executing command." }
                }
            }
        }
    }
}

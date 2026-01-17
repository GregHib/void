package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Players

class InstructionTask(
    private val handlers: InstructionHandlers,
) : Runnable {

    private val logger = InlineLogger()

    override fun run() {
        for (player in Players) {
            for (i in 0 until MAX_INSTRUCTIONS) {
                val instruction = player.instructions.tryReceive().getOrNull() ?: break
                if (player["debug", false]) {
                    logger.debug { "${player.accountName} ${player.tile} - $instruction" }
                }
                try {
                    handlers.handle(player, instruction)
                } catch (e: Throwable) {
                    logger.error(e) { "Error in instruction $instruction" }
                }
            }
        }
    }

    companion object {
        const val MAX_INSTRUCTIONS = 20
    }
}

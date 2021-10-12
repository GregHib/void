package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.player.Players

class InstructionTask(
    private val players: Players
) : Runnable {

    private val logger = InlineLogger()
    private val handlers = InstructionHandlers()

    override fun run() {
        players.forEach { player ->
            val instructions = player.instructions
            for (instruction in instructions.replayCache) {
                logger.debug { "${player.name} ${player.tile} - $instruction" }
                try {
                    handlers.handle(player, instruction)
                } catch (e: Throwable) {
                    logger.error(e) { "Error in instruction $instruction" }
                }
            }
            instructions.resetReplayCache()
        }
    }
}
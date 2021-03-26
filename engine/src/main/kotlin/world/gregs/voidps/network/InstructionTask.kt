package world.gregs.voidps.network

import world.gregs.voidps.engine.entity.character.player.Players

class InstructionTask(
    private val players: Players,
    private val handler: InstructionHandler
) : Runnable {

    override fun run() {
        players.forEach { player ->
            val instructions = player.instructions
            for (instruction in instructions.replayCache) {
                handler.handle(player, instruction)
            }
            instructions.resetReplayCache()
        }
    }
}
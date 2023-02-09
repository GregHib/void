package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.target.TileTargetStrategy
import world.gregs.voidps.network.instruct.Walk

class WalkHandler : InstructionHandler<Walk>() {

    override fun validate(player: Player, instruction: Walk) {
        player.closeDialogue()
        player.clearAnimation()
        player.queue.clearWeak()
        player.suspension = null
        player.mode = Movement(player, TileTargetStrategy(player.tile.copy(instruction.x, instruction.y)))
    }

}
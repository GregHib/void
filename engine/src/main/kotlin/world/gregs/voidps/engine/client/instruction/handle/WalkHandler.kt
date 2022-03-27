package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.Walk

class WalkHandler : InstructionHandler<Walk>() {

    override fun validate(player: Player, instruction: Walk) {
        player.walkTo(
            target = player.tile.copy(instruction.x, instruction.y),
            cancelAction = player.action.type != ActionType.Making
        )
    }

}
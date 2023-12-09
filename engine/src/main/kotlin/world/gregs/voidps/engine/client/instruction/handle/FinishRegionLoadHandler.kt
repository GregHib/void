package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.FinishRegionLoad

class FinishRegionLoadHandler : InstructionHandler<FinishRegionLoad>() {

    override fun validate(player: Player, instruction: FinishRegionLoad) {
        if (player["debug", false]) {
            println("Finished region load. $player ${player.viewport}")
        }
        player.viewport?.loaded = true
    }

}
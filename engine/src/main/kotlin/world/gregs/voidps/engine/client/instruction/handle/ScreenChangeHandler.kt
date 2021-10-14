package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.isOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.setDisplayMode
import world.gregs.voidps.engine.sync
import world.gregs.voidps.network.instruct.ChangeDisplayMode

class ScreenChangeHandler : InstructionHandler<ChangeDisplayMode>() {

    override fun validate(player: Player, instruction: ChangeDisplayMode) = sync {
        if (player.gameFrame.displayMode == instruction.displayMode || !player.isOpen("graphics_options")) {
            return@sync
        }
        player.setDisplayMode(instruction.displayMode)
    }

}
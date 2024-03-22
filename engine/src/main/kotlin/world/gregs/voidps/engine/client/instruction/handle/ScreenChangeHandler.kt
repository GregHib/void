package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.ChangeDisplayMode

class ScreenChangeHandler : InstructionHandler<ChangeDisplayMode>() {

    override fun validate(player: Player, instruction: ChangeDisplayMode) {
        if (player.interfaces.displayMode == instruction.displayMode || !player.hasOpen("graphics_options")) {
            return
        }
        player.interfaces.setDisplayMode(instruction.displayMode)
    }

}
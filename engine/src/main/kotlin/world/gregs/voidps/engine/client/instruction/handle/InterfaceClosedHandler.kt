package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.instruct.CloseInterface

class InterfaceClosedHandler : InstructionHandler<CloseInterface>() {

    override fun validate(player: Player, instruction: CloseInterface) {
        val id = player.interfaces.get("main_screen") ?: player.interfaces.get("underlay")
        if (id != null) {
            player.interfaces.close(id)
        }
    }

}
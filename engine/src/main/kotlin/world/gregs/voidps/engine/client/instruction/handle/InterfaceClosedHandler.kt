package world.gregs.voidps.engine.client.instruction.handle

import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.instruction.InterfaceClosedInstruction

class InterfaceClosedHandler : InstructionHandler<InterfaceClosedInstruction>() {

    override fun validate(player: Player, instruction: InterfaceClosedInstruction) {
        val id = player.interfaces.get("main_screen") ?: player.interfaces.get("wide_screen") ?: player.interfaces.get("underlay")
        if (id != null) {
            player.interfaces.close(id)
        }
    }
}

package world.gregs.voidps.engine.client.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler
import world.gregs.voidps.network.instruct.CloseInterface

class InterfaceClosedHandler : Handler<CloseInterface>() {

    override fun validate(player: Player, instruction: CloseInterface) {
        val id = player.interfaces.get("main_screen") ?: player.interfaces.get("underlay")
        if (id != null) {
            player.interfaces.close(id)
        }
    }

}
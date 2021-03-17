package world.gregs.voidps.network.handle

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Handler

/**
 * @author GregHib <greg@gregs.world>
 * @since July 26, 2020
 */
class InterfaceClosedHandler : Handler() {

    override fun interfaceClosed(player: Player) {
        val id = player.interfaces.get("main_screen") ?: player.interfaces.get("underlay")
        if (id != null) {
            player.interfaces.close(id)
        }
    }

}
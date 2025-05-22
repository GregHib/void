package content.entity.obj.ship

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

object CharterShip {
    fun open(player: Player, location: String) {
        player.open("charter_ship_map")
        player.interfaces.sendVisibility("charter_ship_map", location, false)
    }
}
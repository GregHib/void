package content.entity.obj.ship

import world.gregs.voidps.engine.entity.character.jingle
import content.quest.startCutscene
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

suspend fun Player.boatTravel(journey: String, delay: Int, destination: Tile) {
    val cutscene = startCutscene("ship_travel")
    cutscene.onEnd {
        tele(destination)
    }
    tele(cutscene.instance.tile, clearInterfaces = false)
    sendScript("clear_ships")
    jingle("sailing_journey")
    open("journey_ship")
    set("ships_set_destination", journey)
    delay(delay)
    cutscene.end()
}

package content.entity.obj.ship

import content.entity.sound.jingle
import content.quest.startCutscene
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Tile

suspend fun SuspendableContext<Player>.boatTravel(journey: String, delay: Int, destination: Tile) {
    val cutscene = startCutscene("ship_travel")
    cutscene.onEnd {
        player.tele(destination)
    }
    player.tele(cutscene.instance.tile, clearInterfaces = false)
    player.sendScript("clear_ships")
    player.jingle("sailing_journey")
    player.open("journey_ship")
    player["ships_set_destination"] = journey
    delay(delay)
    cutscene.end(this)
}
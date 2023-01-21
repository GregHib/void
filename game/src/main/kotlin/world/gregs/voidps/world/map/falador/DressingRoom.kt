package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.suspend.delay

internal suspend fun Interaction.openDressingRoom(id: String) {
    player["dressing_room"] = true
    player.dialogues.clear()
    delay(1)
    player.setGraphic("dressing_room_start")
    delay(1)
    player.open(id)
    while (suspended) {
        player.setGraphic("dressing_room")
        delay(1)
    }
}
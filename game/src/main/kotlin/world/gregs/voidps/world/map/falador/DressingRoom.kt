package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.interact
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.suspend.delay

internal suspend fun Interaction.openDressingRoom(id: String) {
    player.dialogues.clear()
    player.interact.onStop = {
        player.start("delay", 1)
        player.close(id)
        player.setGraphic("dressing_room_finish")
        player.flagAppearance()
    }
    delay(1)
    player.setGraphic("dressing_room_start")
    delay(1)
    player.open(id)
    while (suspended) {
        player.setGraphic("dressing_room")
        delay(1)
    }
}
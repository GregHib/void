package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.pause

internal suspend fun SuspendableContext<Player>.openDressingRoom(id: String) {
    player.closeDialogue()
    pause(1)
    player.setGraphic("dressing_room_start")
    pause(1)
    player.open(id)
    player.softTimers.start("dressing_room")
}
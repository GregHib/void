package content.area.asgarnia.falador

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player

internal suspend fun Interaction<Player>.openDressingRoom(id: String) {
    player.closeDialogue()
    delay(1)
    player.gfx("dressing_room_start")
    delay(1)
    player.open(id)
    player.softTimers.start("dressing_room")
}

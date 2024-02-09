package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.entity.character.player.Player

interfaceOpen("notes") { player: Player ->
    player.interfaceOptions.unlockAll(id, "notes", 0..30)
}
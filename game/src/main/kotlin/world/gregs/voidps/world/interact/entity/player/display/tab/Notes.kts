package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.event.interfaceOpen

interfaceOpen("notes") { player ->
    player.interfaceOptions.unlockAll(id, "notes", 0..30)
}
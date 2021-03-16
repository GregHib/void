package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where

InterfaceOption where { name == "notes" } then {
    player.interfaceOptions.unlockAll(name, "notes", 0..30)
}
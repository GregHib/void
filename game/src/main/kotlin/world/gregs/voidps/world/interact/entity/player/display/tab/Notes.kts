package world.gregs.voidps.world.interact.entity.player.display.tab

import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "notes" } then {
    player.interfaceOptions.unlockAll(name, "notes", 0..30)
}
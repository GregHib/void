package world.gregs.void.world.interact.entity.player.display.tab

import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "notes" } then {
    player.interfaceOptions.unlockAll(name, "notes", 0..30)
}
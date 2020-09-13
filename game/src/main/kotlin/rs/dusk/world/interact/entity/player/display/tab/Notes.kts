package rs.dusk.world.interact.entity.player.display.tab

import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "notes" } then {
    player.interfaceOptions.unlockAll(name, "notes", 0..30)
}
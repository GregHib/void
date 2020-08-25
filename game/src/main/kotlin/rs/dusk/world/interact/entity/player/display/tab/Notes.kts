package rs.dusk.world.interact.entity.player.display.tab

import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "notes" } then {
    player.interfaces.sendSettings(id, 9, 0, 30, 0, 1, 2, 3, 18, 20)
}
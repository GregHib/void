package rs.dusk.world.interact.player.display.tab

import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.player.display.InterfaceInteraction

InterfaceInteraction where { name == "notes" } then {
    player.interfaces.sendSettings(id, 9, 0, 30, 2621470)
}
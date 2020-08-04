package rs.dusk.world.interact.entity.player.display.tab

import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceInteraction

InterfaceInteraction where { name == "notes" } then {
    player.interfaces.sendSetting(id, 9, 0, 30, 2621470)
}
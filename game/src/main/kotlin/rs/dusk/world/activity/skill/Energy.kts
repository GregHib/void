package rs.dusk.world.activity.skill

import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "energy_orb" && option == "Turn Run mode on"} then {
    player.movement.running = !player.movement.running
}
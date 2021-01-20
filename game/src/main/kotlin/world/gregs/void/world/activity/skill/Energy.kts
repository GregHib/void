package world.gregs.void.world.activity.skill

import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "energy_orb" && option == "Turn Run mode on"} then {
    player.movement.running = !player.movement.running
}
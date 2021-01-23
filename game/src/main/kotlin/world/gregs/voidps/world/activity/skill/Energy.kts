package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

InterfaceOption where { name == "energy_orb" && option == "Turn Run mode on"} then {
    player.movement.running = !player.movement.running
}
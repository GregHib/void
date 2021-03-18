package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where

InterfaceOption where { name == "energy_orb" && option == "Turn Run mode on"} then {
    player.movement.running = !player.movement.running
}
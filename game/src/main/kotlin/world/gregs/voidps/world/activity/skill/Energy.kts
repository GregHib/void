package world.gregs.voidps.world.activity.skill

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ name == "energy_orb" && option == "Turn Run mode on" }) { player: Player ->
    player.movement.running = !player.movement.running
}
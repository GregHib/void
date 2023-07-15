package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendRunEnergy
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on

on<InterfaceOpened>({ id == "energy_orb" }) { player: Player ->
    player.sendRunEnergy(player.energyPercent())
}

on<Registered> { player: Player ->
    player.sendVariable("movement")
}

on<InterfaceOption>({ id == "energy_orb" && option == "Turn Run mode on" }) { player: Player ->
    if (player.mode is Rest) {
        val walking = player["movement", "walk"] == "walk"
        toggleRun(player, !walking)
        player["movement_temp"] = if (walking) "run" else "walk"
        player.mode = EmptyMode
        return@on
    }
    toggleRun(player, player.running)
}

fun toggleRun(player: Player, run: Boolean) {
    val energy = player.energyPercent()
    if (energy == 0) {
        player.message("You don't have enough energy left to run!", ChatType.Filter)
    }
    player.running = !run && energy > 0
}
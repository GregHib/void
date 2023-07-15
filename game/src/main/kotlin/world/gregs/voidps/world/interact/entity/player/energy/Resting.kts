package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.arriveDelay

on<InterfaceOption>({ id == "energy_orb" && option == "Rest" }) { player: Player ->
    if (player["movement", "walk"] == "rest") {
        player.message("You are already resting.")
    } else {
        player.mode = Rest(player, -1)
    }
}

on<NPCOption>({ operate && def["song", -1] != -1 && option == "Listen-to" }) { player: Player ->
    arriveDelay()
    player.mode = Rest(player, def["song"])
}

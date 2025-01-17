package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.npc.npcOperate

interfaceOption("Rest", id = "energy_orb") {
    if (player["movement", "walk"] == "rest") {
        player.message("You are already resting.")
    } else {
        player.mode = Rest(player, -1)
    }
}

npcOperate("Listen-to") {
    arriveDelay()
    if (def["song", -1] != -1) {
        player.mode = Rest(player, def["song"])
    }
}

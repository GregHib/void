package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.suspend.arriveDelay

interfaceOption("energy_orb", option = "Rest") {
    if (player["movement", "walk"] == "rest") {
        player.message("You are already resting.")
    } else {
        player.mode = Rest(player, -1)
    }
}

npcOperate("Listen-to") {
    if (def["song", -1] != -1) {
        arriveDelay()
        player.mode = Rest(player, def["song"])
    }
}

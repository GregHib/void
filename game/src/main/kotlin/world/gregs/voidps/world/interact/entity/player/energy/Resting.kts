package world.gregs.voidps.world.interact.entity.player.energy

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.arriveDelay

interfaceOption({ id == "energy_orb" && option == "Rest" }) { player: Player ->
    if (player["movement", "walk"] == "rest") {
        player.message("You are already resting.")
    } else {
        player.mode = Rest(player, -1)
    }
}

npcOperate({ def["song", -1] != -1 && option == "Listen-to" }) { player: Player ->
    arriveDelay()
    player.mode = Rest(player, def["song"])
}

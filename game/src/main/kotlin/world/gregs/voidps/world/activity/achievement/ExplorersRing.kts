package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.interact.entity.player.energy.MAX_RUN_ENERGY
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption
import java.util.concurrent.TimeUnit

playerSpawn { player ->
    val lastUse = player["explorers_ring_last_use", -1L]
    if (lastUse != -1L && lastUse != TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())) {
        player["explorers_ring_charges"] = 1
    }
}

inventoryOption("Run-replenish", "explorers_ring_*") {
    if (player.inventory.discharge(player, slot)) {
        player.setAnimation("run_replenish")
        player.setGraphic("run_replenish")
        player.runEnergy += MAX_RUN_ENERGY / 2
        player["explorers_ring_last_use"] = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        player.message("You feel refreshed as the ring revitalises you and a charge is used up.")
    }
}
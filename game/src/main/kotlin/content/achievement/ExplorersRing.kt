package content.achievement

import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.inventory
import java.util.concurrent.TimeUnit

class ExplorersRing : Script {

    init {
        playerSpawn {
            val lastUse: Long = this["explorers_ring_last_use"] ?: return@playerSpawn
            if (lastUse != -1L && lastUse != TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())) {
                this["explorers_ring_charges"] = 1
            }
        }

        itemOption("Run-replenish", "explorers_ring_*") {
            if (inventory.discharge(this, it.slot)) {
                anim("run_replenish")
                gfx("run_replenish")
                runEnergy += MAX_RUN_ENERGY / 2
                set("explorers_ring_last_use", TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()))
                message("You feel refreshed as the ring revitalises you and a charge is used up.")
            }
        }
    }
}

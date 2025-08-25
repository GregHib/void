@file:Suppress("UnusedReceiverParameter")

package content.achievement

import content.entity.player.inv.InventoryOption
import world.gregs.voidps.engine.entity.character.player.Player
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.event.handle.Option
import world.gregs.voidps.engine.event.handle.Spawn
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.inventory
import java.util.concurrent.TimeUnit

@Spawn
fun replenishExplorersRing(player: Player) {
    val lastUse = player["explorers_ring_last_use", -1L]
    if (lastUse != -1L && lastUse != TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())) {
        player["explorers_ring_charges"] = 1
    }
}

@Option("Run-replenish", "explorers_ring_*")
fun InventoryOption.replenishExplorersRing() {
    if (player.inventory.discharge(player, slot)) {
        player.anim("run_replenish")
        player.gfx("run_replenish")
        player.runEnergy += MAX_RUN_ENERGY / 2
        player["explorers_ring_last_use"] = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        player.message("You feel refreshed as the ring revitalises you and a charge is used up.")
    }
}
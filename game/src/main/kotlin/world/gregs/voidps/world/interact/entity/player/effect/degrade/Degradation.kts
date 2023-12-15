package world.gregs.voidps.world.interact.entity.player.effect.degrade

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged

// Switch two tracked items
on<ItemChanged>({ containsCharge(it, inventory, index, from, fromIndex) }) { player: Player ->
    val original = Degrade.variable(from, fromIndex)
    val originalCharge: Int? = player.getOrNull(original)
    val update = Degrade.variable(inventory, index)
    val updateCharge: Int? = player.getOrNull(update)
    player["${original}_${update}"] = true
    if (originalCharge != null) {
        player[update] = originalCharge
    } else {
        player.clear(update)
    }
    if (updateCharge != null) {
        player[original] = updateCharge
    } else {
        player.clear(original)
    }
}

fun containsCharge(player: Player, inventory: String, index: Int, from: String, fromIndex: Int): Boolean {
    val original = Degrade.variable(from, fromIndex)
    val update = Degrade.variable(inventory, index)
    if (player.remove<Boolean>("${update}_$original") != null) {
        // Don't switch back if already switched once
        return false
    }
    return player.contains(update) || player.contains(original)
}
package world.gregs.voidps.world.interact.entity.player.effect.degrade

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.itemChange

// Switch two tracked items
itemChange { player ->
    if (containsCharge(player, inventory, index, from, fromIndex)) {
        val original = Degrade.variable(from, fromIndex)
        val originalCharge: Int? = player[original]
        val update = Degrade.variable(inventory, index)
        val updateCharge: Int? = player[update]
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
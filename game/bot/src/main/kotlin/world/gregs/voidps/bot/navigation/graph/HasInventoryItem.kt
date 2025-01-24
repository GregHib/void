package world.gregs.voidps.bot.navigation.graph

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

class HasInventoryItem(val id: String, val amount: Int) : Condition {
    override fun has(player: Player): Boolean {
        return player.inventory.contains(id, amount)
    }
}
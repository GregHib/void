package content.bot.interact.navigation.graph

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

class HasInventoryItem(val id: String, val amount: Int) : Condition {
    override fun has(player: Player): Boolean = player.inventory.contains(id, amount)
}

package world.gregs.voidps.engine.map.nav

import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player

class HasInventoryItem(val id: String, val amount: Int) : Condition {
    override fun has(player: Player): Boolean {
        return player.inventory.contains(id, amount)
    }
}
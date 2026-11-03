package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.inv.inventory

/** The bot carries any item matching [id] (a wildcard such as `super_restore_*`: any dose counts). */
data class BotCarrying(val id: String) : Condition(1) {
    override fun keys() = setOf("item:$id")
    override fun events() = setOf("inv:inventory")
    override fun check(player: Player): Boolean {
        val inventory = player.inventory
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isNotEmpty() && wildcardEquals(id, item.id)) {
                return true
            }
        }
        return false
    }
}

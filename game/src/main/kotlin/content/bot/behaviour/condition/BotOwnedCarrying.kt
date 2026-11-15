package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

/** The bot carries at least [atLeast] of [id]: a pick-up order is done once the count went up. */
data class BotOwnedCarrying(val id: String, val atLeast: Int) : Condition(1) {
    override fun keys() = setOf("item:$id")
    override fun events() = setOf("inv:inventory")
    override fun check(player: Player) = player.inventory.count(id) >= atLeast
}

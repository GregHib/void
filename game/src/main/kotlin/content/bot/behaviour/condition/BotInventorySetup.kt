package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

data class BotInventorySetup(val items: List<BotItem>) : Condition(100) {
    override fun keys() = items.flatMap { entry -> entry.ids.map { "item:$it" } }.toSet()
    override fun events() = setOf("inv:inventory")
    override fun check(player: Player) = contains(player, player.inventory, items)
}

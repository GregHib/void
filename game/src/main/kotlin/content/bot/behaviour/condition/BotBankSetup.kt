package content.bot.behaviour.condition

import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player

data class BotBankSetup(val items: List<BotItem>) : Condition(80) {
    override fun keys() = items.flatMap { entry -> entry.ids.map { "bank:$it" } }.toSet()
    override fun events() = setOf("inv:bank")
    override fun check(player: Player) = contains(player, player.bank, items)
}
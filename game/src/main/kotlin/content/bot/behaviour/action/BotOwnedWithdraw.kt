package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.Condition
import content.bot.owned.OwnedBank
import content.bot.owned.OwnedResolvers
import content.bot.owned.owner
import content.entity.player.bank.bank
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.inv.inventory

/**
 * An owned bot takes [items] from its owner's bank (its own bank instance is the owner's) by direct
 * transaction, standing beside a bank. Entries already satisfied by the inventory are skipped.
 */
data class BotOwnedWithdraw(val items: List<BotItem>) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val owner = player.owner ?: return BehaviourState.Failed(Reason.Invalid("Owner is offline."))
        if (BotOpenBank().nearby(player) == null) {
            return BehaviourState.Failed(Reason.Invalid("No bank nearby to withdraw from."))
        }
        for (entry in items) {
            if (entry.ids.contains("empty") || Condition.contains(player, player.inventory, listOf(entry))) {
                continue
            }
            val item = player.bank.items.firstOrNull { OwnedResolvers.valid(player, it, entry) }
                ?: return BehaviourState.Failed(Reason.Invalid("Needs ${entry.ids.first().replace('_', ' ')} in the bank."))
            val wanted = (entry.min ?: 1) - player.inventory.count(item.id)
            if (wanted <= 0) {
                continue
            }
            val moved = OwnedBank.withdraw(owner, player, item.id, wanted)
            if (moved <= 0) {
                return BehaviourState.Failed(Reason.Invalid("Couldn't take ${item.id.replace('_', ' ')} from the bank."))
            }
        }
        player.closeInterfaces()
        return BehaviourState.Success
    }
}

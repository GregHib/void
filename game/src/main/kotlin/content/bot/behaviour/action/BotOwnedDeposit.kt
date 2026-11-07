package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.owned.OwnedBank
import content.bot.owned.owner
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.inv.inventory

/**
 * An owned bot empties its inventory into its owner's bank by direct transaction, standing beside a bank.
 * Never opens the bank interface: the bank is the owner's and its tab counters are the owner's variables.
 */
class BotOwnedDeposit : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val owner = player.owner ?: return BehaviourState.Failed(Reason.Invalid("Owner is offline."))
        if (BotOpenBank().nearby(player) == null) {
            return BehaviourState.Failed(Reason.Invalid("No bank nearby to deposit at."))
        }
        val inventory = player.inventory
        OwnedBank.depositAll(owner, inventory)
        if (!inventory.isEmpty()) {
            return BehaviourState.Failed(Reason.Invalid("Your bank is too full for the bot to deposit."))
        }
        player.closeInterfaces()
        return BehaviourState.Success
    }
}

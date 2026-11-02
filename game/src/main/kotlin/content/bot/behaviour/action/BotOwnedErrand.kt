package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.activity.BotActivity
import content.bot.owned.BankOrder
import content.bot.owned.OwnedBank
import content.bot.owned.owner
import content.entity.player.bank.BankDeposit
import content.entity.player.equip.Equipping
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * At the booth (the bank screen is open on the bot), an owned bot carries out every [BankOrder] queued on it in
 * `owned_errand`, tells the owner what it managed, closes the bank and goes back to what it was doing
 * (`owned_resume`, the activity it was pinned to before the errand).
 */
class BotOwnedErrand : BotAction {
    @Suppress("UNCHECKED_CAST")
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val owner = player.owner ?: return BehaviourState.Failed(Reason.Invalid("Owner is offline."))
        if (player.menu != "bank") {
            return BehaviourState.Failed(Reason.Invalid("The bank isn't open."))
        }
        val orders = player.get<MutableList<BankOrder>>("owned_errand") ?: mutableListOf()
        val done = mutableListOf<String>()
        val missed = mutableListOf<String>()
        for (order in orders.toList()) {
            if (carryOut(owner, player, order)) done.add(order.describe()) else missed.add(order.describe())
        }
        orders.clear()
        player.clear("owned_errand")
        player.closeInterfaces()
        val slot = player["owned_slot", 0] + 1
        if (done.isNotEmpty()) {
            owner.message("[Bot $slot] ${player.name}: ${done.joinToString()}.")
        }
        if (missed.isNotEmpty()) {
            owner.message("[Bot $slot] ${player.name} couldn't ${missed.joinToString()}.")
        }
        val resume = player.get<BotActivity>("owned_resume")
        if (resume != null) {
            player.clear("owned_resume")
            bot.pin(resume)
        }
        return BehaviourState.Success
    }

    private fun carryOut(owner: Player, player: Player, order: BankOrder): Boolean = when (order.kind) {
        BankOrder.Kind.Withdraw -> OwnedBank.withdraw(owner, player.inventory, order.item, order.amount) > 0
        BankOrder.Kind.Deposit -> {
            val index = player.inventory.indexOf(order.item)
            if (index == -1) {
                false
            } else {
                val before = player.inventory.count(order.item)
                BankDeposit.deposit(owner, player.inventory, player.inventory[index], order.amount, index, check = false)
                player.inventory.count(order.item) < before
            }
        }
        BankOrder.Kind.DepositAll -> {
            OwnedBank.depositAll(owner, player.inventory)
            player.inventory.isEmpty()
        }
        BankOrder.Kind.Equip -> {
            val index = player.inventory.indexOf(order.item)
            index != -1 && player.hasRequirements(player.inventory[index]) && Equipping.equip(player, player.inventory[index], index)
        }
        BankOrder.Kind.TakeOff -> {
            val slot = EquipSlot.entries.firstOrNull { it.name.equals(order.item, ignoreCase = true) }
            val worn = slot?.let { player.equipment[it.index] }
            if (slot == null || worn == null || worn.isEmpty() || !player.equipment.move(slot.index, player.inventory)) {
                false
            } else {
                val index = player.inventory.indexOf(worn.id)
                if (index != -1) {
                    BankDeposit.deposit(owner, player.inventory, player.inventory[index], worn.amount, index, check = false)
                }
                true
            }
        }
    }
}

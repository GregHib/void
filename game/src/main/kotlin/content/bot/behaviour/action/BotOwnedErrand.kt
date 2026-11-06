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
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.get
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
            val result = carryOut(owner, player, order)
            if (result == null) missed.add(order.describe()) else done.add(result.ifEmpty { order.describe() })
        }
        orders.clear()
        player.clear("owned_errand")
        player.closeInterfaces()
        val slot = player["owned_slot", 0] + 1
        if (done.isNotEmpty()) {
            owner.message("[Companion $slot] ${player.name}: ${done.joinToString()}.")
        }
        if (missed.isNotEmpty()) {
            owner.message("[Companion $slot] ${player.name} couldn't ${missed.joinToString()}.")
        }
        val resume = player.get<BotActivity>("owned_resume")
        if (resume != null) {
            player.clear("owned_resume")
            bot.pin(resume)
        }
        return BehaviourState.Success
    }

    companion object {
        /**
         * Does one [order] at an open bank: null when it couldn't be, "" when done as described, else what was done
         * instead (a charged item the bank was out of, replaced by the best other charge of it).
         */
        fun carryOut(owner: Player, player: Player, order: BankOrder): String? = when (order.kind) {
            BankOrder.Kind.Withdraw -> {
                // The bank out of the item, or short of it: the rest from its other charges or doses, best first
                var left = order.amount - OwnedBank.withdraw(owner, player, order.item, order.amount)
                val instead = mutableListOf<String>()
                for (variant in variants(order.item)) {
                    if (left <= 0) {
                        break
                    }
                    val got = OwnedBank.withdraw(owner, player, variant, left)
                    if (got > 0) {
                        left -= got
                        instead.add("${if (got > 1) "$got x " else ""}${variant.replace('_', ' ')}")
                    }
                }
                when {
                    instead.isNotEmpty() -> "withdraw ${instead.joinToString()} in place of ${order.describe().removePrefix("withdraw ")}"
                    left < order.amount -> ""
                    else -> null
                }
            }
            BankOrder.Kind.Equip -> {
                var index = player.inventory.indexOf(order.item)
                var instead = ""
                if (index == -1) {
                    for (variant in variants(order.item)) {
                        index = player.inventory.indexOf(variant)
                        if (index != -1) {
                            instead = " ${variant.replace('_', ' ')} in place of ${order.item.replace('_', ' ')}"
                            break
                        }
                    }
                }
                if (index != -1 && player.hasRequirements(player.inventory[index]) && Equipping.equip(player, player.inventory[index], index)) {
                    if (instead.isEmpty()) "" else "wear$instead"
                } else {
                    null
                }
            }
            BankOrder.Kind.Deposit -> {
                val index = player.inventory.indexOf(order.item)
                if (index == -1) {
                    null
                } else {
                    val before = player.inventory.count(order.item)
                    BankDeposit.deposit(owner, player.inventory, player.inventory[index], order.amount, index, check = false)
                    if (player.inventory.count(order.item) < before) "" else null
                }
            }
            BankOrder.Kind.DepositAll -> {
                OwnedBank.depositAll(owner, player.inventory)
                if (player.inventory.isEmpty()) "" else null
            }
            BankOrder.Kind.TakeOff -> {
                val slot = EquipSlot.entries.firstOrNull { it.name.equals(order.item, ignoreCase = true) }
                val worn = slot?.let { player.equipment[it.index] }
                if (slot == null || worn == null || worn.isEmpty() || !player.equipment.move(slot.index, player.inventory)) {
                    null
                } else {
                    val index = player.inventory.indexOf(worn.id)
                    if (index != -1) {
                        BankDeposit.deposit(owner, player.inventory, player.inventory[index], worn.amount, index, check = false)
                    }
                    ""
                }
            }
        }

        /**
         * The other charges of a charged item ([id] like `amulet_of_glory_4`): the same name with every other
         * number that is an item, highest first, then the uncharged one; nothing for an item without a charge.
         */
        fun variants(id: String): List<String> {
            val charge = id.substringAfterLast('_').toIntOrNull() ?: return emptyList()
            val base = id.substringBeforeLast('_')
            val definitions: ItemDefinitions = get()
            val list = mutableListOf<String>()
            for (other in MAX_CHARGE downTo 1) {
                if (other != charge && definitions.contains("${base}_$other")) {
                    list.add("${base}_$other")
                }
            }
            if (definitions.contains(base)) {
                list.add(base)
            }
            return list
        }

        private const val MAX_CHARGE = 10
    }
}

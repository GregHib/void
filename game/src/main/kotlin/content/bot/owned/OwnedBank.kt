package content.bot.owned

import content.bot.Bot
import content.bot.bot
import content.entity.player.bank.Bank
import content.entity.player.bank.BankDeposit
import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.MoveItemLimit.moveToLimit
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shiftToFreeIndex

/**
 * The one way owned bots move items between their hands and their owner's bank: the same bookkeeping as the
 * owner's own bank screen (tab counters, compaction, noting, unbankables), without the interface.
 */
object OwnedBank {

    /**
     * Moves up to [amount] of [id] from [owner]'s bank into [target]; the emptied bank slot is compacted and its
     * tab counter dropped as a player's withdrawal does. The number moved.
     */
    fun withdraw(owner: Player, target: Inventory, id: String, amount: Int): Int {
        val bank = owner.bank
        val index = bank.indexOf(id)
        if (index == -1 || amount < 1) {
            return 0
        }
        var moved = 0
        var removed = false
        bank.transaction {
            moved = moveToLimit(id, amount, target)
            if (moved <= 0) {
                error = TransactionError.Full()
                return@transaction
            }
            if (inventory[index].isEmpty()) {
                shiftToFreeIndex(index)
                removed = true
            }
        }
        if (bank.transaction.error != TransactionError.None) {
            return 0
        }
        if (removed) {
            Bank.decreaseTab(owner, Bank.getTab(owner, index))
        }
        return moved
    }

    /** Deposits every item of [inventory] into [owner]'s bank; what the bank refuses stays. */
    fun depositAll(owner: Player, inventory: Inventory) {
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isNotEmpty()) {
                BankDeposit.deposit(owner, inventory, item, item.amount, index, check = false)
            }
        }
    }
}

/**
 * The player who owns this bot, while they are online: found by the index the bot was given at summon, checked
 * against the account name, so no player list scan per tick.
 */
val Player.owner: Player?
    get() {
        val account: String = get("owner") ?: return null
        val player = Players.indexed(this["owner_index", -1]) ?: return null
        return if (player.accountName == account) player else null
    }

/** The bot in [owner]'s slot [slot] while it is in the world, by the index recorded at summon. */
fun ownedBot(owner: Player, slot: Int): Bot? {
    val player = Players.indexed(owner["owned_bot_${slot}_index", -1]) ?: return null
    if (!player.contains("bot") || player.get<String>("owner") != owner.accountName || player["owned_slot", -1] != slot) {
        return null
    }
    return player.bot
}

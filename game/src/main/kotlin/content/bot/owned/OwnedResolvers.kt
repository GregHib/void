package content.bot.owned

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.action.BotGoTo
import content.bot.behaviour.action.BotGoToNearest
import content.bot.behaviour.action.BotOpenBank
import content.bot.behaviour.action.BotOwnedDeposit
import content.bot.behaviour.action.BotOwnedEquip
import content.bot.behaviour.action.BotOwnedWithdraw
import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInArea
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.setup.Resolver
import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Setup resolvers for owned bots. They use a booth like a player but move items by direct transaction, never the
 * bank interface's own handlers (the bank is the owner's and its tab counters are owner variables), and never buy
 * from shops: what the owner's bank lacks fails the task with a message to the owner.
 */
object OwnedResolvers {

    val ids = setOf("owned_deposit", "owned_withdraw", "owned_equip", "go_to_area")

    fun resolver(player: Player, condition: Condition): Resolver? = when (condition) {
        is BotInArea -> Resolver("go_to_area", -1, actions = listOf(BotGoTo(condition.id)))
        is BotInventorySetup -> inventory(player, condition.items)
        is BotEquipmentSetup -> equipment(player, condition.items)
        else -> null
    }

    /**
     * What the owner has to supply for [condition], for the failure message. Empty when nothing is missing.
     */
    fun missing(player: Player, condition: Condition): List<String> = when (condition) {
        is BotInventorySetup -> condition.items.filter { !it.ids.contains("empty") && !carried(player, it) && !inBank(player, it) }.map { it.ids.first() }
        is BotEquipmentSetup -> condition.items.values.filter { !it.ids.contains("empty") && !carried(player, it) && !inBank(player, it) }.map { it.ids.first() }
        else -> emptyList()
    }

    private fun inventory(player: Player, items: List<BotItem>): Resolver? {
        val empty = items.firstOrNull { it.ids.contains("empty") }
        val needsSpace = empty?.min != null && player.inventory.spaces < empty.min
        val missing = items.filter { !it.ids.contains("empty") && !Condition.contains(player, player.inventory, listOf(it)) }
        if (!needsSpace && missing.isEmpty()) {
            return null
        }
        for (entry in missing) {
            if (!inBank(player, entry)) {
                return null
            }
        }
        val actions = mutableListOf<BotAction>()
        actions.add(BotGoToNearest("bank"))
        actions.add(BotOpenBank())
        if (needsSpace) {
            actions.add(BotOwnedDeposit())
        }
        // Emptying the inventory banks the tools too: after a deposit everything the setup needs comes back out
        val fetch = if (needsSpace) items.filter { !it.ids.contains("empty") } else missing
        if (fetch.isNotEmpty()) {
            actions.add(BotOwnedWithdraw(fetch))
        }
        return Resolver(if (missing.isEmpty()) "owned_deposit" else "owned_withdraw", weight = 20, actions = actions)
    }

    private fun equipment(player: Player, items: Map<EquipSlot, BotItem>): Resolver? {
        val wrong = items.filter { (slot, entry) ->
            val worn = player.equipped(slot)
            if (entry.ids.contains("empty")) worn.isNotEmpty() else !entry.ids.contains(worn.id)
        }
        if (wrong.isEmpty()) {
            return null
        }
        val fromBank = wrong.filter { (_, entry) -> !entry.ids.contains("empty") && !carried(player, entry) }
        for ((_, entry) in fromBank) {
            if (!inBank(player, entry)) {
                return null
            }
        }
        val actions = mutableListOf<BotAction>()
        if (fromBank.isNotEmpty()) {
            actions.add(BotGoToNearest("bank"))
            actions.add(BotOpenBank())
            actions.add(BotOwnedWithdraw(fromBank.values.map { it.copy(min = 1) }))
        }
        actions.add(BotOwnedEquip(wrong))
        return Resolver("owned_equip", weight = 15, actions = actions)
    }

    fun carried(player: Player, entry: BotItem): Boolean = player.inventory.items.any { valid(player, it, entry) }

    fun inBank(player: Player, entry: BotItem): Boolean = player.bank.items.any { valid(player, it, entry) }

    /**
     * [item] satisfies [entry]: one of its ids, and the bot can use or wear it when the entry asks.
     */
    fun valid(player: Player, item: Item, entry: BotItem): Boolean {
        if (item.isEmpty() || !entry.ids.contains(item.id)) {
            return false
        }
        if (entry.usable && !player.hasRequirementsToUse(item)) {
            return false
        }
        if (entry.equippable && !player.hasRequirements(item)) {
            return false
        }
        return true
    }
}

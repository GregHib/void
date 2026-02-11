package content.bot.behaviour.setup

import content.bot.behaviour.Condition
import content.bot.behaviour.Condition.Entry
import content.bot.behaviour.action.BotAction
import content.entity.npc.shop.stock.Price
import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object DynamicResolvers {

    fun ids() = setOf("withdraw_from_bank", "equip_from_bank", "go_to_area", "buy_from_shop")

    // location, npc
    val shopItems = mutableMapOf<String, MutableList<Pair<String, String>>>()

    fun resolver(player: Player, condition: Condition): Resolver? = when (condition) {
        is Condition.InArea -> Resolver("go_to_area", -1, actions = listOf(BotAction.GoTo(condition.id)))
        is Condition.Equipment -> resolveEquipment(player, condition.items)
        is Condition.Inventory -> resolveInventory(player, condition.items)
        else -> null
    }

    private fun resolveInventory(player: Player, items: List<Entry>): Resolver? {
        var resolver = withdraw(player, items)
        if (resolver != null) {
            return resolver
        }
        resolver = buyItems(player, items)
        if (resolver != null) {
            return resolver
        }
        return null
    }

    private fun buyItems(player: Player, items: List<Entry>): Resolver? {
        for (entry in items) {
            val amount = entry.min ?: 1
            if (entry.ids.any { id -> player.inventory.contains(id, amount) }) {
                continue
            }
            for (id in entry.ids) {
                for ((location, npc) in shopItems[id] ?: continue) {
                    val actions = mutableListOf<BotAction>()
                    val price = Price.of(id)
                    if (!player.ownsItem("coins", price * amount)) {
                        continue
                    }
                    actions.add(BotAction.GoTo(location))
                    actions.add(BotAction.InteractNpc("Trade", npc, success = Condition.InterfaceOpen("shop")))
                    var remaining = amount
                    while (remaining > 0) {
                        val amount = when {
                            remaining >= 500 -> 500
                            remaining >= 50 -> 50
                            remaining >= 10 -> 10
                            remaining >= 5 -> 5
                            else -> 1
                        }
                        actions.add(BotAction.InterfaceOption("Buy-${amount}", "shop:stock:${id}"))
                        remaining -= amount
                    }
                    actions.add(BotAction.CloseInterface)
                    val spaces = if (player.inventory.stackable(id)) 1 else amount
                    return Resolver(
                        id = "buy_from_shop",
                        weight = 25,
                        setup = listOf(Condition.Inventory(listOf(Entry(setOf("coins"), min = price * amount), Entry(setOf("empty"), min = spaces)))),
                        actions = actions,
                        produces = setOf("item:${id}")
                    )
                }
            }
        }
        return null
    }

    private fun valid(player: Player, item: Item, entry: Entry): Boolean {
        if (item.isEmpty()) {
            return false
        }
        if (!entry.ids.contains(item.id)) {
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

    private fun resolveEquipment(player: Player, equipment: Map<EquipSlot, Entry>): Resolver? {
        val equipment = equipment.toMutableMap()
        var resolver = unequipItems(player, equipment)
        if (resolver != null) {
            return resolver
        }
        resolver = equipItems(player, equipment)
        if (resolver != null) {
            return resolver
        }
        resolver = resolveInventory(player, equipment.values.toList())
        if (resolver != null) {
            return resolver
        }
        return null
    }

    private fun withdraw(player: Player, items: List<Entry>): Resolver? {
        val actions = mutableListOf<BotAction>()
        actions.add(BotAction.GoToNearest("bank"))
        actions.add(BotAction.InteractObject("Use-quickly", "bank_booth*", success = Condition.InterfaceOpen("bank")))
        actions.add(BotAction.InterfaceOption("Deposit carried items", "bank:carried", success = Condition.Inventory(listOf(Entry(setOf("empty"), min = 28)))))
        var found = false
        for (entry in items) {
            val item = player.bank.items.firstOrNull { item -> valid(player, item, entry) }
            if (item == null) {
                continue
            }
            withdraw(actions, entry, item)
            found = true
        }
        if (found) {
            actions.add(BotAction.CloseInterface)
            return Resolver("withdraw_from_bank", weight = 20, actions = actions)
        }
        return null
    }

    private fun equipItems(player: Player, equipment: Map<EquipSlot, Entry>): Resolver? {
        val actions = mutableListOf<BotAction>()
        val produces = mutableSetOf<String>()
        for (item in player.inventory.items) {
            val slot = item.slot
            if (slot == EquipSlot.None) {
                continue
            }
            val entry = equipment[slot] ?: continue
            if (!entry.ids.contains(item.id)) {
                continue
            }
            actions.add(BotAction.InterfaceOption("Equip", "inventory:inventory:${item.id}", success = Condition.Equipment(mapOf(slot to entry))))
            produces.add("equipment:${item.id}")
        }
        if (actions.isNotEmpty()) {
            return Resolver("equip_items", weight = 15, actions = actions, produces = produces)
        }
        return null
    }

    private fun unequipItems(player: Player, equipment: Map<EquipSlot, Entry>): Resolver? {
        val actions = mutableListOf<BotAction>()
        for ((slot, entry) in equipment) {
            if (!entry.ids.contains("empty")) {
                continue
            }
            val item = player.equipped(slot)
            if (item.isNotEmpty()) {
                actions.add(BotAction.InterfaceOption("Remove", "worn_equipment:${slot.name.lowercase()}_slot:$item"))
            }
        }
        if (actions.isNotEmpty()) {
            return Resolver("unequip_items", weight = 10, actions = actions)
        }
        return null
    }

    private fun withdraw(actions: MutableList<BotAction>, entry: Entry, item: Item) {
        if (entry.min != null && entry.min > 1) {
            actions.add(BotAction.InterfaceOption("Withdraw-X", "bank:inventory:${item.id}"))
            actions.add(BotAction.IntEntry(entry.min))
        } else {
            actions.add(BotAction.InterfaceOption("Withdraw-1", "bank:inventory:${item.id}"))
        }
    }
}

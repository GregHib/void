package content.bot.behaviour.setup

import content.bot.behaviour.action.BotAction
import content.bot.behaviour.Condition
import content.bot.behaviour.Condition.Entry
import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

object DynamicResolvers {

    fun ids() = setOf("withdraw_from_bank", "equip_from_bank")

    fun resolver(player: Player, condition: Condition) = when (condition) {
        is Condition.InArea -> Resolver("go_to_${condition.id}", -1, actions = listOf(BotAction.GoTo(condition.id)))
        is Condition.Equipment -> resolveEquipment(player, condition.items)
        is Condition.Inventory -> resolveInventory(player, condition.items)
        else -> null
    }

    private fun resolveInventory(player: Player, items: List<Entry>): Resolver? {
        val actions = mutableListOf<BotAction>()
        val items = items.toMutableList()
        actions.add(BotAction.GoToNearest("bank"))
        actions.add(BotAction.InteractObject("Use-quickly", "bank_booth*", success = Condition.InterfaceOpen("bank")))
        // Free up inventory space
        actions.add(BotAction.InterfaceOption("Deposit carried items", "bank:carried", success = Condition.Inventory(listOf(Entry(setOf("empty"), min = 28)))))
        var found = false
        for (entry in items) {
            if (entry.ids.contains("empty")) {
                continue
            }
            val index = player.bank.items.indexOfFirst { valid(player, it, entry) }
            if (index == -1) {
                // No valid items in the bank
                return null
            }
            val item = player.bank.items[index]
            // Withdraw the necessary item
            withdraw(actions, entry, item)
            found = true
        }
        actions.add(BotAction.CloseInterface)
        if (found) {
            return Resolver("withdraw_from_bank", weight = 20, actions = actions)
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
        val actions = mutableListOf<BotAction>()
        val equipment = equipment.toMutableMap()

        // Unequip items
        for ((slot, entry) in equipment) {
            if (!entry.ids.contains("empty")) {
                continue
            }
            val item = player.equipped(slot)
            if (item.isNotEmpty()) {
                actions.add(BotAction.InterfaceOption("Remove", "worn_equipment:${slot.name.lowercase()}_slot:${item}"))
            }
            equipment.remove(slot)
        }

        // Equip any held items
        for (item in player.inventory.items) {
            val slot = item.slot
            if (slot == EquipSlot.None) {
                continue
            }
            val entry = equipment[slot] ?: continue
            if (!entry.ids.contains(item.id)) {
                continue
            }
            actions.add(BotAction.InterfaceOption("Equip", "inventory:inventory:${item.id}"))
            equipment.remove(slot)
        }

        // Grab anything else from the bank
        if (equipment.isNotEmpty()) {
            actions.add(BotAction.GoToNearest("bank"))
            actions.add(BotAction.InteractObject("Use-quickly", "bank_booth*", success = Condition.InterfaceOpen("bank")))
            // Free up inventory space if needed
            if (player.inventory.spaces < equipment.size) {
                actions.add(BotAction.InterfaceOption("Deposit carried items", "bank:carried", success = Condition.Inventory(listOf(Entry(setOf("empty"), min = 28)))))
            }
            val toEquip = mutableSetOf<String>()
            for (item in player.bank.items) {
                if (item.isEmpty()) {
                    continue
                }
                val slot = item.slot
                if (slot == EquipSlot.None) {
                    continue
                }
                if (equipment.isEmpty()) {
                    break
                }
                // Check if item meets requirement
                val entry = equipment[slot] ?: continue
                if (!entry.ids.contains(item.id)) {
                    continue
                }
                if (entry.usable && !player.hasRequirementsToUse(item)) {
                    continue
                }
                if (entry.equippable && !player.hasRequirements(item)) {
                    continue
                }
                // Withdraw the necessary item
                withdraw(actions, entry, item)
                toEquip.add(item.id)
                equipment.remove(slot)
            }
            actions.add(BotAction.CloseInterface)
            // Equip all items
            for (id in toEquip) {
                actions.add(BotAction.InterfaceOption("Equip", "inventory:inventory:${id}"))
            }
        }
        if (actions.isNotEmpty()) {
            return Resolver("equip_from_bank", weight = 20, actions = actions)
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
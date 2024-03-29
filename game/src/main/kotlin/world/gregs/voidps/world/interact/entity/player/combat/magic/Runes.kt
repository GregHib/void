package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.bank.ownsItem

object Runes {
    fun castCount(player: Player, definition: InterfaceComponentDefinition): Int {
        var min = Int.MAX_VALUE
        for (item in definition.spellRequiredItems()) {
            if (item.id.endsWith("_staff")) {
                if (!player.ownsItem(item.id)) {
                    return 0
                }
                continue
            }

            if (hasInfiniteRunesEquipped(player, item.id, EquipSlot.Weapon) || hasInfiniteRunesEquipped(player, item.id, EquipSlot.Shield)) {
                min = Int.MAX_VALUE
                continue
            }
            val total = player.inventory.count(item.id)
            val casts = total / item.amount
            if (casts < min) {
                min = casts
            }
        }
        if (min == Int.MAX_VALUE) {
            return 0
        }
        return min
    }

    fun hasRunes(player: Player, item: Item, runes: MutableList<Item>, items: MutableList<Item>): Boolean {
        if (hasInfiniteRunesEquipped(player, item.id, EquipSlot.Weapon)) {
            return true
        }
        if (hasInfiniteRunesEquipped(player, item.id, EquipSlot.Shield)) {
            return true
        }

        if (item.id.endsWith("_staff") && player.equipped(EquipSlot.Weapon).id == item.id) {
            return true
        }

        var remaining = item.amount
        var found = player.inventory.count(item.id)
        if (found > 0) {
            runes.add(Item(item.id, remaining.coerceAtMost(found)))
            remaining -= found
            if (remaining <= 0) {
                return true
            }
        }

        fun hasWeaponCharge(): Boolean {
            /*val staff = player.equipped(EquipSlot.Weapon)
            if (staff.charge > 0) {
                items.add(Item(staff.id, remaining.coerceAtMost(staff.charge)))
                remaining -= staff.charge
                if (remaining <= 0) {
                    return true
                }
            }*/
            return false
        }

        if (item.id == "nature_rune" && player.equipped(EquipSlot.Weapon).id == "nature_staff" && hasWeaponCharge()) {
            return true
        }

        if (item.id == "law_rune" && player.equipped(EquipSlot.Weapon).id == "law_staff" && hasWeaponCharge()) {
            return true
        }

        val combinations: List<String>? = item.def.getOrNull("combination")
        if (combinations != null) {
            for (combination in combinations) {
                found = player.inventory.count(combination)
                if (found > 0) {
                    runes.add(Item(item.id, remaining.coerceAtMost(found)))
                    remaining -= found
                }
                if (remaining <= 0) {
                    return true
                }
            }
        }
        return remaining <= 0
    }

    private fun hasInfiniteRunesEquipped(player: Player, id: String, slot: EquipSlot): Boolean {
        val runes: List<String> = player.equipped(slot).def.getOrNull("infinite") ?: return false
        for (rune in runes) {
            if (id == rune) {
                return true
            }
        }
        return false
    }
}

fun InterfaceComponentDefinition.spellRequiredItems(): List<Item> {
    val array = requiredItems ?: return emptyList()
    val list = mutableListOf<Item>()
    val definitions: ItemDefinitions = get()
    for (i in 8..14 step 2) {
        val id = array[i] as Int
        val amount = array[i + 1] as Int
        if (id == -1 || amount <= 0) {
            break
        }
        list.add(Item(definitions.get(id).stringId, amount))
    }
    return list
}
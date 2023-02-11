package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.data.definition.extra.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.data.definition.extra.getComponentOrNull
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.interact.entity.combat.spellBook

object Runes {
    private val definitions: InterfaceDefinitions by inject()

    fun getCastCount(player: Player, definition: InterfaceComponentDefinition): Int {
        var min = Int.MAX_VALUE
        for (item in definition.spellRequiredItems()) {
            if (item.id.endsWith("_staff")) {
                if (!player.hasBanked(item.id)) {
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

    fun hasSpellRequirements(player: Player, spell: String): Boolean {
        val component = definitions.get(player.spellBook).getComponentOrNull(spell) ?: return false
        if (!player.has(Skill.Magic, component.magicLevel, message = true)) {
            return false
        }
        val items = mutableListOf<Item>()
        for (item in component.spellRequiredItems()) {
            if (!hasRunes(player, item, items)) {
                player.message("You do not have the required items to cast this spell.")
                return false
            }
        }
        for (rune in items) {
//            if (rune.id.endsWith("_staff")) {
//                val staff = player.equipped(EquipSlot.Weapon)
//                staff.charge = (staff.charge - rune.amount).coerceAtLeast(0)
//            } else {
                player.inventory.remove(rune.id, rune.amount)
//            }
        }
        return true
    }

    private fun hasRunes(player: Player, item: Item, items: MutableList<Item>): Boolean {
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
            items.add(Item(item.id, remaining.coerceAtMost(found)))
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

        val combinations: ArrayList<String>? = item.def.getOrNull("combination")
        if (combinations != null) {
            for (combination in combinations) {
                found = player.inventory.count(combination)
                if (found > 0) {
                    items.add(Item(item.id, remaining.coerceAtMost(found)))
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
        val runes: ArrayList<String> = player.equipped(slot).def.getOrNull("infinite") ?: return false
        for (rune in runes) {
            if (id == rune) {
                return true
            }
        }
        return false
    }
}

fun InterfaceComponentDefinition.spellRequiredItems(): List<Item> {
    val array = anObjectArray4758 ?: return emptyList()
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

val InterfaceComponentDefinition.magicLevel: Int
    get() = anObjectArray4758?.getOrNull(5) as? Int ?: 0

val InterfaceComponentDefinition.prettyName: String
    get() = anObjectArray4758?.getOrNull(6) as? String ?: ""
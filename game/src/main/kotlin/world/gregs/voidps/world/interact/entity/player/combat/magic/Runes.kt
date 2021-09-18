package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.getComponentOrNull
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.spellBook

object Runes {
    private val definitions: InterfaceDefinitions by inject()
    private val itemDefs: ItemDefinitions by inject()

    fun hasSpellRequirements(player: Player, spell: String): Boolean {
        val component = definitions.get(player.spellBook).getComponentOrNull(spell) ?: return false
        val array = component.anObjectArray4758 ?: return false
        val magicLevel = array[5] as Int

        if (!player.has(Skill.Magic, magicLevel, message = true)) {
            return false
        }
        val items = mutableListOf<Item>()
        for (i in 8..14 step 2) {
            val id = array[i] as Int
            val amount = array[i + 1] as Int
            if (id == -1 || amount <= 0) {
                continue
            }
            if (!hasRunes(player, id, amount, items)) {
                player.message("You do not have the required items to cast this spell.")
                return false
            }
        }
        for (rune in items) {
            if (rune.name.endsWith("_staff")) {
                val staff = player.equipped(EquipSlot.Weapon)
                staff.charge = (staff.charge - rune.amount).coerceAtLeast(0)
            } else {
                player.inventory.remove(rune.name, rune.amount)
            }
        }
        return true
    }

    private fun hasRunes(player: Player, id: Int, amount: Int, items: MutableList<Item>): Boolean {
        val name = itemDefs.getName(id)

        if (hasInfiniteRunesEquipped(player, name, EquipSlot.Weapon)) {
            return true
        }
        if (hasInfiniteRunesEquipped(player, name, EquipSlot.Shield)) {
            return true
        }

        if (name.endsWith("_staff") && player.equipped(EquipSlot.Weapon).name == name) {
            return true
        }

        var remaining = amount
        var index = player.inventory.indexOf(name)
        if (index != -1) {
            val found = player.inventory.getAmount(index)
            if (found > 0) {
                items.add(Item(name, remaining.coerceAtMost(found)))
                remaining -= found
                if (remaining <= 0) {
                    return true
                }
            }
        }

        fun hasWeaponCharge(): Boolean {
            val staff = player.equipped(EquipSlot.Weapon)
            if (staff.charge > 0) {
                items.add(Item(staff.name, remaining.coerceAtMost(staff.charge)))
                remaining -= staff.charge
                if (remaining <= 0) {
                    return true
                }
            }
            return false
        }

        if (name == "nature_rune" && player.equipped(EquipSlot.Weapon).name == "nature_staff" && hasWeaponCharge()) {
            return true
        }

        if (name == "law_rune" && player.equipped(EquipSlot.Weapon).name == "law_staff" && hasWeaponCharge()) {
            return true
        }

        val combinations = itemDefs.get(id).getOrNull("combination") as? ArrayList<String>
        if (combinations != null) {
            for (combination in combinations) {
                index = player.inventory.indexOf(combination)
                if (index != -1) {
                    val found = player.inventory.getAmount(index)
                    if (found > 0) {
                        items.add(Item(name, remaining.coerceAtMost(found)))
                        remaining -= found
                    }
                    if (remaining <= 0) {
                        return true
                    }
                }
            }
        }
        return remaining <= 0
    }

    private fun hasInfiniteRunesEquipped(player: Player, name: String, slot: EquipSlot): Boolean {
        val runes = player.equipped(slot).def.getOrNull("infinite") as? ArrayList<String>
        if (runes != null) {
            for (rune in runes) {
                if (name == rune) {
                    return true
                }
            }
        }
        return false
    }
}
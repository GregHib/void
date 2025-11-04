package content.skill.melee.armour

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.ItemAdded
import world.gregs.voidps.engine.inv.ItemRemoved
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class VoidSet : Script {

    init {
        playerSpawn {
            if (hasFullSet("")) {
                set("void_set_effect", true)
            } else if (hasFullSet("elite_")) {
                set("elite_void_set_effect", true)
            }
        }

        for (slot in listOf(
            EquipSlot.Hat.index,
            EquipSlot.Chest.index,
            EquipSlot.Legs.index,
            EquipSlot.Hands.index,
        )) {
            itemAdded("void_*", "worn_equipment", slot, ::addedVoid)
            itemRemoved("void_*", "worn_equipment", slot, ::removed)
            itemAdded("elite_void_*", "worn_equipment", slot, ::addedElite)
            itemRemoved("elite_void_*", "worn_equipment", slot, ::removed)
        }
    }

    fun addedVoid(player: Player, update: ItemAdded) {
        if (player.hasFullSet("")) {
            player["void_set_effect"] = true
        } else if (player.hasFullSet("elite_")) {
            player["elite_void_set_effect"] = true
        }
    }

    fun addedElite(player: Player, update: ItemAdded) {
        if (player.hasFullSet("elite_")) {
            player["elite_void_set_effect"] = true
        }
    }

    fun removed(player: Player, update: ItemRemoved) {
        player.clear("void_set_effect")
        player.clear("elite_void_set_effect")
    }

    fun Player.hasFullSet(prefix: String): Boolean = equipped(EquipSlot.Chest).id.startsWith("${prefix}void_knight_top") &&
        equipped(EquipSlot.Legs).id.startsWith("${prefix}void_knight_robe") &&
        equipped(EquipSlot.Hands).id.startsWith("void_knight_gloves") &&
        isHelm(equipped(EquipSlot.Hat))

    fun isHelm(item: Item): Boolean = when (item.id) {
        "void_ranger_helm", "void_melee_helm", "void_mage_helm" -> true
        else -> false
    }
}

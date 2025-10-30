package content.skill.melee.armour

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class VoidSet : Script {

    val slots = setOf(
        EquipSlot.Hat.index,
        EquipSlot.Chest.index,
        EquipSlot.Legs.index,
        EquipSlot.Hands.index,
    )

    init {
        playerSpawn {
            if (hasFullSet("")) {
                set("void_set_effect", true)
            } else if (hasFullSet("elite_")) {
                set("elite_void_set_effect", true)
            }
        }

        itemRemoved("void_*", slots, "worn_equipment") { player ->
            player.clear("void_set_effect")
            player.clear("elite_void_set_effect")
        }

        itemAdded("void_*", slots, "worn_equipment") { player ->
            if (player.hasFullSet("")) {
                player["void_set_effect"] = true
            } else if (player.hasFullSet("elite_")) {
                player["elite_void_set_effect"] = true
            }
        }

        itemRemoved("elite_void_*", slots, "worn_equipment") { player ->
            player.clear("elite_void_set_effect")
        }

        itemAdded("elite_void_*", slots, "worn_equipment") { player ->
            if (player.hasFullSet("elite_")) {
                player["elite_void_set_effect"] = true
            }
        }
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

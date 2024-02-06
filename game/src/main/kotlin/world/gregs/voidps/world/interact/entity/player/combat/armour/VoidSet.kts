package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.network.visual.update.player.EquipSlot

playerSpawn { player: Player ->
    if (player.hasFullSet("")) {
        player["void_set_effect"] = true
    } else if (player.hasFullSet("elite_")) {
        player["elite_void_set_effect"] = true
    }
}

val slots = setOf(
    EquipSlot.Hat.index,
    EquipSlot.Chest.index,
    EquipSlot.Legs.index,
    EquipSlot.Hands.index
)

itemRemoved("worn_equipment", "void_*", slots) { player: Player ->
    player.clear("void_set_effect")
    player.clear("elite_void_set_effect")
}

itemAdded("worn_equipment", "void_*", slots) { player: Player ->
    if (player.hasFullSet("")) {
        player["void_set_effect"] = true
    } else if (player.hasFullSet("elite_")) {
        player["elite_void_set_effect"] = true
    }
}

itemRemoved("worn_equipment", "elite_void_*", slots) { player: Player ->
    player.clear("elite_void_set_effect")
}

itemAdded("worn_equipment", "elite_void_*", slots) { player: Player ->
    if (player.hasFullSet("elite_")) {
        player["elite_void_set_effect"] = true
    }
}

fun Player.hasFullSet(prefix: String): Boolean {
    return equipped(EquipSlot.Chest).id.startsWith("${prefix}void_knight_top") &&
            equipped(EquipSlot.Legs).id.startsWith("${prefix}void_knight_robe") &&
            equipped(EquipSlot.Hands).id.startsWith("void_knight_gloves") &&
            isHelm(equipped(EquipSlot.Hat))
}

fun isHelm(item: Item): Boolean = when (item.id) {
    "void_ranger_helm", "void_melee_helm", "void_mage_helm" -> true
    else -> false
}
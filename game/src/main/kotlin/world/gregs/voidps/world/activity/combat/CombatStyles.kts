package world.gregs.voidps.world.activity.combat

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.clearInterfaces
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.data.definition.extra.StyleDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.weaponStyle
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot

val names = arrayOf("default", "staff", "axe", "sceptre", "pickaxe", "dagger", "sword", "2h", "mace", "claws", "hammer", "whip", "fun", "pie", "spear", "halberd", "bow", "crossbow", "thrown", "chinchompa", "fixed_device", "salamander", "scythe", "flail", "", "trident", "sol")
val styles: StyleDefinitions by inject()

on<Registered> { npc: NPC ->
    npc["combat_style"] = npc.def["style", ""]
}

on<InterfaceOpened>({ id == "combat_styles" }) { player: Player ->
    player.sendVariable("attack_style_index")
    player.sendVariable("special_attack_energy")
    player.sendVariable("auto_retaliate")
    refreshStyle(player)
}

on<InterfaceRefreshed>({ id == "combat_styles" }) { player: Player ->
    player.interfaceOptions.unlockAll(id, "style1")
    player.interfaceOptions.unlockAll(id, "style2")
    player.interfaceOptions.unlockAll(id, "style3")
    player.interfaceOptions.unlockAll(id, "style4")
}

on<ItemChanged>({ index == EquipSlot.Weapon.index }) { player: Player ->
    refreshStyle(player)
}

on<InterfaceOption>({ id == "combat_styles" && component.startsWith("style") }) { player: Player ->
    val index = component.removePrefix("style").toIntOrNull() ?: return@on
    player.clearInterfaces()
    val type = getWeaponStyleType(player)
    if (index == 1) {
        player.clear("attack_style_${names[type]}")
    } else {
        player["attack_style_${names[type]}"] = index - 1
    }
    refreshStyle(player)
}

on<InterfaceOption>({ id == "combat_styles" && component == "retaliate" }) { player: Player ->
    player.clearInterfaces()
    player.toggle("auto_retaliate")
}

fun refreshStyle(player: Player) {
    val type = getWeaponStyleType(player)
    val index = player["attack_style_${names[type]}", 0]
    val style = styles.get(type)?.getOrNull(index)
    player["attack_type"] = style?.first ?: ""
    player["attack_style"] = style?.second ?: ""
    player["combat_style"] = style?.third ?: ""
    player["attack_style_index"] = index
}

fun getWeaponStyleType(player: Player): Int {
    val key = player.equipped(EquipSlot.Weapon).def.weaponStyle()
    return if (styles.contains(key)) key else 0
}

on<InterfaceOption>({ id == "combat_styles" && component == "special_attack_bar" && option == "Use" }) { player: Player ->
    player.toggle("special_attack")
}
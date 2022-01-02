package world.gregs.voidps.world.activity.combat

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.client.variable.toggleVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.definition.StyleDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.item.weaponStyle
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject

val names = arrayOf("default", "staff", "axe", "sceptre", "pickaxe", "dagger", "sword", "2h", "mace", "claws", "hammer", "whip", "fun", "pie", "spear", "halberd", "bow", "crossbow", "thrown", "chinchompa", "fixed_device", "salamander", "scythe", "flail", "", "trident", "sol")
val styles: StyleDefinitions by inject()

on<Registered> { npc: NPC ->
    npc["combat_style"] = npc.def["style", ""]
}

on<InterfaceOpened>({ id == "combat_styles" }) { player: Player ->
    player.sendVar("attack_style")
    player.sendVar("special_attack_energy")
    player.sendVar("auto_retaliate")
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
    val type = getWeaponStyleType(player)
    if (index == 1) {
        player.clear("attack_style_${names[type]}")
    } else {
        player["attack_style_${names[type]}", true] = index - 1
    }
    refreshStyle(player)
}

on<InterfaceOption>({ id == "combat_styles" && component == "retaliate" }) { player: Player ->
    player.toggleVar("auto_retaliate")
}

fun refreshStyle(player: Player) {
    val type = getWeaponStyleType(player)
    val index = player["attack_style_${names[type]}", 0]
    val style = styles.get(type)?.getOrNull(index)
    player.setVar("attack_style", index)
    player["attack_type"] = style?.first ?: ""
    player["attack_style"] = style?.second ?: ""
    player["combat_style"] = style?.third ?: ""
}

fun getWeaponStyleType(player: Player): Int {
    val key = player.equipped(EquipSlot.Weapon).def.weaponStyle()
    return if (styles.contains(key)) key else 0
}

on<InterfaceOption>({ id == "combat_styles" && component == "special_attack_bar" && option == "Use" }) { player: Player ->
    player.toggleVar("special_attack")
}
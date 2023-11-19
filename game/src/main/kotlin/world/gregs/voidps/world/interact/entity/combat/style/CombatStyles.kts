package world.gregs.voidps.world.interact.entity.combat.style

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.weaponStyle
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.network.visual.update.player.EquipSlot

val styles: WeaponStyleDefinitions by inject()

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
    player.closeInterfaces()
    val type = getWeaponStyleType(player)
    val style = styles.get(type)
    if (index == 1) {
        player.clear("attack_style_${style.stringId}")
    } else {
        player["attack_style_${style.stringId}"] = index - 1
    }
    refreshStyle(player)
}

on<InterfaceOption>({ id == "combat_styles" && component == "retaliate" }) { player: Player ->
    player.closeInterfaces()
    player.toggle("auto_retaliate")
}

fun refreshStyle(player: Player) {
    val type = getWeaponStyleType(player)
    val style = styles.get(type)
    val index = player["attack_style_${style.stringId}", 0]
    player["attack_type"] = style.attackTypes.getOrNull(index) ?: ""
    player["attack_style"] = style.attackStyles.getOrNull(index) ?: ""
    player["combat_style"] = style.combatStyles.getOrNull(index) ?: ""
    player["attack_style_index"] = index
}

fun getWeaponStyleType(player: Player): Int {
    return player.equipped(EquipSlot.Weapon).def.weaponStyle()
}

on<InterfaceOption>({ id == "combat_styles" && component == "special_attack_bar" && option == "Use" }) { player: Player ->
    player.toggle("special_attack")
}
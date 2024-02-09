package world.gregs.voidps.world.interact.entity.combat.style

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.itemChange
import world.gregs.voidps.network.visual.update.player.EquipSlot

val styles: WeaponStyleDefinitions by inject()

npcSpawn { npc: NPC ->
    npc["combat_style"] = npc.def["style", ""]
}

interfaceOpen("combat_styles") { player: Player ->
    player.sendVariable("attack_style_index")
    player.sendVariable("special_attack_energy")
    player.sendVariable("auto_retaliate")
    refreshStyle(player)
}

interfaceRefresh("combat_styles") { player: Player ->
    player.interfaceOptions.unlockAll(id, "style1")
    player.interfaceOptions.unlockAll(id, "style2")
    player.interfaceOptions.unlockAll(id, "style3")
    player.interfaceOptions.unlockAll(id, "style4")
}

itemChange(EquipSlot.Weapon) { player: Player ->
    refreshStyle(player)
}

interfaceOption(component = "style*", id = "combat_styles") {
    val index = component.removePrefix("style").toIntOrNull() ?: return@interfaceOption
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

interfaceOption(component = "retaliate", id = "combat_styles") {
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
    return player.equipped(EquipSlot.Weapon).def["weapon_style", 0]
}

interfaceOption("Use", "special_attack_bar", "combat_styles") {
    player.toggle("special_attack")
}
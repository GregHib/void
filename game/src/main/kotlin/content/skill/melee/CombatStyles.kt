package content.skill.melee

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

@Script
class CombatStyles : Api {

    val styles: WeaponStyleDefinitions by inject()

    init {
        npcSpawn { npc ->
            npc["combat_style"] = npc.def.getOrNull("style") ?: return@npcSpawn
        }

        interfaceOpen("combat_styles") { player ->
            player.sendVariable("attack_style_index")
            player.sendVariable("special_attack_energy")
            player.sendVariable("auto_retaliate")
            refreshStyle(player)
        }

        interfaceRefresh("combat_styles") { player ->
            player.interfaceOptions.unlockAll(id, "style1")
            player.interfaceOptions.unlockAll(id, "style2")
            player.interfaceOptions.unlockAll(id, "style3")
            player.interfaceOptions.unlockAll(id, "style4")
        }

        inventoryChanged("worn_equipment", EquipSlot.Weapon) { player ->
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

        interfaceOption("Auto Retaliate", "retaliate", "combat_styles") {
            player.closeInterfaces()
            player.toggle("auto_retaliate")
        }

        interfaceOption("Use", "special_attack_bar", "combat_styles") {
            player.toggle("special_attack")
        }
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

    fun getWeaponStyleType(player: Player): Int = player.equipped(EquipSlot.Weapon).def["weapon_style", 0]
}

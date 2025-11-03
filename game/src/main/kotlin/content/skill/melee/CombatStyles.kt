package content.skill.melee

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventoryChanged
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CombatStyles : Script {

    val styles: WeaponStyleDefinitions by inject()

    init {
        npcSpawn {
            this["combat_style"] = def.getOrNull("style") ?: return@npcSpawn
        }

        interfaceOpen("combat_styles") {
            sendVariable("attack_style_index")
            sendVariable("special_attack_energy")
            sendVariable("auto_retaliate")
            refreshStyle(this)
        }

        interfaceRefresh("combat_styles") { id ->
            interfaceOptions.unlockAll(id, "style1")
            interfaceOptions.unlockAll(id, "style2")
            interfaceOptions.unlockAll(id, "style3")
            interfaceOptions.unlockAll(id, "style4")
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

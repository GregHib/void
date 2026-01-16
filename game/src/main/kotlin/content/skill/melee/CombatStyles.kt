package content.skill.melee

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CombatStyles(val styles: WeaponStyleDefinitions) : Script {

    init {
        npcSpawn {
            this["combat_style"] = def.getOrNull("style") ?: return@npcSpawn
        }

        interfaceOpened("combat_styles") {
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

        slotChanged("worn_equipment", EquipSlot.Weapon) {
            refreshStyle(this)
        }

        interfaceOption(id = "combat_styles:style*") {
            val index = it.component.removePrefix("style").toIntOrNull() ?: return@interfaceOption
            closeInterfaces()
            val type = getWeaponStyleType(this)
            val style = styles.get(type)
            if (index == 1) {
                clear("attack_style_${style.stringId}")
            } else {
                set("attack_style_${style.stringId}", index - 1)
            }
            refreshStyle(this)
        }

        interfaceOption("Auto Retaliate", "combat_styles:retaliate") {
            closeInterfaces()
            toggle("auto_retaliate")
        }

        interfaceOption("Use", "combat_styles:special_attack_bar") {
            toggle("special_attack")
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

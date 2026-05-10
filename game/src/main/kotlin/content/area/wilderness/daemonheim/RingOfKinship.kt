package content.area.wilderness.daemonheim

import content.entity.effect.toxin.curePoison
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class RingOfKinship : Script {
    init {
        itemOption("Customise", "ring_of_kinship") {
            message("You must be inside of Daemonheim to do this.")
            return@itemOption
        }

        itemOption("Open party interface", "ring_of_kinship") {
            if (tile !in Areas["daemonheim_courtyard"]) {
                message("You must be inside of Daemonheim to do this.")
                return@itemOption
            }
            open("dungeoneering_party")
            tab(Tab.QuestJournals)
        }


        itemOption("Customise", "ring_of_kinship_*") {
            open("kinship_customisation")
        }

        itemOption("Quick-switch", "ring_of_kinship_*") {
            closeMenu()
            message("Switched to Ring of kinship (berserker).")
        }

        interfaceOpened("kinship_customisation") {
            set("kinship_customisation_tab", "melee")
            sendScript("kin_ring_button_click", 65077505)
        }

        interfaceOption("Select", "kinship_customisation:melee,kinship_customisation:ranged,kinship_customisation:magic,kinship_customisation:skiller") {
            set("kinship_customisation_tab", it.component)
        }

        /*
            TODO:
                max out upgrade in one class
                reset all (do you get tokens back?)
                attempt second reset
         */
        interfaceOption("Upgrade", "kinship_customisation:upgrade_*") {
            val index = it.component.removePrefix("upgrade_").toInt()
            val tab: String = get("kinship_customisation_tab") ?: return@interfaceOption
            val type = className(tab, index)
            set("kinship_upgrade_class", type)
            val enum = EnumDefinitions.get("kinship_upgrades_${type}").map ?: return@interfaceOption
            interfaces.sendVisibility(it.id, "upgrade_overlay", true)
            val currentLevel = get("kinship_${type}_level", 0)
            interfaces.sendText(
                it.id, "upgrade_text", """
                Current state:
                ${enum[currentLevel]}
                
                Next state:
                ${enum[currentLevel + 1]}
            """.trimIndent().replace("\n", "<br>")
            )
            interfaces.sendText(it.id, "upgrade_cost", "UPGRADE COST: ${EnumDefinitions.int("kinship_upgrade_costs", currentLevel).toDigitGroupString()}")
            interfaces.sendText(it.id, "upgrade_tokens", get("dungeoneering_tokens", 0).toDigitGroupString())
        }

        interfaceOption("Confirm", "kinship_customisation:upgrade_confirm") {
            val tab: String = get("kinship_customisation_tab") ?: return@interfaceOption
            val type: String = get("kinship_upgrade_class") ?: return@interfaceOption
            val currentLevel = get("kinship_${type}_level", 0)
            val cost = EnumDefinitions.int("kinship_upgrade_costs", currentLevel)
            val tokens = get("dungeoneering_tokens", 0)
            if (tokens < cost) {
                message("You don't have enough tokens for that upgrade.") // TODO proper message
                return@interfaceOption
            }
            // 47500

            dec("dungeoneering_tokens", cost)
            inc("kinship_${type}_level")
            message("Your ring has been upgraded!")
            interfaces.sendVisibility(it.id, "upgrade_overlay", false)
            sendScript("kin_ring_button_click", InterfaceDefinitions.getComponent("kinship_customisation", tab)!!.id)
        }

        destructible("ring_of_kinship_*") {
            message("You cannot destroy that here.")
            false
        }

        interfaceOption("Switch-to", "kinship_customisation:switch_*") {
            val index = it.component.removePrefix("switch_").toInt()
            val tab: String = get("kinship_customisation_tab") ?: return@interfaceOption
            val type = className(tab, index)
            val currentClass = get("kinship_class", "tank") // TODO check the default class
            if (type == currentClass) {
                message("You are already using that ring.")
                return@interfaceOption
            }
            val quickSwitch = get("kinship_quick_switch_class", "tank")
            if (type == quickSwitch) {
                message("As that was your quick-switch ring choice, your quick-switch has now reset.")
                if (quickSwitch == "tank") {
                    set("kinship_quick_switch_class", "tactician")
                } else {
                    clear("kinship_quick_switch_class")
                }
            }
            inventory.replace("ring_of_kinship_${currentClass}", "ring_of_kinship_${type}")
            equipment.replace("ring_of_kinship_${currentClass}", "ring_of_kinship_${type}")
            set("kinship_class", type)
            message("Switching to ${type.replace("_", "-")} ring.")
        }

        interfaceOption("Quick-switch", "kinship_customisation:switch_*") {
            val index = it.component.removePrefix("switch_").toInt()
            val tab: String = get("kinship_customisation_tab") ?: return@interfaceOption
            val type = className(tab, index)
            set("kinship_quick_switch_class", type)
            message("Quick-switch ring set to ${type.replace("_", "-")}.")
        }

        interfaceOption("Cancel", "kinship_customisation:upgrade_cancel") {
            interfaces.sendVisibility(it.id, "upgrade_overlay", false)
        }

        interfaceOption("Reset", "kinship_customisation:reset") {
            interfaces.sendVisibility(it.id, "reset_overlay", true)
        }

        interfaceOption("Cancel", "kinship_customisation:reset_cancel") {
            interfaces.sendVisibility(it.id, "reset_overlay", false)
        }
    }

    private fun className(tab: String, index: Int): String {
        return EnumDefinitions.getStruct<String>("kinship_group_${tab}", index, "dungeoneering_class_name").toSnakeCase()
    }
}
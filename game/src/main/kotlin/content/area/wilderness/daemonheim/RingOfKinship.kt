package content.area.wilderness.daemonheim

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.skill.magic.jewellery.itemTeleport
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace

class RingOfKinship : Script {
    init {
        itemOption("Teleport to Daemonheim", "ring_of_kinship", "*") {
            itemTeleport(this, Areas["daemonheim_teleport"], "kinship")
        }

        itemOption("Open party interface", "ring_of_kinship*", "*") {
            if (!inDungeoneering && tile !in Areas["daemonheim_castle"]) {
                message("You must be inside of Daemonheim to do this.")
                return@itemOption
            }
            open("dungeoneering_party")
            tab(Tab.QuestJournals)
        }

        itemOption("Customise", "ring_of_kinship*", "*") {
            if (!inDungeoneering) {
                message("You must be inside of Daemonheim to do this.")
                return@itemOption
            }
            open("kinship_customisation")
        }

        itemOption("Quick-switch", "ring_of_kinship_*", "*") {
            if (!inDungeoneering) {
                message("You must be inside of Daemonheim to do this.")
                return@itemOption
            }
            closeMenu()
            val quickSwitch: String? = get("kinship_quick_switch_class")
            if (quickSwitch == null) {
                message("You have no ring selected to quick-switch to. Select 'Customise' and pick a secondary ring to quick-switch to.")
                return@itemOption
            }
            val currentClass: String = get("kinship_class") ?: it.item.id.removePrefix("ring_of_kinship_")
            val inventory = inventories.inventory(it.inventory)
            inventory.replace("ring_of_kinship_$currentClass", "ring_of_kinship_$quickSwitch")
            set("kinship_class", quickSwitch)
            set("kinship_quick_switch_class", currentClass)
            message("Switched to Ring of kinship (${quickSwitch.replace("_", "-")}).")
        }

        interfaceOpened("kinship_customisation") {
            set("kinship_customisation_tab", "melee")
            sendVariable("kinship_class")
            forEachClass { name ->
                sendVariable("kinship_${name}_level")
            }
            refreshTab()
        }

        interfaceOption("Select", "kinship_customisation:melee,kinship_customisation:ranged,kinship_customisation:magic,kinship_customisation:skiller") {
            set("kinship_customisation_tab", it.component)
        }

        destructible("ring_of_kinship_*") {
            message("You cannot destroy that here.")
            false
        }

        /*
            Upgrade
         */

        interfaceOption("Upgrade", "kinship_customisation:upgrade_*") {
            val index = it.component.removePrefix("upgrade_").toInt()
            val tab: String = get("kinship_customisation_tab") ?: return@interfaceOption
            val type = className(tab, index)
            set("kinship_upgrade_class", type)
            val enum = EnumDefinitions.get("kinship_upgrades_$type").map ?: return@interfaceOption
            interfaces.sendVisibility(it.id, "upgrade_overlay", true)
            val currentLevel = get("kinship_${type}_level", 0)
            interfaces.sendText(
                it.id,
                "upgrade_text",
                """
                Current state:
                ${enum[currentLevel]}
                
                Next state:
                ${enum[currentLevel + 1]}
                """.trimIndent().replace("\n", "<br>"),
            )
            interfaces.sendText(it.id, "upgrade_cost", "UPGRADE COST: ${EnumDefinitions.int("kinship_upgrade_costs", currentLevel).toDigitGroupString()}")
            interfaces.sendText(it.id, "upgrade_tokens", get("dungeoneering_tokens", 0).toDigitGroupString())
        }

        interfaceOption("Confirm", "kinship_customisation:upgrade_confirm") {
            val type: String = get("kinship_upgrade_class") ?: return@interfaceOption
            val currentLevel = get("kinship_${type}_level", 0)
            val cost = EnumDefinitions.int("kinship_upgrade_costs", currentLevel)
            val tokens = get("dungeoneering_tokens", 0)
            if (tokens < cost) {
                message("You need $cost tokens to upgrade this ring.")
                return@interfaceOption
            }
            dec("dungeoneering_tokens", cost)
            inc("kinship_${type}_level")
            message("Your ring has been upgraded!")
            interfaces.sendVisibility(it.id, "upgrade_overlay", false)
            refreshTab()
        }

        interfaceOption("Cancel", "kinship_customisation:upgrade_cancel") {
            interfaces.sendVisibility(it.id, "upgrade_overlay", false)
        }

        /*
            Switch
         */

        interfaceOption("Switch-to", "kinship_customisation:switch_*") {
            val index = it.component.removePrefix("switch_").toInt()
            val tab: String = get("kinship_customisation_tab") ?: return@interfaceOption
            val type = className(tab, index)
            val currentClass = get("kinship_class", "none") // TODO check the default class
            if (type == currentClass) {
                message("You are already using that ring.")
                return@interfaceOption
            }
            val quickSwitch: String? = get("kinship_quick_switch_class")
            if (type == quickSwitch) {
                message("As that was your quick-switch ring choice, your quick-switch has now reset.")
                if (quickSwitch == "tank") {
                    set("kinship_quick_switch_class", "tactician")
                } else {
                    clear("kinship_quick_switch_class")
                }
            }
            val current = if (currentClass == "none") "ring_of_kinship" else "ring_of_kinship_$currentClass"
            inventory.replace(current, "ring_of_kinship_$type")
            equipment.replace(current, "ring_of_kinship_$type")
            set("kinship_class", type)

            // FIXME: manual text changing and text changing using 3494.cs2 doesn't work for some reason.
            message("Switching to ${type.replace("_", "-")} ring.")
        }

        interfaceOption("Quick-switch", "kinship_customisation:switch_*") {
            val index = it.component.removePrefix("switch_").toInt()
            val tab: String? = get("kinship_customisation_tab")
            if (tab == null) {
                message("You have no ring selected to quick-switch to. Select 'Customise' and pick a secondary ring to quick-switch to.")
                return@interfaceOption
            }
            val currentClass: String? = get("kinship_class")
            if (tab == currentClass) {
                message("This ring is already in use.")
                return@interfaceOption
            }
            val type = className(tab, index)
            set("kinship_quick_switch_class", type)
            message("Quick-switch ring set to ${type.replace("_", "-")}.")
        }

        /*
            Reset
         */

        interfaceOption("Reset", "kinship_customisation:reset") {
            interfaces.sendVisibility(it.id, "reset_overlay", true)
            val resets = get("kinship_reset_count", 0)
            interfaces.sendText(it.id, "reset_count", resets.toString())
            interfaces.sendText(it.id, "reset_text", if (resets > 0) "Are you sure you wish to reset?" else "Sorry, you have no remaining resets.")
            interfaces.sendVisibility(it.id, "reset_disabled", resets <= 0)
            interfaces.sendText(it.id, "reset_count", get("kinship_reset_count", 0).toString())
        }

        interfaceOption("Reset", "kinship_customisation:reset_confirm") {
            val resets = get("kinship_reset_count", 0)
            if (resets <= 0) {
                return@interfaceOption
            }
            var tokens = 0
            forEachClass { name ->
                val currentLevel = get("kinship_${name}_level", 0)
                if (currentLevel > 0) {
                    for (level in 0 until currentLevel) {
                        tokens += EnumDefinitions.int("kinship_upgrade_costs", level)
                    }
                    set("kinship_${name}_level", 0)
                }
            }
            if (tokens == 0) {
                message("You currently have no class upgrades to reset.") // Rs3 doesn't have a proper message or protect from this.
                return@interfaceOption
            }
            dec("kinship_reset_count")
            inc("dungeoneering_tokens", tokens)
            refreshTab()
            message("The tokens you spent on Dungeoneering reward rings have been refunded.")
            interfaces.sendVisibility(it.id, "reset_overlay", false)
        }

        interfaceOption("Cancel", "kinship_customisation:reset_cancel") {
            interfaces.sendVisibility(it.id, "reset_overlay", false)
        }
    }

    private fun forEachClass(block: (String) -> Unit) {
        for (group in listOf("melee", "ranged", "magic", "skiller")) {
            val map = EnumDefinitions.get("kinship_group_$group").map ?: continue
            for ((_, id) in map) {
                id as Int
                val name = StructDefinitions.get(id).get<String>(Params.DUNGEONEERING_CLASS_NAME).toSnakeCase()
                block(name)
            }
        }
    }

    private fun Player.refreshTab() {
        val tab: String = get("kinship_customisation_tab") ?: return
        sendScript("kin_ring_button_click", InterfaceDefinitions.getComponent("kinship_customisation", tab)!!.id)
    }

    private fun className(tab: String, index: Int): String = EnumDefinitions.getStruct<String>("kinship_group_$tab", index, "dungeoneering_class_name").toSnakeCase()
}

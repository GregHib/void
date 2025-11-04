package content.entity.player.equip

import content.entity.player.equip.EquipBonuses.names
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.InterfaceInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.VisualMask.APPEARANCE_MASK
import java.math.RoundingMode
import java.text.DecimalFormat

class EquipmentBonuses : Script {

    val definitions: ItemDefinitions by inject()

    val df = DecimalFormat("0.0").apply {
        roundingMode = RoundingMode.FLOOR
    }

    init {
        playerSpawn {
            updateStats(this)
            set("bank_hidden", true)
        }

        slotChanged("worn_equipment") {
            updateStats(this, it.fromItem, false)
            updateStats(this, it.item, true)
        }

        interfaceOpen("equipment_bonuses") {
            interfaces.sendVisibility("equipment_bonuses", "close", !get("equipment_bank_button", false))
            updateEmote(this)
            open("equipment_side")
            interfaceOptions.unlockAll("equipment_bonuses", "inventory", 0 until 16)
            updateStats(this)
            set("bank_hidden", true)
            sendScript("bank_show_equip_screen")
            tab(Tab.Inventory)
        }

        interfaceClose("equipment_bonuses") {
            open("inventory")
        }

        interfaceRefresh("equipment_side") { player ->
            interfaceOptions.send("equipment_side", "inventory")
            interfaceOptions.unlockAll("equipment_side", "inventory", 0 until 28)
        }

        interfaceOption("Stats", "equipment_bonuses:inventory") { (item) ->
            if (equipping()) {
                showStats(this, definitions.get(item.id))
            }
        }

        interfaceOption("Done", "equipment_bonuses:stats_done") {
            if (equipping()) {
                clear("equipment_titles")
                clear("equipment_names")
                clear("equipment_stats")
                clear("equipment_name")
            }
        }

        interfaceOption("Equip", "equipment_side:inventory") { (item, itemSlot) ->
            if (equipping()) {
                InterfaceInteraction.itemOption(this, ItemOption(item, itemSlot, "inventory", "Wield"))
                checkEmoteUpdate(this)
            }
        }

        interfaceOption("Remove", "equipment_bonuses:inventory") { (item, itemSlot) ->
            if (equipping()) {
                InterfaceInteraction.itemOption(this, ItemOption(item, itemSlot, "inventory", "Remove"))
                checkEmoteUpdate(this)
            }
        }
    }

    fun Player.equipping() = menu == "equipment_bonuses"

    /*
        Redirect equipping actions to regular inventories
     */

    fun checkEmoteUpdate(player: Player) {
        if (player.visuals.flagged(APPEARANCE_MASK)) {
            updateEmote(player)
        }
    }

    fun updateEmote(player: Player) {
        player["equipment_emote"] = player.appearance.emote
    }

    fun updateStats(player: Player, item: Item, add: Boolean) {
        names.forEach { (name, key) ->
            val value = item.def[key, 0]
            if (value > 0) {
                val current = player[key, 0]
                val modified = if (add) {
                    current + value
                } else {
                    current - value
                }
                player[key] = modified
                sendBonus(player, name, key, modified)
            }
        }
    }

    fun sendBonus(player: Player, name: String, key: String, value: Int) {
        if (player.menu == "equipment_bonuses") {
            player.interfaces.sendText("equipment_bonuses", key, "$name: ${EquipBonuses.format(key, value, true)}")
        }
    }

    fun updateStats(player: Player) {
        names.forEach { (name, key) ->
            player[key] = 0
            sendBonus(player, name, key, 0)
        }
        player.equipment.items.forEach {
            if (it.isNotEmpty()) {
                updateStats(player, it, true)
            }
        }
    }

    /*
        https://www.reddit.com/r/runescape/comments/k0irv/new_clothing_and_weapons_from_branches_of/
        https://www.wikihow-fun.com/images/thumb/0/04/Mine-for-Gems-in-RuneScape-Step-6.jpg/aid803430-v4-728px-Mine-for-Gems-in-RuneScape-Step-6.jpg
     */
    fun showStats(player: Player, item: ItemDefinition) {
        player["equipment_name"] = item.name

        val titles = StringBuilder()
        val types = StringBuilder()
        val stats = StringBuilder()

        var first = true

        fun appendTitle(name: String, prefix: Boolean) {
            if (prefix) {
                titles.append("<br>")
                types.append("<br>")
                stats.append("<br>")
            }
            titles.append("$name<br>")
            types.append("<br>")
            stats.append("<br>")
        }

        fun appendLine(name: String, value: String) {
            titles.append("<br>")
            types.append(name.replace("Ranged Strength", "Ranged Str."), if (first) ":                 " else ":", "<br>")
            stats.append(value, "<br>")
            first = false
        }

        fun addValue(index: Int) {
            val (name, key) = names[index]
            val value = EquipBonuses.getValue(item, key)
            if (value != null) {
                appendLine(name, value)
            }
        }

        appendTitle("Attack bonus", false)
        for (i in 0 until 5) {
            addValue(i)
        }
        appendTitle("Defence bonus", true)
        for (i in 5 until 14) {
            addValue(i)
        }
        appendTitle("Other", true)
        for (i in 14 until 18) {
            addValue(i)
        }

        if (item.contains("attack_speed")) {
            val attackSpeed = item["attack_speed", 4]
            if (attackSpeed != 0) {
                appendLine(
                    "Attack Rate",
                    when (attackSpeed) {
                        2 -> "Very fast"
                        3 -> "Fast"
                        4 -> "Standard"
                        5 -> "Slow"
                        6 -> "Very slow"
                        else -> attackSpeed.toString()
                    },
                )
            }
        }
        appendLine("Weight", "${df.format(item["weight", 0.0])} kg")

        player["equipment_titles"] = titles.toString()
        player["equipment_names"] = types.toString()
        player["equipment_stats"] = stats.toString()
        player.sendVariable("equipment_titles")
        player.sendVariable("equipment_names")
        player.sendVariable("equipment_stats")
    }
}

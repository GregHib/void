package content.entity.player.equip

import content.entity.player.equip.EquipBonuses.names
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Publishers
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.VisualMask.APPEARANCE_MASK
import world.gregs.voidps.type.sub.*
import java.math.RoundingMode
import java.text.DecimalFormat

class EquipmentBonuses(private val definitions: ItemDefinitions) {

    val df = DecimalFormat("0.0").apply {
        roundingMode = RoundingMode.FLOOR
    }

    @Spawn
    fun spawn(player: Player) {
        updateStats(player)
        player["bank_hidden"] = true
    }

    @InventorySlotChanged("worn_equipment")
    fun update(player: Player, item: Item, fromItem: Item) {
        updateStats(player, fromItem, false)
        updateStats(player, item, true)
    }

    @Open("equipment_bonuses")
    fun open(player: Player) {
        player.interfaces.sendVisibility("equipment_bonuses", "close", !player["equipment_bank_button", false])
        updateEmote(player)
        player.open("equipment_side")
        player.interfaceOptions.unlockAll("equipment_bonuses", "inventory", 0 until 16)
        updateStats(player)
        player["bank_hidden"] = true
        player.sendScript("bank_show_equip_screen")
        player.tab(Tab.Inventory)
    }

    @Close("equipment_bonuses")
    fun close(player: Player) {
        player.open("inventory")
    }

    @Refresh("equipment_side")
    fun refresh(player: Player) {
        player.interfaceOptions.send("equipment_side", "inventory")
        player.interfaceOptions.unlockAll("equipment_side", "inventory", 0 until 28)
    }

    @Interface("Stats", "inventory", "equipment_bonuses")
    fun stats(player: Player, item: Item) {
        if (player.equipping()) {
            showStats(player, definitions.get(item.id))
        }
    }

    @Interface("Done", "stats_done", "equipment_bonuses")
    fun done(player: Player) {
        if (player.equipping()) {
            player.clear("equipment_titles")
            player.clear("equipment_names")
            player.clear("equipment_stats")
            player.clear("equipment_name")
        }
    }

    @Interface("Equip", "inventory", "equipment_side")
    suspend fun equip(player: Player, item: Item, itemSlot: Int) {
        if (player.equipping()) {
            Publishers.all.inventoryOption(player, item, "inventory", "Wield", itemSlot)
            checkEmoteUpdate(player)
        }
    }

    @Interface("Remove", "inventory", "equipment_bonuses")
    suspend fun remove(player: Player, item: Item, itemSlot: Int) {
        if (player.equipping()) {
            Publishers.all.inventoryOption(player, item, "worn_equipment", "Remove", itemSlot)
            checkEmoteUpdate(player)
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

package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.contain.ItemChanged
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.interact.StopInteraction
import world.gregs.voidps.engine.entity.character.mode.interact.clear
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.attackSpeed
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.visual.VisualMask.APPEARANCE_MASK
import world.gregs.voidps.world.interact.entity.player.equip.EquipBonuses.names
import java.math.RoundingMode
import java.text.DecimalFormat

val definitions: ItemDefinitions by inject()

fun Player.equipping() = menu == "equipment_bonuses"

on<Registered> { player: Player ->
    updateStats(player)
}

on<ItemChanged>({ container == "worn_equipment" }) { player: Player ->
    updateStats(player, oldItem, false)
    updateStats(player, item, true)
}

on<InterfaceOpened>({ id == "equipment_bonuses" }) { player: Player ->
    player.interfaces.sendVisibility("equipment_bonuses", "close", !player.getVar("equipment_banking", false))
    updateEmote(player)
    player.open("equipment_side")
    player.interfaceOptions.unlockAll("equipment_bonuses", "container", 0 until 16)
    updateStats(player)
}

on<StopInteraction>({ it.equipping() }) { player: Player ->
    player.close("equipment_bonuses")
}

on<InterfaceClosed>({ id == "equipment_bonuses" }) { player: Player ->
    player.open("inventory")
}

on<InterfaceRefreshed>({ id == "equipment_side" }) { player: Player ->
    player.interfaceOptions.send("equipment_side", "container")
    player.interfaceOptions.unlockAll("equipment_side", "container", 0 until 28)
}

on<InterfaceOption>({ it.equipping() && (id == "equipment_side" || id == "equipment_bonuses") && component == "container" && option == "Stats" }) { player: Player ->
    showStats(player, definitions.get(item.id))
}

/*
    Redirect equipping actions to regular containers
 */

on<InterfaceOption>({ it.equipping() && id == "equipment_side" && component == "container" && option == "Equip" }) { player: Player ->
    player.events.emit(ContainerOption(player, "inventory", item, itemSlot, "Wield"))
    checkEmoteUpdate(player)
}

on<InterfaceOption>({ it.equipping() && id == "equipment_bonuses" && component == "container" && option == "Remove" }) { player: Player ->
    player.events.emit(ContainerOption(player, "worn_equipment", item, itemSlot, "Remove"))
    checkEmoteUpdate(player)
}

fun checkEmoteUpdate(player: Player) {
    player.clear()
    if (player.visuals.flagged(APPEARANCE_MASK)) {
        updateEmote(player)
    }
}

fun updateEmote(player: Player) {
    player.setVar("equipment_emote", player.appearance.emote)
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
    player.setVar("equipment_name", item.name)

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
        titles.append("${name}<br>")
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

    if (item.has(14)) {
        appendLine("attack rate", when (item.attackSpeed()) {
            2 -> "Very fast"
            3 -> "Fast"
            4 -> "Standard"
            5 -> "Slow"
            6 -> "Very slow"
            else -> item.attackSpeed().toString()
        })
    }
    appendLine("Weight", "${df.format(item["weight", 0.0])} kg")

    player.setVar("equipment_titles", titles.toString())
    player.setVar("equipment_names", types.toString())
    player.setVar("equipment_stats", stats.toString())
}

val df = DecimalFormat("0.0").apply {
    roundingMode = RoundingMode.FLOOR
}
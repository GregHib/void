package rs.dusk.world.interact.entity.player.equip

import rs.dusk.cache.definition.data.ItemDefinition
import rs.dusk.engine.action.ActionType
import rs.dusk.engine.action.action
import rs.dusk.engine.client.ui.awaitInterface
import rs.dusk.engine.client.ui.close
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.client.variable.*
import rs.dusk.engine.entity.character.contain.ContainerModification
import rs.dusk.engine.entity.character.contain.equipment
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.set
import rs.dusk.engine.entity.character.update.visual.player.APPEARANCE_MASK
import rs.dusk.engine.entity.character.update.visual.player.appearance
import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.func.toPascalCase
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceOption
import java.math.RoundingMode
import java.text.DecimalFormat

BooleanVariable(4894, Variable.Type.VARBIT, defaultValue = false).register("equipment_banking")
IntVariable(779, Variable.Type.VARC, defaultValue = 1426).register("equipment_emote")
StringVariable(321, Variable.Type.VARCSTR).register("equipment_name")
StringVariable(322, Variable.Type.VARCSTR).register("equipment_titles")
StringVariable(323, Variable.Type.VARCSTR).register("equipment_names")
StringVariable(324, Variable.Type.VARCSTR).register("equipment_stats")
StringVariable(325, Variable.Type.VARCSTR).register("comparison_stats")

val definitions: ItemDefinitions by inject()

fun Player.equipping() = action.type == ActionType.Equipping

InterfaceOpened where { name == "equipment_bonuses" } then {
    player.action(ActionType.Equipping) {
        val listener: (List<ContainerModification>) -> Unit = equipmentUpdateListener(player)
        try {
            player.interfaces.sendVisibility("equipment_bonuses", "close", !player.getVar("equipment_banking", false))
            player.equipment.listeners.add(listener)
            updateEmote(player)
            player.open("equipment_side")
            player.interfaceOptions.unlockAll("equipment_bonuses", "container", 0 until 16)
            updateStats(player)
            awaitInterface(name)
        } finally {
            player.equipment.listeners.remove(listener)
            player.open("inventory")
            player.close("equipment_bonuses")
        }
    }
}

InterfaceOpened where { name == "equipment_side" } then {
    player.interfaceOptions.send("equipment_side", "container")
    player.interfaceOptions.unlockAll("equipment_side", "container", 0 until 28)
}

InterfaceOption where { player.equipping() && (name == "equipment_side" || name == "equipment_bonuses") && component == "container" && option == "Stats" } then {
    showStats(player, definitions.get(name))
}

/*
    Redirect equipping actions to regular containers
 */
val bus: EventBus by inject()

InterfaceOption where { player.equipping() && name == "equipment_side" && component == "container" && option == "Equip" } then {
    bus.emit(ContainerAction(player, "inventory", item, itemIndex, "Wield"))
    checkEmoteUpdate(player)
}

InterfaceOption where { player.equipping() && name == "equipment_bonuses" && component == "container" && option == "Remove" } then {
    bus.emit(ContainerAction(player, "worn_equipment", item, itemIndex, "Remove"))
    checkEmoteUpdate(player)
}

fun equipmentUpdateListener(player: Player): (List<ContainerModification>) -> Unit = { list ->
    list.forEach {
        updateStats(player, it.oldItem, false)
        updateStats(player, it.item, true)
    }
}

fun checkEmoteUpdate(player: Player) {
    if (player.visuals.flagged(APPEARANCE_MASK)) {
        updateEmote(player)
    }
}

fun updateEmote(player: Player) {
    player.setVar("equipment_emote", player.appearance.emote)
}

fun updateStats(player: Player, id: Int, add: Boolean) {
    val item = definitions.getOrNull(id) ?: return
    names.forEach { (name, key) ->
        val value = item.getInt(key.toLong(), 0)
        if (value > 0) {
            val current = player[name, 0]
            val modified = if (add) {
                current + value
            } else {
                current - value
            }
            player[name] = modified
            sendBonus(player, name, modified)
        }
    }
}

fun sendBonus(player: Player, name: String, value: Int) {
    player.interfaces.sendText("equipment_bonuses", name, "${toDisplayName(name, false)}: ${format(name, value, true)}")
}

fun updateStats(player: Player) {
    names.forEach { (name, _) ->
        player[name] = 0
        sendBonus(player, name, 0)
    }
    player.equipment.getItems().forEach {
        if(it != -1) {
            updateStats(player, it, true)
        }
    }
}

fun toDisplayName(name: String, shorten: Boolean): String {
    var value = name.replace("_def", "").replace("_", " ").toPascalCase()
    if (shorten) {
        value = value.replace("Ranged Strength", "Ranged Str.")
    }
    return value
}

val names = listOf(
    "stab" to 0,
    "slash" to 1,
    "crush" to 2,
    "magic" to 3,
    "range" to 4,
    "stab_def" to 5,
    "slash_def" to 6,
    "crush_def" to 7,
    "magic_def" to 8,
    "ranged_def" to 9,
    "summoning" to 417,
    "absorb_melee" to 967,
    "absorb_magic" to 969,
    "absorb_ranged" to 968,
    "strength" to 641,
    "ranged_strength" to 643,
    "prayer" to 11,
    "magic_damage" to 685
)

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
        types.append(toDisplayName(name, true), if (first) ":                 " else ":", "<br>")
        stats.append(value, "<br>")
        first = false
    }

    fun addValue(index: Int) {
        val (name, key) = names[index]
        val value = getValue(name, item, key)
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
    appendLine("weight", "${df.format(item["weight", 0.0])} kg")

    player.setVar("equipment_titles", titles.toString())
    player.setVar("equipment_names", types.toString())
    player.setVar("equipment_stats", stats.toString())
}

val df = DecimalFormat("0.0").apply {
    roundingMode = RoundingMode.FLOOR
}

fun getValue(name: String, item: ItemDefinition, key: Int): String? {
    val value = item.getInt(key.toLong(), 0)
    if (value == 0) {
        return null
    }

    return format(name, value, false)
}

fun format(name: String, value: Int, bonuses: Boolean): String? {
    return when (name) {
        "magic_damage", "absorb_melee", "absorb_magic", "absorb_ranged" -> "${if (value >= 0) "+" else "-"}${value}%"
        "strength", "ranged_strength" -> "${if (value > 0) "+" else if (value < 0) "-" else ""}${value / 10.0}"
        else -> if(bonuses) "${if (value >= 0) "+" else "-"}${value}" else value.toString()
    }
}
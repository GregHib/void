package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.APPEARANCE_MASK
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
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

on<Registered> { player: Player ->
    updateStats(player)
}

on<ItemChanged>({ container == "worn_equipment" }) { player: Player ->
    updateStats(player, oldItem, false)
    updateStats(player, item, true)
}

on<InterfaceOpened>({ name == "equipment_bonuses" }) { player: Player ->
    player.action(ActionType.Equipping) {
        try {
            player.interfaces.sendVisibility("equipment_bonuses", "close", !player.getVar("equipment_banking", false))
            updateEmote(player)
            player.open("equipment_side")
            player.interfaceOptions.unlockAll("equipment_bonuses", "container", 0 until 16)
            updateStats(player)
            awaitInterface(name)
        } finally {
            player.open("inventory")
            player.close("equipment_bonuses")
        }
    }
}

on<InterfaceOpened>({ name == "equipment_side" }) { player: Player ->
    player.interfaceOptions.send("equipment_side", "container")
    player.interfaceOptions.unlockAll("equipment_side", "container", 0 until 28)
}

on<InterfaceOption>({ it.equipping() && (name == "equipment_side" || name == "equipment_bonuses") && component == "container" && option == "Stats" }) { player: Player ->
    showStats(player, definitions.get(item.name))
}

/*
    Redirect equipping actions to regular containers
 */

on<InterfaceOption>({ it.equipping() && name == "equipment_side" && component == "container" && option == "Equip" }) { player: Player ->
    player.events.emit(ContainerOption("inventory", item, itemIndex, "Wield"))
    checkEmoteUpdate(player)
}

on<InterfaceOption>({ it.equipping() && name == "equipment_bonuses" && component == "container" && option == "Remove" }) { player: Player ->
    player.events.emit(ContainerOption("worn_equipment", item, itemIndex, "Remove"))
    checkEmoteUpdate(player)
}

fun checkEmoteUpdate(player: Player) {
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
    if (player.action.type == ActionType.Equipping) {
        player.interfaces.sendText("equipment_bonuses", key, "$name: ${EquipBonuses.format(key, value, true)}")
    }
}

fun updateStats(player: Player) {
    names.forEach { (name, key) ->
        player[key] = 0
        sendBonus(player, name, key, 0)
    }
    player.equipment.getItems().forEach {
        if (it.isNotEmpty()) {
            updateStats(player, it, true)
        }
    }
}

val names = listOf(
    "Stab" to "stab",
    "Slash" to "slash",
    "Crush" to "crush",
    "Magic" to "magic",
    "Range" to "range",
    "Stab" to "stab_def",
    "Slash" to "slash_def",
    "Crush" to "crush_def",
    "Magic" to "magic_def",
    "Ranged" to "range_def",
    "Summoning" to "summoning_def",
    "Absorb Melee" to "absorb_melee",
    "Absorb Magic" to "absorb_magic",
    "Absorb Ranged" to "absorb_range",
    "Strength" to "str",
    "Ranged Strength" to "range_str",
    "Prayer" to "prayer",
    "Magic Damage" to "magic_damage"
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
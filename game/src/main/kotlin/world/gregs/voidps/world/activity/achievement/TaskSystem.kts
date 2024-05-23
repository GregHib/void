package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject


val variables: VariableDefinitions by inject()
val enumDefinitions: EnumDefinitions by inject()
val structDefinitions: StructDefinitions by inject()

interfaceOpen("task_system") { player ->
    player.sendVariable("task_introducing_explorer_jack")
    player.sendVariable("task_pin_index")
    player.sendVariable("task_pinned")
    refreshSlots(player)
    if (player.contains("task_dont_show_again")) {
        player.sendVariable("task_dont_show_again")
    }
    if (!player.contains("task_progress_total")) {
        player["task_progress_total"] = 0
    } else {
        player.sendVariable("task_progress_total")
    }
}

enterArea("lumbridge") {
    player["task_area"] = "lumbridge_draynor"
    player["task_progress_current"] = 0
    player["task_progress_total"] = 124
}

exitArea("lumbridge") {
    player["task_area"] = "dnd_activities"
}

enterArea("draynor") {
    player["task_area"] = "lumbridge_draynor"
}

exitArea("draynor") {
    player["task_area"] = "dnd_activities"
}

interfaceOption("Close", "close_hint", "task_system") {
    player.interfaces.sendVisibility(id, "message_overlay", false)
}

interfaceOption("Select Task", "task_*", "task_system") {
    val index = component.removePrefix("task_").toInt()
    player["selected_task"] = index
    player["task_pins"] = index
}

interfaceOption("Toggle", "dont_show", "task_system") {
    player["task_dont_show_again"] = !player["task_dont_show_again", false]
}

interfaceOption("Open", "task_list", "task_system") {
    player.open("task_list")
}

interfaceOption("OK", "ok", "task_system") {
    player.interfaces.sendVisibility("task_system", "summary_overlay", false)
    refreshSlots(player)
}

interfaceOption("Pin/Unpin Task", "task_*", "task_system") {
    val index = component.removePrefix("task_").toInt()
    if (player["task_pin_index", -1] == index) {
        player.clear("task_pinned")
        player.clear("task_pin_index")
    } else {
        player["task_pinned"] = index(player, index) ?: return@interfaceOption
        player["task_pin_index"] = index
    }
}

fun index(player: Player, index: Int, areaId: Int = areaId(player)): Int? {
    var next = enumDefinitions.get("task_area_start_indices").getInt(areaId)
    var count = 1
    while (next != -1) {
        val struct = enumDefinitions.get("task_structs").getInt(next)
        val definition = structDefinitions.getOrNull(struct) ?: break
        if (player["task_hide_completed", false] && isCompleted(player, definition.stringId) || definition["task_members", 0] == 1 && !World.members) {
            count++
            continue
        }
        if (count == index) {
            return next
        }
        if (player["task_pin_index", -1] == count) {
            count++
            continue
        }
        count++
        next = definition["task_next_index", -1]
    }
    return null
}

variableSet("task_pin_index", "task_area") { player ->
    refreshSlots(player)
}

variableSet("*_task", to = true) { player ->
    refreshSlots(player)
}

fun refreshSlots(player: Player) {
    val areaId = areaId(player)
    var next = enumDefinitions.get("task_area_start_indices").getInt(areaId)
    var i = 1
    while (i < 7 && next != 4091) {
        val struct = enumDefinitions.get("task_structs").getInt(next)
        val definition = structDefinitions.getOrNull(struct) ?: break
        val pinned = pinned(player, i)
        if (player["task_pinned", -1] == next && !pinned || !Tasks.hasRequirements(player, definition) || isCompleted(player, definition.stringId)) {
            next = definition["task_next_index", 4091]
            continue
        }
        if (pinned) {
            player["task_slot_${i++}"] = player["task_pinned", 4091]
        } else {
            player["task_slot_${i++}"] = next
            next = definition["task_next_index", 4091]
        }
    }
}

fun pinned(player: Player, index: Int): Boolean {
    val pinIndex = player["task_pin_index", -1]
    return pinIndex != -1 && index == pinIndex
}

fun areaId(player: Player) = variables.get("task_area")!!.values.toInt(player["task_area", "empty"])

fun isCompleted(player: Player, id: String) = player[id, false]
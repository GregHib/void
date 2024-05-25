package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
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
import world.gregs.voidps.world.interact.entity.player.display.Tab

val variables: VariableDefinitions by inject()
val enumDefinitions: EnumDefinitions by inject()
val structDefinitions: StructDefinitions by inject()

interfaceOpen("task_system") { player ->
    player.sendVariable("task_pin_index")
    player.sendVariable("task_pinned")
    player.sendVariable("introducing_explorer_jack_task")
    refreshSlots(player)
    if (player.contains("task_dont_show_again")) {
        player.sendVariable("task_dont_show_again")
    }
    if (player["unstable_foundations", "unstarted"] == "unstarted") {
        player["task_pinned"] = 3520 // Talk to explorer jack
        player["task_pin_index"] = 1
        player["task_selected"] = 1
        player["unstable_foundations"] = "incomplete"
    }
}

enterArea("lumbridge") {
    player["task_area"] = "lumbridge_draynor"
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
    player["task_selected"] = index
}

interfaceOption("Toggle", "dont_show", "task_system") {
    player["task_dont_show_again"] = !player["task_dont_show_again", false]
}

interfaceOption("Open", "task_list", "task_system") {
    player.open("task_list")
}

interfaceOption("OK", "ok", "task_system") {
    player.interfaces.sendVisibility("task_system", "summary_overlay", false)
    val selected = player["task_selected", -1]
    if (selected != -1 && selected == player["task_pin_index", -1]) {
        player.clear("task_pinned")
        player.clear("task_pin_index")
        player.interfaces.sendVisibility("task_system", "ok", false)
    }
    refreshSlots(player)
}

interfaceOption("Pin/Unpin Task", "task_*", "task_system") {
    val index = component.removePrefix("task_").toInt()
    if (player["task_pin_index", -1] == index) {
        player.clear("task_pinned")
        player.clear("task_pin_index")
    } else {
        player["task_pinned"] = indexOfSlot(player, index) ?: return@interfaceOption
        player["task_pin_index"] = index
    }
}

fun indexOfSlot(player: Player, slot: Int): Int? {
    var count = 1
    return Tasks.forEach(areaId(player)) {
        count++
        val hideCompleted = player["task_hide_completed", false] && Tasks.isCompleted(player, definition.stringId)
        val hideMembers = definition["task_members", 0] == 1 && !World.members
        if (hideCompleted || hideMembers) {
            return@forEach null
        }
        if (count - 1 == slot) {
            return@forEach this.index
        }
        val skipPinned = player["task_pin_index", -1] == count
        if (skipPinned) {
            skip = true
        }
        null
    }
}

variableSet("task_pin_index", "task_area") { player ->
    refreshSlots(player)
}

fun refreshSlots(player: Player) {
    var slot = 1
    var completed = 0
    var total = 0
    Tasks.forEach(areaId(player)) {
        total++
        val pinned = pinned(player, slot)
        if (player["task_pinned", -1] == index && !pinned || !Tasks.hasRequirements(player, definition)) {
            return@forEach null
        }
        if (Tasks.isCompleted(player, definition.stringId)) {
            completed++
            return@forEach null
        }
        if (pinned) {
            player["task_slot_${slot++}"] = player["task_pinned", 4091]
            skip = true
        } else if (slot < 7) {
            player["task_slot_${slot++}"] = index
        }
        null
    }
    player["task_progress_total"] = total
    player["task_progress_current"] = completed
}

fun pinned(player: Player, index: Int): Boolean {
    val pinIndex = player["task_pin_index", -1]
    return pinIndex != -1 && index == pinIndex
}

fun areaId(player: Player) = variables.get("task_area")!!.values.toInt(player["task_area", "empty"])

/*
    Task completion
 */

interfaceOption("Details", "details", "task_popup") {
    player["task_popup_summary"] = true
    player["tab"] = Tab.TaskSystem.name
}

variableSet("*_task") { player ->
    if (to == true || to == "completed") {
        completeTask(player, key)
    }
}

fun completeTask(player: Player, id: String) {
    val definition = structDefinitions.get(id)
    val index = definition["task_index", -1]
    player["task_popup"] = index
    val difficulty = definition["task_difficulty", 0]
    val area = definition["task_area", 61]
    val areaName = enumDefinitions.get("task_area_names").getString(area)
    val difficultyName = enumDefinitions.get("task_difficulties").getString(difficulty)
    if (areaName.isNotBlank() && difficultyName.isNotBlank()) {
        player.message("You have completed the Task '${definition["task_name", ""]}' in the $difficultyName $areaName set!")
    } else {
        player.message("You have completed the Task '${definition["task_name", ""]}'!")
    }
    val before = player["task_progress_current", 0]
    refreshSlots(player)
    val total = player.inc("task_progress_overall")
    player.message("You have now completed $total ${"Task".plural(total)} in total.")
    val after = player["task_progress_current", 0]
    val maximum = player["task_progress_total", -1]
    if (before != after && after == maximum) {
        val prettyName = when (area) {
            1 -> "Lumbridge and Draynor"
            else -> areaName
        }
        player.message("Congratulations! You have completed all of the $difficultyName Tasks in the $prettyName")
        val npc = when {
            area == 1 && difficulty == 1 -> "Explorer Jack in Lumbridge"
            area == 1 && difficulty == 2 -> "Bob in Bob's Axes in Lumbridge"
            area == 1 && difficulty == 3 -> "Ned in Draynor Village"
            else -> "someone somewhere"
        }
        player.message("set. Speak to $npc to claim your reward.")
    }
}
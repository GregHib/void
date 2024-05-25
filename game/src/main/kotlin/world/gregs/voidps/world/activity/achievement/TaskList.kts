package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inject

val variables: VariableDefinitions by inject()

playerSpawn { player ->
    player.sendVariable("task_disable_popups")
    player["task_popup"] = 0
    player["task_previous_popup"] = 0
}

interfaceOpen("task_list") { player ->
    refresh(player)
    player.sendVariable("task_progress_overall")
    player.sendVariable("task_hide_completed")
    player.sendVariable("task_filter_sets")
}

interfaceOption("Select", "area_*", "task_list") {
    player["task_list_area"] = component.removePrefix("area_")
    refresh(player)
}

interfaceOption("Summary", "tasks", "task_list") {
    player["selected_task"] = itemSlot / 4
}

interfaceOption("Pin", "tasks", "task_list") {
    pin(player, itemSlot / 4)
}

interfaceOption("Pin", "pin", "task_list") {
    pin(player, player["selected_task", 0])
}

fun indexOfSlot(player: Player, slot: Int): Int? {
    var count = 0
    return Tasks.forEach(areaId(player)) {
        count++
        val incomplete = !player["task_hide_completed", false] || !isCompleted(player, definition.stringId)
        if (incomplete && count - 1 == slot) {
            return@forEach index
        }
        null
    }
}

fun isCompleted(player: Player, id: String) = player.contains(id) && player[id, false]

fun find(player: Player, id: Int): Int {
    for (i in 0 until 6) {
        if (player["task_slot_${i}", -1] == id) {
            return i
        }
    }
    return 1
}

interfaceOption("Filter-sets", "filter_sets", "task_list") {
    player["task_filter_sets"] = !player["task_filter_sets", false]
}

interfaceOption("Filter-done", "filter_done", "task_list") {
    player["task_hide_completed"] = !player["task_hide_completed", false]
}

interfaceOption("Turn-off", "toggle_popups", "task_list") {
    val disable = !player["task_disable_popups", false]
    player["task_disable_popups"] = disable
    if (disable) {
        player["task_popup"] = 0
        player["task_previous_popup"] = 0
    }
}

fun refresh(player: Player) {
    player.sendVariable("task_list_area")
    val id = areaId(player)
    player.sendScript("task_main_list_populate", id, 999, 999)
    player.interfaceOptions.unlockAll("task_list", "tasks", 0..100)
    refreshCompletedCount(player)
}

variableSet("task_pin_index") { player ->
    player.close("task_list")
}

fun areaId(player: Player) = variables.get("task_list_area")!!.values.toInt(player["task_list_area", "unstable_foundations"])

fun pin(player: Player, index: Int) {
    val id = indexOfSlot(player, index) ?: return
    if (player["task_pinned", -1] == id) {
        player.clear("task_pinned")
        player.clear("task_pin_index")
    } else {
        player["task_pinned"] = id
        player["task_pin_index"] = find(player, id)
    }
}

fun refreshCompletedCount(player: Player) {
    var total = 0
    var completed = 0
    Tasks.forEach(areaId(player)) {
        if (isCompleted(player, definition.stringId)) {
            completed++
        }
        total++
    }
    player["task_progress_current"] = completed
    player["task_progress_total"] = total
}
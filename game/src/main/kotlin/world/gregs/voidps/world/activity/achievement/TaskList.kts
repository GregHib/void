package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
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

val enumDefinitions: EnumDefinitions by inject()
val structDefinitions: StructDefinitions by inject()

fun index(player: Player, index: Int, area: Int = areaId(player)): Int? {
    var next = enumDefinitions.get("task_area_start_indices").getInt(area)
    var count = 0
    while (next != -1) {
        val struct = enumDefinitions.get("task_structs").getInt(next)
        val definition = structDefinitions.getOrNull(struct) ?: break
        if (player["task_hide_completed", false] && isCompleted(player, definition.stringId)) {
            count++
            continue
        }
        if (count++ == index) {
            return next
        }
        next = definition["task_next_index", -1]
    }
    return null
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
        player.set("task_popup", 0)
        player.set("task_previous_popup", 0)
    }
}

fun refresh(player: Player) {
    player.sendVariable("task_list_area")
    val id = areaId(player)
    player.sendScript("task_main_list_populate", id, 999, 999)
    player.interfaceOptions.unlockAll("task_list", "tasks", 0..100)
    count(player)
}

variableSet("task_pin_index") { player ->
    player.close("task_list")
}

fun areaId(player: Player) = variables.get("task_list_area")!!.values.toInt(player["task_list_area", "unstable_foundations"])

fun pin(player: Player, index: Int) {
    val id = index(player, index) ?: return
    if (player["task_pinned", -1] == id) {
        player.clear("task_pinned")
        player.clear("task_pin_index")
    } else {
        player["task_pinned"] = id
        player["task_pin_index"] = find(player, id)
    }
}

fun count(player: Player) {
    val area = areaId(player)
    var next = enumDefinitions.get("task_area_start_indices").getInt(area)
    var total = 0
    var completed = 0
    val structs = enumDefinitions.get("task_structs")
    while (next != -1 && next != 450) {
        val struct = structs.getInt(next)
        val definition = structDefinitions.getOrNull(struct) ?: break
        if (isCompleted(player, definition.stringId)) {
            completed++
        }
        total++
        next = definition["task_next_index", -1]
    }
    player["task_progress_current"] = completed
    player["task_progress_total"] = total
}
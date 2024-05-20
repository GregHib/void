package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject

val variables: VariableDefinitions by inject()

interfaceOpen("task_list") { player ->
    refresh(player)
    player.sendVariable("task_progress_overall")
    player.sendVariable("task_hide_completed")
    player.sendVariable("task_filter_sets")
    player.sendVariable("task_disable_popups")
}

interfaceOption("Select", "area_*", "task_list") {
    player["task_list_area"] = component.removePrefix("area_")
    refresh(player)
}

/*
    dnds=3002..3034
    unstable foundations=3500..3522
    varrock=256..345

 */
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

fun pin(player: Player, id: Int) {
    val areaId = areaId(player)
    var start = enumDefinitions.get("task_area_start_indices").getInt(areaId)
    var count = 0
    var taskIndex = -1
    while (start != -1) {
        val struct = enumDefinitions.get("task_structs").getInt(start)
        val definition = structDefinitions.getOrNull(struct) ?: break
        start = definition["task_next_index", -1]
        if (definition["task_members", 0] == 1 && !World.members) { // TODO test if members tasks are displayed for f2p or not
            count++
            continue
        }
        if (count++ == id) {
            taskIndex = definition["task_index", -1]
            break
        }
    }
    if (taskIndex == -1) {
        return // Task not found
    }
    var index = -1
    for (i in 1..6) {
        if (!player.containsVarbit("task_pins", i)) {
            index = i
            break
        }
        if (player["task_pin_${i}", -1] == taskIndex) {
            return // Already pinned
        }
    }

    if (index == -1) {
        return // Too many pins
    }
    player.addVarbit("task_pins", index)
    player["task_pin_${index}"] = taskIndex
}

interfaceOption("Filter-sets", "filter_sets", "task_list") {
    player["task_filter_sets"] = !player["task_filter_sets", false]
}

interfaceOption("Filter-done", "filter_done", "task_list") {
    player["task_hide_completed"] = !player["task_hide_completed", false]
}

interfaceOption("Turn-off", "toggle_popups", "task_list") {
    player["task_disable_popups"] = !player["task_disable_popups", false]
}

fun refresh(player: Player) {
    player.sendVariable("task_list_area")
    val int = areaId(player)
    player.sendScript("task_main_list_populate", int, 999, 999)
    player.interfaceOptions.unlockAll("task_list", "tasks", 0..100)
}

fun areaId(player: Player) = variables.get("task_list_area")!!.values.toInt(player["task_list_area", "dnd_activities"])
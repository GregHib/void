@file:Suppress("UnusedReceiverParameter")

package content.achievement

import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.event.handle.Variable
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.event.handle.On
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.handle.Option
import world.gregs.voidps.engine.inject

private val variables: VariableDefinitions by inject()

@On
fun Spawn.loginTasks(player: Player) {
    player.sendVariable("task_disable_popups")
    player["task_popup"] = 0
    player["task_previous_popup"] = 0
    var total = 0
    for (area in 0 until 8) {
        Tasks.forEach(area) {
            if (Tasks.isCompleted(player, definition.stringId)) {
                player.sendVariable(definition.stringId)
                total++
            }
            null
        }
    }
    player["task_progress_overall"] = total
    player.sendVariable("task_hide_completed")
    player.sendVariable("task_filter_sets")
}

@On("task_list")
fun InterfaceOpened.openTaskList(player: Player) {
    player.interfaceOptions.unlockAll("task_list", "tasks", 0..492)
    refresh(player)
}

@Option("Select", "area_*:task_list")
fun InterfaceOption.selectTaskArea() {
    player["task_list_area"] = component.removePrefix("area_")
    refresh(player)
}

@Option("Summary", "tasks:task_list")
fun InterfaceOption.taskSummary() {
    player["task_slot_selected"] = itemSlot / 4
}

@Option("Pin", "tasks:task_list")
fun InterfaceOption.pinTask() {
    pin(player, itemSlot / 4)
}

@Option("Pin", "pin:task_list")
fun InterfaceOption.pinSelected() {
    pin(player, player["task_slot_selected", 0])
}

private fun indexOfSlot(player: Player, slot: Int): Int? {
    var count = 0
    return Tasks.forEach(areaId(player)) {
        if (player["task_hide_completed", false] && Tasks.isCompleted(player, definition.stringId)) {
            return@forEach null
        }
        if (player["task_filter_sets", false] && !definition.contains("task_sprite_offset")) {
            return@forEach null
        }
        if (count++ == slot) {
            return@forEach index
        }
        null
    }
}

private fun find(player: Player, id: Int): Int {
    for (i in 0 until 6) {
        if (player["task_slot_$i", -1] == id) {
            return i
        }
    }
    return 1
}

@Option("Filter-sets", "filter_sets:task_list")
fun InterfaceOption.filterTaskSets() {
    player["task_filter_sets"] = !player["task_filter_sets", false]
}

@Option("Filter-done", "filter_done:task_list")
fun InterfaceOption.filterDoneTasks() {
    player["task_hide_completed"] = !player["task_hide_completed", false]
}

@Option("Turn-off", "toggle_popups:task_list")
fun InterfaceOption.toggleTaskPopups() {
    val disable = !player["task_disable_popups", false]
    player["task_disable_popups"] = disable
    if (disable) {
        player["task_popup"] = 0
        player["task_previous_popup"] = 0
    }
}

private fun refresh(player: Player) {
    player.sendVariable("task_list_area")
    val id = areaId(player)
    player.sendScript("task_main_list_populate", id, 999, 999)
    refreshCompletedCount(player)
}

@Variable("task_pin_slot")
fun VariableSet.taskPinChanged(player: Player) {
    player.close("task_list")
}

private fun areaId(player: Player) = variables.get("task_list_area")!!.values.toInt(player["task_list_area", "unstable_foundations"])

private fun pin(player: Player, slot: Int) {
    val index = indexOfSlot(player, slot) ?: return
    if (player["task_pinned", -1] == index) {
        player.clear("task_pinned")
        player.clear("task_pin_slot")
    } else {
        player["task_pinned"] = index
        player["task_pin_slot"] = find(player, index)
    }
}

private fun refreshCompletedCount(player: Player) {
    var total = 0
    var completed = 0
    Tasks.forEach(areaId(player)) {
        if (Tasks.isCompleted(player, definition.stringId)) {
            completed++
            player.sendVariable(definition.stringId)
        }
        total++
        null
    }
    player["task_progress_current"] = completed
    player["task_progress_total"] = total
}

/*
    Hints
 */

val enumDefinitions: EnumDefinitions by inject()

@Option("Hint", "hint_*:task_list")
fun InterfaceOption.showTaskHint() {
    val selected = player["task_slot_selected", 0]
    val index = indexOfSlot(player, selected) ?: return
    val tile: Int = enumDefinitions.getStructOrNull("task_structs", index, component.replace("hint_", "task_hint_tile_")) ?: return
    // TODO I expect the functionality is actually minimap highlights not world map
    player["world_map_marker_1"] = tile
    player["world_map_marker_text_1"] = ""
    player.open("world_map")
}
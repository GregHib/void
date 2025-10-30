package content.achievement

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject

class TaskList : Script {

    val variables: VariableDefinitions by inject()

    val enumDefinitions: EnumDefinitions by inject()

    init {
        playerSpawn {
            sendVariable("task_disable_popups")
            set("task_popup", 0)
            set("task_previous_popup", 0)
            var total = 0
            for (area in 0 until 8) {
                Tasks.forEach(area) {
                    if (Tasks.isCompleted(this@playerSpawn, definition.stringId)) {
                        sendVariable(definition.stringId)
                        total++
                    }
                    null
                }
            }
            set("task_progress_overall", total)
            sendVariable("task_hide_completed")
            sendVariable("task_filter_sets")
        }

        interfaceOpen("task_list") { player ->
            player.interfaceOptions.unlockAll("task_list", "tasks", 0..492)
            refresh(player)
        }

        variableSet("task_pin_slot") { player, _, _, _ ->
            player.close("task_list")
        }

        interfaceOption("Select", "area_*", "task_list") {
            player["task_list_area"] = component.removePrefix("area_")
            refresh(player)
        }

        interfaceOption("Summary", "tasks", "task_list") {
            player["task_slot_selected"] = itemSlot / 4
        }

        interfaceOption("Pin", "tasks", "task_list") {
            pin(player, itemSlot / 4)
        }

        interfaceOption("Pin", "pin", "task_list") {
            pin(player, player["task_slot_selected", 0])
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

        interfaceOption("Hint", "hint_*", "task_list") {
            val selected = player["task_slot_selected", 0]
            val index = indexOfSlot(player, selected) ?: return@interfaceOption
            val tile: Int = enumDefinitions.getStructOrNull("task_structs", index, component.replace("hint_", "task_hint_tile_")) ?: return@interfaceOption
            // TODO I expect the functionality is actually minimap highlights not world map
            player["world_map_marker_1"] = tile
            player["world_map_marker_text_1"] = ""
            player.open("world_map")
        }
    }

    fun indexOfSlot(player: Player, slot: Int): Int? {
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

    fun find(player: Player, id: Int): Int {
        for (i in 0 until 6) {
            if (player["task_slot_$i", -1] == id) {
                return i
            }
        }
        return 1
    }

    fun refresh(player: Player) {
        player.sendVariable("task_list_area")
        val id = areaId(player)
        player.sendScript("task_main_list_populate", id, 999, 999)
        refreshCompletedCount(player)
    }

    fun areaId(player: Player) = variables.get("task_list_area")!!.values.toInt(player["task_list_area", "unstable_foundations"])

    fun pin(player: Player, slot: Int) {
        val index = indexOfSlot(player, slot) ?: return
        if (player["task_pinned", -1] == index) {
            player.clear("task_pinned")
            player.clear("task_pin_slot")
        } else {
            player["task_pinned"] = index
            player["task_pin_slot"] = find(player, index)
        }
    }

    fun refreshCompletedCount(player: Player) {
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
}

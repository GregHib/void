package content.achievement

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
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

        interfaceOpened("task_list") {
            interfaceOptions.unlockAll("task_list", "tasks", 0..492)
            refresh(this)
        }

        variableSet("task_pin_slot") { _, _, _ ->
            close("task_list")
        }

        interfaceOption("Select", "task_list:area_*") {
            set("task_list_area", it.component.removePrefix("area_"))
            refresh(this)
        }

        interfaceOption("Summary", "task_list:tasks") { (_, itemSlot) ->
            set("task_slot_selected", itemSlot / 4)
        }

        interfaceOption("Pin", "task_list:tasks") { (_, itemSlot) ->
            pin(this, itemSlot / 4)
        }

        interfaceOption("Pin", "task_list:pin") {
            pin(this, get("task_slot_selected", 0))
        }

        interfaceOption("Filter-sets", "task_list:filter_sets") {
            set("task_filter_sets", !get("task_filter_sets", false))
        }

        interfaceOption("Filter-done", "task_list:filter_done") {
            set("task_hide_completed", !get("task_hide_completed", false))
        }

        interfaceOption("Turn-off", "task_list:toggle_popups") {
            val disable = !get("task_disable_popups", false)
            set("task_disable_popups", disable)
            if (disable) {
                set("task_popup", 0)
                set("task_previous_popup", 0)
            }
        }

        interfaceOption("Hint", "task_list:hint_*") {
            val selected = get("task_slot_selected", 0)
            val index = indexOfSlot(this, selected) ?: return@interfaceOption
            val tile: Int = enumDefinitions.getStructOrNull("task_structs", index, it.component.replace("hint_", "task_hint_tile_")) ?: return@interfaceOption
            // TODO I expect the functionality is actually minimap highlights not world map
            set("world_map_marker_1", tile)
            set("world_map_marker_text_1", "")
            open("world_map")
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

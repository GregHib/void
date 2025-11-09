package content.achievement

import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inject

class TaskSystem : Script {

    val variables: VariableDefinitions by inject()
    val enumDefinitions: EnumDefinitions by inject()
    val structDefinitions: StructDefinitions by inject()

    init {
        interfaceOpened("task_system") {
            sendVariable("task_pin_slot")
            sendVariable("task_pinned")
            sendVariable("introducing_explorer_jack_task")
            refreshSlots(this)
            if (contains("task_dont_show_again")) {
                sendVariable("task_dont_show_again")
            }
            if (!questCompleted("unstable_foundations")) {
                set("task_pinned", 3520) // Talk to explorer jack
                set("task_pin_slot", 1)
                set("task_slot_selected", 1)
                set("unstable_foundations", "incomplete")
            }
        }

        entered("lumbridge") {
            set("task_area", "lumbridge_draynor")
        }

        exited("lumbridge") {
            set("task_area", "dnd_activities")
        }

        entered("draynor") {
            set("task_area", "lumbridge_draynor")
        }

        exited("draynor") {
            set("task_area", "dnd_activities")
        }

        interfaceOption("Close", "task_system:close_hint") {
            interfaces.sendVisibility("task_system", "message_overlay", false)
        }

        interfaceOption("Select Task", "task_system:task_*") {
            val slot = it.component.removePrefix("task_").toInt()
            set("task_slot_selected", slot)
        }

        interfaceOption("Toggle", "task_system:dont_show") {
            set("task_dont_show_again", !get("task_dont_show_again", false))
        }

        interfaceOption("Open", "task_system:task_list") {
            open("task_list")
        }

        interfaceOption("OK", "task_system:ok") {
            interfaces.sendVisibility("task_system", "summary_overlay", false)
            val slot = get("task_slot_selected", 0)
            val selected = indexOfSlot(this, slot) ?: return@interfaceOption
            if (selected == get("task_pinned", -1)) {
                clear("task_pinned")
                clear("task_pin_slot")
            }
            interfaces.sendVisibility("task_system", "ok", false)
            refreshSlots(this)
        }

        interfaceOption("Pin/Unpin Task", "task_system:task_*") {
            val index = it.component.removePrefix("task_").toInt()
            pin(this, index)
        }

        interfaceOption("Set", "task_system:pin") {
            val slot = get<Int>("task_slot_selected") ?: return@interfaceOption
            pin(this, slot)
            interfaces.sendVisibility("task_system", "summary_overlay", false)
        }

        interfaceOption("Details", "task_popup:details") {
            if (questCompleted("unstable_foundations")) {
                set("task_popup_summary", true)
                interfaces.sendVisibility("task_system", "ok", true)
                val index = get("task_popup", -1)
                for (slot in 0 until 6) {
                    if (get("task_slot_$slot", -1) == index) {
                        set("task_slot_selected", slot)
                        break
                    }
                }
            }
            tab(Tab.TaskSystem)
        }

        interfaceOption("Hint", "task_system:hint_*") {
            val selected = get("task_slot_selected", 0)
            val index = indexOfSlot(this, selected) ?: return@interfaceOption
            val tile: Int = enumDefinitions.getStructOrNull("task_structs", index, it.component.replace("hint_", "task_hint_tile_")) ?: return@interfaceOption
            // TODO I expect the functionality is actually minimap highlights not world map
            set("world_map_marker_1", tile)
            set("world_map_marker_text_1", "")
            open("world_map")
        }

        variableSet("task_pin_slot,task_area,*_task") { key, _, to ->
            if (key == "task_pin_slot" || key == "task_area") {
                refreshSlots(this)
            } else if (key.endsWith("_task") && (to == true || to == "completed")) {
                completeTask(this, key)
            }
        }
    }

    fun pin(player: Player, slot: Int) {
        if (player["task_pin_slot", -1] == slot) {
            player.clear("task_pinned")
            player.clear("task_pin_slot")
        } else {
            player["task_pinned"] = indexOfSlot(player, slot) ?: return
            player["task_pin_slot"] = slot
        }
    }

    fun indexOfSlot(player: Player, slot: Int): Int? {
        var count = 1
        return Tasks.forEach(areaId(player)) {
            val hideCompleted = Tasks.isCompleted(player, definition.stringId)
            val hideMembers = definition["task_members", 0] == 1 && !World.members
            if (hideCompleted || hideMembers) {
                return@forEach null
            }
            if (count == player["task_pin_slot", -1]) {
                val pinned = player["task_pinned", 4091]
                if (count == slot) {
                    return@forEach pinned
                }
                skip = pinned != index
            }
            if (count++ == slot) {
                return@forEach index
            }
            null
        }
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
                total--
                skip = true
            } else if (slot < 7) {
                player["task_slot_${slot++}"] = index
            }
            null
        }
        if (slot < 7) {
            for (i in slot..6) {
                player["task_slot_$i"] = 4091
            }
        }
        player["task_progress_total"] = total
        player["task_progress_current"] = completed
    }

    fun pinned(player: Player, slot: Int): Boolean {
        val pinned = player["task_pin_slot", -1]
        return pinned != -1 && slot == pinned
    }

    fun areaId(player: Player) = variables.get("task_area")!!.values.toInt(player["task_area", "empty"])

    /*
        Task completion
     */
    fun completeTask(player: Player, id: String) {
        val definition = structDefinitions.get(id)
        AuditLog.event(player, "task_completed", id)
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

    /*
        Hints
     */
}

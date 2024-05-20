package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea

interfaceOpen("task_system") { player ->
    for (i in 1..6) {
        if(player.contains("task_pin_$i")) {
            player.sendVariable("task_pin_${i}")
        } else {
            player["task_pin_${i}"] = 4091
        }
    }
    if (player.contains("task_dont_show_again")) {
        player.sendVariable("task_dont_show_again")
    }
    if (!player.contains("task_progress_total")) {
        player["task_progress_total"] = 0
    } else {
        player.sendVariable("task_progress_total")
    }
    player.sendVariable("task_pins")
}

enterArea("lumbridge") {
    player["task_area"] = "lumbridge_draynor"
    player["task_progress_current"] = 0
    player["task_progress_total"] = 124
}

exitArea("lumbridge") {
    player["task_area"] = "empty"
}

enterArea("draynor") {
    player["task_area"] = "lumbridge_draynor"
}

exitArea("draynor") {
    player["task_area"] = "empty"
}

interfaceOption("Task System", "task_system", "toplevel*") {
    // Hacky - It shouldn't be open to begin with
    player.interfaces.sendVisibility("task_system", "summary_overlay", false)
}

interfaceOption("Close", "close_hint", "task_system") {
    player.interfaces.sendVisibility(id, "message_overlay", false)
}

interfaceOption("Toggle", "dont_show", "task_system") {
    player["task_dont_show_again"] = !player["task_dont_show_again", false]
}

interfaceOption("Open", "task_list", "task_system") {
    player.open("task_list")
}
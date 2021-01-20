package world.gregs.void.world.activity.achievement

import world.gregs.void.engine.client.ui.open
import world.gregs.void.engine.event.on
import world.gregs.void.engine.event.then
import world.gregs.void.world.interact.entity.player.display.InterfaceOption

on(InterfaceOption) {
    where {
        name == "task_system" && component == "task_list" && option == "Open"
    }
    then {
        player.open("task_list")
    }
}
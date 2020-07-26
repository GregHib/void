package rs.dusk.world.activity.achievement

import rs.dusk.engine.client.ui.event.InterfaceInteraction
import rs.dusk.engine.client.ui.open
import rs.dusk.engine.event.on
import rs.dusk.engine.event.then

on(InterfaceInteraction) {
    where {
        name == "task_system" && component == "task_list" && option == "Open"
    }
    then {
        player.open("task_list")
    }
}
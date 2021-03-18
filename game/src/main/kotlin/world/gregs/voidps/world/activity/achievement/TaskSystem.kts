package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where

InterfaceOption where { name == "task_system" && component == "task_list" && option == "Open" } then {
    player.open("task_list")
}

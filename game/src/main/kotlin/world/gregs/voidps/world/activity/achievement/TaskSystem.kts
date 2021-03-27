package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ name == "task_system" && component == "task_list" && option == "Open" }) { player: Player ->
    player.open("task_list")
}

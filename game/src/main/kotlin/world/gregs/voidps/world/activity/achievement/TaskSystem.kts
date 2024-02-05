package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player

interfaceOption({ id == "task_system" && component == "task_list" && option == "Open" }) { player: Player ->
    player.open("task_list")
}

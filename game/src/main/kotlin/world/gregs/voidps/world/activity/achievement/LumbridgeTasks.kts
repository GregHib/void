package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.move.move
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.playerSpawn

playerSpawn { player ->
    player.open("task_popup")
}

move({ player.visuals.runStep != -1 && player.running && !player["on_the_run_task", false] }) {
    player["on_the_run_task"] = true
}
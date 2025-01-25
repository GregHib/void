package content.entity.player.command.admin

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject

val objects: GameObjects by inject()

adminCommand("get", "get all objects under the player") {
    objects[player.tile].forEach {
        println(it)
    }
}
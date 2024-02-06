package world.gregs.voidps.world.command.admin

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject

val objects: GameObjects by inject()

adminCommand("get") {
    objects[player.tile].forEach {
        println(it)
    }
}
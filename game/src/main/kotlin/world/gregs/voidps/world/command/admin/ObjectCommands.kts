package world.gregs.voidps.world.command.admin

import world.gregs.voidps.engine.client.ui.event.command
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject

val objects: GameObjects by inject()

command({ prefix == "get" }) { player: Player ->
    objects[player.tile].forEach {
        println(it)
    }
}
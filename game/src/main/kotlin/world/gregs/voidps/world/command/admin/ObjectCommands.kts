package world.gregs.voidps.world.command.admin

import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject

val objects: GameObjects by inject()

on<Command>({ prefix == "get" }) { player: Player ->
    objects[player.tile].forEach {
        println(it)
    }
}
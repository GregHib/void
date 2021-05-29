import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.utility.inject

val objects: Objects by inject()

on<Command>({ prefix == "get" }) { player: Player ->
    val obj = objects[player.tile.chunk]
    obj.filter { it.tile == player.tile }.forEach {
        println(it)
    }
}
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.Command

val objects: Objects by inject()

on<Command>({ prefix == "get" }) { player: Player ->
    val obj = objects[player.tile.chunk]
    obj.filter { it.tile == player.tile }.forEach {
        println(it)
    }
}
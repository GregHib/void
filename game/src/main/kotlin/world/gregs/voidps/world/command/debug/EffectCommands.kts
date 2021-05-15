import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command

on<Command>({ prefix == "effects" }) { player: Player ->
    for ((key, value) in player.values) {
        if (key.endsWith("_effect")) {
            player.message("$key - $value")
        }
    }
    for ((key, value) in player.values.temporary) {
        if (key.endsWith("_effect")) {
            player.message("$key - $value")
        }
    }
}

on<Command>({ prefix == "stop-effect" }) { player: Player ->
    player.stop(content)
}
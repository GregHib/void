import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command

on<Command>({ prefix == "effects" }) { player: Player ->
    player.message("-- Effects --", ChatType.Console)
    for (key in player.values.keys) {
        if (key.endsWith("_effect")) {
            player.message(key.removeSuffix("_effect"), ChatType.Console)
        }
    }
    for (key in player.values.temporary.keys) {
        if (key.endsWith("_effect")) {
            player.message(key.removeSuffix("_effect"), ChatType.Console)
        }
    }
}

on<Command>({ prefix == "stop" }) { player: Player ->
    player.stop(content)
}
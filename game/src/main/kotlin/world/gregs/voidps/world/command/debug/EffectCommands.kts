package world.gregs.voidps.world.command.debug

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on

on<Command>({ prefix == "effects" }) { player: Player ->
    for (key in player.values?.keys ?: emptySet()) {
        if (key.endsWith("_effect")) {
            player.message(key.removeSuffix("_effect"), ChatType.Console)
        }
    }
    for (key in player.values?.temporary?.keys ?: emptySet()) {
        if (key.endsWith("_effect")) {
            player.message(key.removeSuffix("_effect"), ChatType.Console)
        }
    }
}

on<Command>({ prefix == "start" }) { player: Player ->
    player.start(content, restart = true)
}

on<Command>({ prefix == "stop" }) { player: Player ->
    player.stop(content)
}
import world.gregs.voidps.engine.client.compress
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PublicMessage
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.publicChat

on<PublicMessage> { player: Player ->
    val text = message.compress()
    player.viewport.players.current.forEach {
        it.client?.publicChat(text, player.index, effects, player.rights.ordinal)
    }
}
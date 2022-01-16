import world.gregs.voidps.engine.client.compress
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.PrivateMessage
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.privateChatFrom
import world.gregs.voidps.network.encode.privateChatTo

val players: Players by inject()

on<PrivateMessage> { player: Player ->
    val target = players.get(friend)
    if (target == null) {
        player.message("Unable to send message - player unavailable.")
        return@on
    }
    val text = message.compress()
    player.client?.privateChatTo(target.name, text)
    target.client?.privateChatFrom(player.name, player.rights.ordinal, text)
}
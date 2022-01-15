import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.privateChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.PrivateMessage
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject

val players: Players by inject()

on<PrivateMessage> { player: Player ->
    val target = players.get(friend)
    if (target == null) {
        player.message("Error sending message.")
        return@on
    }
    target.privateChat(player.name, player.name, 0, message)
}
import world.gregs.voidps.engine.client.privateChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.PrivateMessage
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.on

on<PrivateMessage> { player: Player ->
    println("Pm $this")
    player.privateChat(player.accountName, player.name, player.rights.ordinal, message)
}
import world.gregs.voidps.bot.isBot
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message

on<Registered>({ !it.isBot }) { player: Player ->
    player.message("Welcome to Void.")
}
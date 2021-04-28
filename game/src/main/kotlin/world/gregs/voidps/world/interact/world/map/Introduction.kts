import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.bot.isBot

on<Registered>({ !it.isBot }) { player: Player ->
    player.message("Welcome to Void.")
}
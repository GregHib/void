import rs.dusk.engine.client.verify.verify
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.player.command.Command
import rs.dusk.network.rs.codec.game.decode.message.ConsoleCommandMessage
import rs.dusk.utility.inject

val bus: EventBus by inject()

ConsoleCommandMessage verify { player ->
    val parts = command.split(" ")
    val prefix = parts[0]
    bus.emit(Command(player, prefix, command.removePrefix("$prefix ")))
}
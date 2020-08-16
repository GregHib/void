import rs.dusk.engine.client.send
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ContextMenuOptionMessage
import rs.dusk.world.command.Command

Command where { prefix == "test" } then {
    player.send(ContextMenuOptionMessage("Test $content", content.toInt(), true))
}

Command where { prefix == "test2" } then {
    player.options.remove(1)
}
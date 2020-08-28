import rs.dusk.engine.client.send
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.ContainerItemsMessage
import rs.dusk.world.command.Command
import rs.dusk.world.community.trade.warn

Command where { prefix == "test" } then {
    player.warn("trade_main", "other_stuff", 0)
}

Command where { prefix == "sendItems" } then {
    player.send(ContainerItemsMessage(90, IntArray(28) { 995 }, IntArray(28) { 1 }, false))
    player.send(ContainerItemsMessage(90, IntArray(28) { 11694 }, IntArray(28) { 1 }, true))
}
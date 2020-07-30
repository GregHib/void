package rs.dusk.world.interact.player

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.network.rs.codec.game.encode.message.InterfaceSettingsMessage

InterfaceOpened where { name == "inventory" } then {
    player.send(InterfaceSettingsMessage(id, 0, 0, 27, 4554126))// Item slots
    player.send(InterfaceSettingsMessage(id, 0, 28, 55, 2097152))// Draggable slots
//    player.send(InterfaceItems())
}
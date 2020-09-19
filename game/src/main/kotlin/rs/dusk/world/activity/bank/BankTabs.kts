package rs.dusk.world.activity.bank

import rs.dusk.engine.entity.character.contain.sendContainer
import rs.dusk.engine.entity.character.player.PlayerSpawn
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceSwitch

PlayerSpawn then {
    player.bank.listeners.add {
        player.bank.sort()// TODO tracking sort?
        player.sendContainer("bank")// This isn't very efficient and negates the existing update system
    }
}
InterfaceSwitch where { name == "bank" && component == "container" } then {
    // TODO insert between & update tab positions
}

InterfaceSwitch then {
    println(this)
}
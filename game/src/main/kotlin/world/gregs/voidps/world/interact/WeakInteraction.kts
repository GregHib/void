import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.clearInterfaces
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

on<InterfaceSwitch>(priority = Priority.HIGH) { player: Player ->
    player.clearInterfaces()
}

on<InterfaceOnInterface>(priority = Priority.HIGH) { player: Player ->
    player.clearInterfaces()
}

on<InterfaceClosed> { player: Player ->
    player.queue.clearWeak()
}
import world.gregs.voidps.engine.client.ui.InterfaceSwitch
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.CloseInterface
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

on<InterfaceSwitch>({ id != "bank" }, Priority.HIGH) { player: Player ->
    player.closeInterfaces()
}

on<InterfaceOnInterface>(priority = Priority.HIGH) { player: Player ->
    player.closeInterfaces()
}

on<CloseInterface> { player: Player ->
    player.queue.clearWeak()
}
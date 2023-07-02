package world.gregs.voidps.world.interact

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.event.CloseInterface
import world.gregs.voidps.engine.client.ui.interact.ItemOnItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

on<ItemOnItem>(priority = Priority.HIGH) { player: Player ->
    player.closeInterfaces()
}

on<CloseInterface> { player: Player ->
    player.queue.clearWeak()
}
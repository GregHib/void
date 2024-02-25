package world.gregs.voidps.world.interact

import world.gregs.voidps.engine.client.ui.event.CloseInterface
import world.gregs.voidps.engine.event.on

on<CloseInterface> { player ->
    player.queue.clearWeak()
}
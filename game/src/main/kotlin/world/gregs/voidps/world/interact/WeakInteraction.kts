package world.gregs.voidps.world.interact

import world.gregs.voidps.engine.client.ui.event.interfaceClose

interfaceClose { player ->
    player.queue.clearWeak()
}
package world.gregs.voidps.world.interact

import world.gregs.voidps.engine.client.ui.event.CloseInterface
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<CloseInterface> { player: Player ->
    player.queue.clearWeak()
}
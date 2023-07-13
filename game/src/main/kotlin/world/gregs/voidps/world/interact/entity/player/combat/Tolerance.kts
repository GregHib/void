package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.mode.move.Moved
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.area.Cuboid
import java.util.concurrent.TimeUnit

on<Registered> { player: Player ->
    player.start("tolerance", TimeUnit.MINUTES.toTicks(10))
    player["tolerance_area"] = player.tile.toCuboid(10)
}

on<Moved>({ to !in it.get<Cuboid>("tolerance_area") }) { player: Player ->
    player["tolerance_area"] = player.tile.toCuboid(10)
    player.start("tolerance", TimeUnit.MINUTES.toTicks(10))
}
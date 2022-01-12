import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.restart
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

on<Registered>(priority = Priority.HIGHEST) { player: Player ->
    for (key in player.values.keys) {
        if (key.endsWith("_effect")) {
            player.restart(key.removeSuffix("_effect"))
        }
    }
}
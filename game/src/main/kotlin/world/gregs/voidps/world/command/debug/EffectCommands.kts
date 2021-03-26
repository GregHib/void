import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command

on<Command>({ prefix == "effects" }) { player: Player ->
    for(effect in player.effects.getAll().values) {
        player.message(effect.toString())
    }
}

on<Command>({ prefix == "remove-effect" }) { player: Player ->
    player.effects.remove(content)
}
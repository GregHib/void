import world.gregs.void.engine.event.then
import world.gregs.void.engine.event.where
import world.gregs.void.network.codec.game.encode.message
import world.gregs.void.world.command.Command

Command where { prefix == "effects" } then {
    for(effect in player.effects.getAll().values) {
        player.message(effect.toString())
    }
}

Command where { prefix == "remove-effect" } then {
    player.effects.remove(content)
}
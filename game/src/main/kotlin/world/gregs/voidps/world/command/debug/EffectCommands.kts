import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.network.codec.game.encode.message
import world.gregs.voidps.world.command.Command

Command where { prefix == "effects" } then {
    for(effect in player.effects.getAll().values) {
        player.message(effect.toString())
    }
}

Command where { prefix == "remove-effect" } then {
    player.effects.remove(content)
}
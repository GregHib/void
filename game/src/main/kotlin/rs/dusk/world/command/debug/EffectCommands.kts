import rs.dusk.engine.entity.character.player.chat.message
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.command.Command

Command where { prefix == "effects" } then {
    for(effect in player.effects.getAll().values) {
        player.message(effect.toString())
    }
}

Command where { prefix == "remove-effect" } then {
    player.effects.remove(content)
}
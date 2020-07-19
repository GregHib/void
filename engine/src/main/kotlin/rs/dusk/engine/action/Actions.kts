import rs.dusk.engine.action.Suspension
import rs.dusk.engine.event.Priority
import rs.dusk.engine.event.priority
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.index.Character
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.utility.inject

val players: Players by inject()
val npcs: NPCs by inject()

Tick priority Priority.ACTION_PROCESS then {
    players.forEach(::action)
    npcs.forEach(::action)
}

fun action(character: Character) {
    if (character.action.suspension == Suspension.Tick) {
        character.action.resume()
    }
}
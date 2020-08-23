import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.player.skill.exp
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.command.Command

Command where { prefix == "test" } then {
    player.exp(Skill.valueOf(content.capitalize()), 10050.0)
}
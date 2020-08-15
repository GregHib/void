import rs.dusk.engine.client.send
import rs.dusk.engine.event.then
import rs.dusk.network.rs.codec.game.encode.message.SkillLevelMessage
import rs.dusk.world.activity.skill.exp.Experience

var total = 14000000

Experience then {
    total += increase
    player.send(SkillLevelMessage(skill.ordinal, 99, total))
}
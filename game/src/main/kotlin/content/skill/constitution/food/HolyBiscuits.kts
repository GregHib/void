package content.skill.constitution.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.constitution.consume

consume("holy_biscuits") { player ->
    player.levels.restore(Skill.Prayer, 10)
}
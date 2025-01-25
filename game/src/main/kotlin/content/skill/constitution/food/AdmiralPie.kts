package content.skill.constitution.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.constitution.consume

consume("admiral_pie*") { player ->
    player.levels.boost(Skill.Fishing, 5)
}
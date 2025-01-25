package content.skill.constitution.food

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.skill.constitution.consume

consume("garden_pie*") { player ->
    player.levels.boost(Skill.Farming, 5)
}
package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill

consume("garden_pie*") { player ->
    player.levels.boost(Skill.Farming, 5)
}

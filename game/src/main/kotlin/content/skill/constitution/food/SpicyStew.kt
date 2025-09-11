package content.skill.constitution.food

import content.skill.constitution.consume
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.random

@Script
class SpicyStew {

    init {
        consume("spicy_stew") { player ->
            if (random.nextInt(100) > 5) {
                player.levels.boost(Skill.Cooking, 6)
            } else {
                player.levels.drain(Skill.Cooking, 6)
            }
        }
    }
}

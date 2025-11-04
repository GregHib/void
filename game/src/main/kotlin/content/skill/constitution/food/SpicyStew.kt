package content.skill.constitution.food

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.random

class SpicyStew : Script {

    init {
        consumed("spicy_stew") { _, _ ->
            if (random.nextInt(100) > 5) {
                levels.boost(Skill.Cooking, 6)
            } else {
                levels.drain(Skill.Cooking, 6)
            }
        }
    }
}

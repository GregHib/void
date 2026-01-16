package content.entity.death

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class CharacterDeath : Script {

    init {
        levelChanged(Skill.Constitution) { _, _, to ->
            if (to <= 0 && !queue.contains("death")) {
                Death.killed(this)
            }
        }

        npcLevelChanged(Skill.Constitution) { _, _, to ->
            if (to <= 0 && !queue.contains("death")) {
                Death.killed(this)
            }
        }
    }
}

package content.entity.death

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class CharacterDeath : Script {
    init {
        levelChanged(Skill.Constitution) { skill, from, to ->
            if (to <= 0 && !queue.contains("death")) {
                emit(Death)
            }
        }

        npcLevelChanged(Skill.Constitution) { skill, from, to ->
            if (to <= 0 && !queue.contains("death")) {
                emit(Death)
            }
        }
    }
}

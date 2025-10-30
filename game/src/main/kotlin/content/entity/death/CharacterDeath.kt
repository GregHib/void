package content.entity.death

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class CharacterDeath : Script {
    init {
        levelChanged(Skill.Constitution) { player, skill, from, to ->
            if (to <= 0 && !player.queue.contains("death")) {
                player.emit(Death)
            }
        }

        npcLevelChanged(Skill.Constitution) { npc, skill, from, to ->
            if (to <= 0 && !npc.queue.contains("death")) {
                npc.emit(Death)
            }
        }
    }
}

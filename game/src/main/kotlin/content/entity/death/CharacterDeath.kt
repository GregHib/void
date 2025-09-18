package content.entity.death

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class CharacterDeath : Api {

    override fun levelChanged(npc: NPC, skill: Skill, from: Int, to: Int) {
        if (skill == Skill.Constitution && to <= 0 && !npc.queue.contains("death")) {
            npc.emit(Death)
        }
    }

    override fun levelChanged(player: Player, skill: Skill, from: Int, to: Int) {
        if (skill == Skill.Constitution && to <= 0 && !player.queue.contains("death")) {
            player.emit(Death)
        }
    }
}

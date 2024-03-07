package world.gregs.voidps.world.interact.entity.death

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.characterLevelChange

characterLevelChange(Skill.Constitution) { character ->
    if (to <= 0 && !character.queue.contains("death")) {
        character.emit(Death)
    }
}
package world.gregs.voidps.world.interact.entity.death

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.levelChange

levelChange({ skill == Skill.Constitution && to <= 0 && !it.queue.contains("death") }) { character: Character ->
    character.events.emit(Death)
}
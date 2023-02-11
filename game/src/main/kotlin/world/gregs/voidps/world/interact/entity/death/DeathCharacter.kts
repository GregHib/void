package world.gregs.voidps.world.interact.entity.death

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.CurrentLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.ActionPriority

on<CurrentLevelChanged>({ skill == Skill.Constitution && to <= 0 && !it.queue.contains(ActionPriority.Strong) }) { character: Character ->
    character.events.emit(Death)
}
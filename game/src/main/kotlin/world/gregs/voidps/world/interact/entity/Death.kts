import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.player.skill.CurrentLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on

on<CurrentLevelChanged>({ skill == Skill.Constitution && to <= 0 && it.action.type != ActionType.Dying }) { character: Character ->
    character.events.emit(Death)
}
package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit

val Character.retaliates: Boolean
    get() = if (this is NPC) {
        def["retaliates", true]
    } else {
        this["auto_retaliate", false]
    }

characterCombatHit { character: Character ->
    if (source == character || !character.retaliates) {
        return@characterCombatHit
    }
    if (character.levels.get(Skill.Constitution) <= 0 || character.underAttack && character.target == source) {
        return@characterCombatHit
    }
    if (character.mode !is CombatMovement) {
        val target = source
        character.mode = CombatMovement(character, target)
        character.target = target
    }
}
package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.entity.combat.hit.combatHit

val Character.retaliates: Boolean
    get() = if (this is NPC) {
        def["retaliates", true]
    } else {
        this["auto_retaliate", false]
    }

combatHit({ source != it && it.retaliates }) { character: Character ->
    if (character.levels.get(Skill.Constitution) <= 0 || character.underAttack && character.target == source) {
        return@combatHit
    }
    if (character.mode !is CombatMovement) {
        val target = source
        character.mode = CombatMovement(character, target)
        character.target = target
    }
}
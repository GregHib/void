package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
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

characterCombatHit { character ->
    if (source == character || !character.retaliates) {
        return@characterCombatHit
    }
    if (type == "poison" || type == "disease" || type == "healed") {
        return@characterCombatHit
    }
    if (character.levels.get(Skill.Constitution) <= 0 || character.inCombat && character.target == source) {
        return@characterCombatHit
    }
    if (!character.hasClock("in_combat") || character.mode is CombatMovement) {
        character.mode = CombatMovement(character, source)
        character.target = source
        val delay = character.attackSpeed / 2
        character.start("action_delay", delay)
        character.start("in_combat", delay + 8)
    } else {
        character.start("in_combat", 8)
    }
}

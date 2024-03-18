package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.GameLoop
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
    if (character.mode is CombatMovement) {
        return@characterCombatHit
    }
    val target = source
    character.mode = CombatMovement(character, target)
    character.target = target
    if (character.hasClock("hit_delay", if (character is NPC) GameLoop.tick - 8 else GameLoop.tick)) {
        character.start("hit_delay", character.attackSpeed / 2)
    }
}
package content.skill.magic.book.ancient

import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import content.skill.magic.spell.Spell

characterCombatAttack(spell = "shadow_*", type = "magic") { source ->
    if (damage <= 0) {
        return@characterCombatAttack
    }
    Spell.drain(source, target, spell)
}
package content.skill.magic.book.ancient

import content.entity.combat.hit.characterCombatAttack
import content.skill.magic.spell.Spell
import world.gregs.voidps.engine.event.Script

@Script
class ShadowSpells {

    init {
        characterCombatAttack(spell = "shadow_*", type = "magic") { source ->
            if (damage <= 0) {
                return@characterCombatAttack
            }
            Spell.drain(source, target, spell)
        }
    }
}

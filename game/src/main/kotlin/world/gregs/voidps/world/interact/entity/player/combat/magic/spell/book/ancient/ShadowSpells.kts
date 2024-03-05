package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.ancient

import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell

characterCombatAttack(spell = "shadow_*", type = "magic") { source ->
    if (damage <= 0) {
        return@characterCombatAttack
    }
    Spell.drain(source, target, spell)
}
package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.world.interact.entity.combat.characterCombatPrepare
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

characterCombatPrepare("magic") { character ->
    if (!Spell.canDrain(target, character.spell)) {
        cancel()
    }
}
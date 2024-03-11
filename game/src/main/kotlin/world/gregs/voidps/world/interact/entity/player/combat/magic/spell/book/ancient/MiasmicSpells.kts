package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.ancient

import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack

val definitions: SpellDefinitions by inject()

characterCombatAttack(spell = "miasmic_*", type = "magic") {
    if (damage <= 0) {
        return@characterCombatAttack
    }
    val seconds: Int = definitions.get(spell)["effect_seconds"]
    target.start("miasmic", seconds, epochSeconds())
}
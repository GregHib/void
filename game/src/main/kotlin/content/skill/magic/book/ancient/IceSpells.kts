package content.skill.magic.book.ancient

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.inject
import content.entity.combat.hit.characterCombatAttack
import content.entity.effect.freeze

val definitions: SpellDefinitions by inject()

characterCombatAttack(spell = "ice_*", type = "magic") { source ->
    if (damage <= 0) {
        return@characterCombatAttack
    }
    val ticks: Int = definitions.get(spell)["freeze_ticks"]
    source.freeze(target, ticks)
}
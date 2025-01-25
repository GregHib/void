package content.skill.magic.book.ancient

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import content.entity.effect.toxin.poison

val definitions: SpellDefinitions by inject()

characterCombatAttack(spell = "smoke_*", type = "magic") { source ->
    if (damage <= 0) {
        return@characterCombatAttack
    }
    if (random.nextDouble() <= 0.2) {
        val damage: Int = definitions.get(spell)["poison_damage"]
        source.poison(target, damage)
    }
}
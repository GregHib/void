package content.skill.magic.book.ancient

import content.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.engine.event.Script
@Script
class MiasmicSpells {

    val definitions: SpellDefinitions by inject()
    
    init {
        characterCombatAttack(spell = "miasmic_*", type = "magic") {
            if (damage <= 0) {
                return@characterCombatAttack
            }
            val seconds: Int = definitions.get(spell)["effect_seconds"]
            target.start("miasmic", seconds, epochSeconds())
        }

    }

}

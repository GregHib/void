package content.skill.magic.book.ancient

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack

val definitions: SpellDefinitions by inject()

characterCombatAttack(spell = "blood_*", type = "magic") { source ->
    if (damage <= 0) {
        return@characterCombatAttack
    }
    val maxHeal: Int = definitions.get(spell)["max_heal"]
    val health = (damage / 4).coerceAtMost(maxHeal)
    source.levels.restore(Skill.Constitution, health)
}
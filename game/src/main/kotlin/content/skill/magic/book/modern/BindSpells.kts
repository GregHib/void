package content.skill.magic.book.modern

import content.entity.combat.hit.CombatAttack
import content.entity.combat.hit.characterCombatAttack
import content.entity.effect.freeze
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.inject

val definitions: SpellDefinitions by inject()

val attackHandler: suspend CombatAttack.(Character) -> Unit = { character ->
    if (damage > 0) {
        character.freeze(target, definitions.get(spell)["freeze_ticks"])
    }
}
characterCombatAttack(spell = "bind", type = "magic", handler = attackHandler)
characterCombatAttack(spell = "snare", type = "magic", handler = attackHandler)
characterCombatAttack(spell = "entangle", type = "magic", handler = attackHandler)

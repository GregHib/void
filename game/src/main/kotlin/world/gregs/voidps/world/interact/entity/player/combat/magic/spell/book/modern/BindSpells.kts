package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.effect.freeze

val definitions: SpellDefinitions by inject()

val attackHandler: suspend CombatAttack.(Character) -> Unit = { character ->
    if (damage > 0) {
        character.freeze(target, definitions.get(spell)["freeze_ticks"])
    }
}
characterCombatAttack(spell = "bind", type = "magic", block = attackHandler)
characterCombatAttack(spell = "snare", type = "magic", block = attackHandler)
characterCombatAttack(spell = "entangle", type = "magic", block = attackHandler)
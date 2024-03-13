package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.modern

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.characterCombatPrepare
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell

val spellDefinitions: SpellDefinitions by inject()

characterCombatPrepare("magic") { character ->
    val definition = spellDefinitions.get(character.spell)
    if (definition.contains("drain_skill") && !Spell.canDrain(target, definition)) {
        cancel()
    }
}
package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.ancient

import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.hit.characterSpellAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.spellSwing
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

spellSwing("ice_*", Priority.LOW) { player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (Spell.isMultiTarget(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    player.hit(target)
    delay = 5
}

characterSpellAttack("ice_*") { source ->
    val ticks: Int = definitions.get(spell)["freeze_ticks"]
    source.freeze(target, ticks)
}
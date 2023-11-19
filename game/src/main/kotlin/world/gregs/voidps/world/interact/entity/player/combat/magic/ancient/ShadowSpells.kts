package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.Spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.drainSpell
import world.gregs.voidps.world.interact.entity.proj.shoot

on<CombatSwing>({ player -> !swung() && player.spell.startsWith("shadow_") }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (Spell.isMultiTarget(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    player.hit(target)
    delay = 5
}

on<CombatHit>({ spell.startsWith("shadow_") && damage > 0 }) { target: Character ->
    source.drainSpell(target, spell)
}
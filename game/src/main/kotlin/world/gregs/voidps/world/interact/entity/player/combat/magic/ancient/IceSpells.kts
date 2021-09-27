package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.isMultiTargetSpell
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

fun isSpell(spell: String) = spell.startsWith("ice_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (isMultiTargetSpell(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    if (player.hit(target) != -1) {
        val ticks: Int = definitions.get(spell)["freeze_ticks"]
        player.freeze(target, ticks)
    }
    delay = 5
}
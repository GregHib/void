package world.gregs.voidps.world.interact.entity.player.combat.magic.ancient

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.spell
import world.gregs.voidps.world.interact.entity.player.combat.magic.isMultiTargetSpell
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.proj.shoot
import java.util.concurrent.TimeUnit

fun isSpell(spell: String) = spell.startsWith("ice_")

on<CombatSwing>({ player -> !swung() && isSpell(player.spell) }, Priority.LOW) { player: Player ->
    val spell = player.spell
    player.setAnimation("ancient_spell${if (isMultiTargetSpell(spell)) "_multi" else ""}")
    player.shoot(spell, target)
    player["spell_damage"] = maxHit(spell)
    player["spell_experience"] = experience(spell)
    if (player.hit(target) != -1) {
        player.freeze(target, TimeUnit.SECONDS.toTicks(seconds(spell)))
    }
    delay = 5
}

fun maxHit(spell: String) = when (spell) {
    "ice_rush" -> 160
    "ice_burst" -> 220
    "ice_blitz" -> 260
    "ice_barrage" -> 300
    else -> 0
}

fun experience(spell: String) = when (spell) {
    "ice_rush" -> 34.0
    "ice_burst" -> 40.0
    "ice_blitz" -> 46.0
    "ice_barrage" -> 52.0
    else -> 0.0
}

fun seconds(spell: String) = when (spell) {
    "ice_rush" -> 5
    "ice_burst" -> 10
    "ice_blitz" -> 15
    "ice_barrage" -> 20
    else -> 0
}
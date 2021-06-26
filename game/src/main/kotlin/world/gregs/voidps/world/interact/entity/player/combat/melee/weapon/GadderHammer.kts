package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor
import kotlin.random.Random

fun isShade(target: Character?): Boolean = target != null

on<HitDamageModifier>({ type == "melee" && weapon?.name == "gadderhammer" && isShade(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * if (Random.nextDouble() < 0.05) 2.0 else 1.25)
}
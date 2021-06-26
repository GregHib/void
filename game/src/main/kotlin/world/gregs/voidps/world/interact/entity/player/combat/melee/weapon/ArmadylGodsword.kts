package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack
import kotlin.math.floor

on<HitDamageModifier>({ player -> type == "melee" && player.specialAttack && weapon?.name == "armadyl_godsword" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.25)
}
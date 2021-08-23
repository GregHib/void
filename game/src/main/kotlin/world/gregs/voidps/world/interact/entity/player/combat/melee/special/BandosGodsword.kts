package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

on<HitDamageModifier>({ type == "melee" && special && weapon?.name == "bandos_godsword" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.1)
}
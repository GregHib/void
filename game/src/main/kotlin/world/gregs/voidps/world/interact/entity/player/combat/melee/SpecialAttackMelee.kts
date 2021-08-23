package world.gregs.voidps.world.interact.entity.player.combat.melee

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.HitRatingModifier
import kotlin.math.floor

on<HitRatingModifier>({ offense && type == "melee" && special && weapon?.def?.has("spec_acc_multiplier") == true }, Priority.HIGH) { player: Player ->
    val multiplier = weapon?.def?.getOrNull("spec_acc_multiplier") as? Double ?: 1.0
    rating = floor(rating * multiplier)
}

on<HitDamageModifier>({ type == "melee" && special && weapon?.def?.has("spec_dmg_multiplier") == true }, Priority.HIGH) { player: Player ->
    val multiplier = weapon?.def?.getOrNull("spec_dmg_multiplier") as? Double ?: 1.0
    damage = floor(damage * multiplier)
}
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.HitRatingModifier
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import kotlin.math.floor

on<HitRatingModifier>({ player -> offense && type == "melee" && player.specialAttack && weapon?.def?.has("spec_acc_multiplier") == true }, Priority.HIGH) { player: Player ->
    val multiplier = weapon?.def?.getOrNull("spec_acc_multiplier") as? Double ?: 1.0
    rating = floor(rating * multiplier)
}

on<HitDamageModifier>({ player -> type == "melee" && player.specialAttack && weapon?.def?.has("spec_dmg_multiplier") == true }, Priority.HIGH) { player: Player ->
    val multiplier = weapon?.def?.getOrNull("spec_dmg_multiplier") as? Double ?: 1.0
    damage = floor(damage * multiplier)
}

on<HitDamageModifier>({ player -> type == "melee" && player.specialAttack && weapon?.name == "armadyl_godsword" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.25)
}

on<HitDamageModifier>({ player -> type == "melee" && player.specialAttack && weapon?.name == "bandos_godsword" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.1)
}
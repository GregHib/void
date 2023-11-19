package world.gregs.voidps.world.interact.entity.player.combat.range.ammo

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.combat.hit

on<CombatHit>({ source is Player && type == "range" && source.ammo == "saradomin_arrows" }) { character: Character ->
    val chance = if (weapon?.id == "saradomin_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // water_strike
        val damage = hit(source, character, type, weapon)
        hit(source, character, damage, "magic", weapon)
    }
}

on<CombatHit>({ source is Player && type == "range" && source.ammo == "guthix_arrows" }) { character: Character ->
    val chance = if (weapon?.id == "guthix_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // earth_strike
        val damage = hit(source, character, type, weapon)
        hit(source, character, damage, "magic", weapon)
    }
}

on<CombatHit>({ source is Player && type == "range" && source.ammo == "zamorak_arrows" }) { character: Character ->
    val chance = if (weapon?.id == "zamorak_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // fire_strike
        val damage = hit(source, character, type, weapon)
        hit(source, character, damage, "magic", weapon)
    }
}
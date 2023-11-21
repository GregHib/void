package world.gregs.voidps.world.interact.entity.player.combat.range.ammo

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo

on<CombatAttack>({ type == "range" && it.ammo == "saradomin_arrows" }) { source: Player ->
    val chance = if (weapon.id == "saradomin_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // water_strike
        val damage = Damage.roll(source, target, type, weapon)
        source.hit(target, weapon, "magic", CLIENT_TICKS.toTicks(delay), damage = damage)
    }
}

on<CombatAttack>({ type == "range" && it.ammo == "guthix_arrows" }) { source: Player ->
    val chance = if (weapon.id == "guthix_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // earth_strike
        val damage = Damage.roll(source, target, type, weapon)
        source.hit(target, weapon, "magic", CLIENT_TICKS.toTicks(delay), damage = damage)
    }
}

on<CombatAttack>({ type == "range" && it.ammo == "zamorak_arrows" }) { source: Player ->
    val chance = if (weapon.id == "zamorak_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // fire_strike
        val damage = Damage.roll(source, target, type, weapon)
        source.hit(target, weapon, "magic", CLIENT_TICKS.toTicks(delay), damage = damage)
    }
}
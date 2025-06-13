package content.skill.ranged.ammo

import content.entity.combat.hit.Damage
import content.entity.combat.hit.combatAttack
import content.entity.combat.hit.hit
import content.skill.ranged.ammo
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import world.gregs.voidps.type.random

combatAttack(type = "range") { source ->
    if (source.ammo != "saradomin_arrows") {
        return@combatAttack
    }
    val chance = if (weapon.id == "saradomin_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // water_strike
        val damage = Damage.roll(source, target, type, weapon)
        source.hit(target, weapon, "magic", HitSplat.Mark.Magic, CLIENT_TICKS.toTicks(delay), damage = damage)
    }
}

combatAttack(type = "range") { source ->
    if (source.ammo != "guthix_arrows") {
        return@combatAttack
    }
    val chance = if (weapon.id == "guthix_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // earth_strike
        val damage = Damage.roll(source, target, type, weapon)
        source.hit(target, weapon, "magic", HitSplat.Mark.Magic, CLIENT_TICKS.toTicks(delay), damage = damage)
    }
}

combatAttack(type = "range") { source ->
    if (source.ammo != "zamorak_arrows") {
        return@combatAttack
    }
    val chance = if (weapon.id == "zamorak_bow") 0.2 else 0.1
    if (random.nextDouble() < chance) {
        // fire_strike
        val damage = Damage.roll(source, target, type, weapon)
        source.hit(target, weapon, "magic", HitSplat.Mark.Magic, CLIENT_TICKS.toTicks(delay), damage = damage)
    }
}

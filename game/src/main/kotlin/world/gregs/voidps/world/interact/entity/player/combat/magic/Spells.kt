package world.gregs.voidps.world.interact.entity.player.combat.magic

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.world.interact.entity.combat.height
import world.gregs.voidps.world.interact.entity.proj.ShootProjectile
import world.gregs.voidps.world.interact.entity.proj.shoot

fun Character.cast(
    name: String,
    target: Character,
    delay: Int = 52,
    height: Int = this.height - 4,
    curve: Int = ShootProjectile.DEFAULT_CURVE
) = shoot(name = name, target = target, delay = delay, height = height, endHeight = target.height, curve = curve)

fun getElementalSpellDamage(spell: String): Double {
    val base = when {
        spell.endsWith("rush") -> 10
        spell.endsWith("strike") -> 20
        spell.endsWith("bolt") -> 90
        spell.endsWith("blast") -> 130
        spell.endsWith("wave") -> 170
        spell.endsWith("surge") -> 220
        else -> 0
    }
    val double = if (spell.endsWith("strike") || spell.endsWith("surge")) 2.0 else 1.0
    val addition = when {
        spell.startsWith("wind") -> 0.0
        spell.startsWith("water") -> 10.0
        spell.startsWith("earth") -> 20.0
        spell.startsWith("earth") -> 30.0
        else -> 0.0
    }
    return base + addition * double
}

fun getElementalSpellExperience(spell: String): Double {
    val type = when {
        spell.endsWith("rush") -> 2.7
        spell.endsWith("strike") -> 5.5
        spell.endsWith("bolt") -> 13.5
        spell.endsWith("blast") -> 25.5
        spell.endsWith("wave") -> 36.0
        spell.endsWith("surge") -> 75.0
        else -> 0.0
    }
    val difference = when {
        spell.endsWith("strike") -> 2.0
        spell.endsWith("bolt") -> 3.0
        spell.endsWith("blast") -> 3.0
        spell.endsWith("wave") -> 2.5
        spell.endsWith("surge") -> 5.0
        else -> 0.0
    }
    val multiplier = when {
        spell.startsWith("wind") -> 0.0
        spell.startsWith("water") -> 1.0
        spell.startsWith("earth") -> 2.0
        spell.startsWith("earth") -> 3.0
        else -> 0.0
    }
    return type + difference * multiplier
}
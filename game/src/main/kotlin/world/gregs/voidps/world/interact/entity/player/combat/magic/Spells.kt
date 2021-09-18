package world.gregs.voidps.world.interact.entity.player.combat.magic

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
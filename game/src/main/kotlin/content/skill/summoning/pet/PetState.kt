package content.skill.summoning.pet

import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Hunger / growth / hunger-warning state for one pet variant the player owns.
 * Persisted as a single comma-separated `<def.id>:<hunger>:<growth>:<warn>`
 * blob in the `pet_stats` player variable (declared persistent in pet.vars.toml).
 */
data class PetStats(
    var hunger: Double = 0.0,
    var growth: Double = 0.0,
    var warn: Int = 0,
) {
    fun isDefault(): Boolean = hunger == 0.0 && growth == 0.0 && warn == 0
}

private fun Player.readPetStats(): MutableMap<String, PetStats> {
    val raw = get("pet_stats", "")
    if (raw.isBlank()) return mutableMapOf()
    val map = mutableMapOf<String, PetStats>()
    for (entry in raw.split(',')) {
        if (entry.isBlank()) continue
        val parts = entry.split(':')
        if (parts.size != 4) continue
        val hunger = parts[1].toDoubleOrNull() ?: continue
        val growth = parts[2].toDoubleOrNull() ?: continue
        val warn = parts[3].toIntOrNull() ?: continue
        map[parts[0]] = PetStats(hunger, growth, warn)
    }
    return map
}

private fun Player.writePetStats(map: Map<String, PetStats>) {
    val encoded = map.entries
        .filter { !it.value.isDefault() }
        .joinToString(",") { (id, s) -> "$id:${s.hunger}:${s.growth}:${s.warn}" }
    set("pet_stats", encoded)
}

fun Player.getPetHunger(id: String): Double = readPetStats()[id]?.hunger ?: 0.0
fun Player.getPetGrowth(id: String): Double = readPetStats()[id]?.growth ?: 0.0
fun Player.getPetWarn(id: String): Int = readPetStats()[id]?.warn ?: 0

/** Mutate the named pet's stats and persist the blob. */
fun Player.updatePetStats(id: String, block: PetStats.() -> Unit) {
    val map = readPetStats()
    val stats = map.getOrPut(id) { PetStats() }
    block(stats)
    writePetStats(map)
}

/** Remove any persisted stats for the named pet (e.g. on run-away / shoo). */
fun Player.clearPetStats(id: String) {
    val map = readPetStats()
    if (map.remove(id) != null) writePetStats(map)
}

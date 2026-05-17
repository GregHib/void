package content.skill.summoning.pet

import world.gregs.voidps.engine.entity.character.player.Player

data class PetStats(
    var hunger: Double,
    var growth: Double,
    var warn: Int,
)

fun Player.getPetHunger(id: String): Double = get("pet_${id}_hunger", 0.0)
fun Player.getPetGrowth(id: String): Double = get("pet_${id}_growth", 0.0)
fun Player.getPetWarn(id: String): Int = get("pet_${id}_warn", 0)

/** Mutate the named pet's stats. Each field persists in its own player variable. */
fun Player.updatePetStats(id: String, block: PetStats.() -> Unit) {
    val stats = PetStats(getPetHunger(id), getPetGrowth(id), getPetWarn(id))
    block(stats)
    set("pet_${id}_hunger", stats.hunger)
    set("pet_${id}_growth", stats.growth)
    set("pet_${id}_warn", stats.warn)
}

/** Remove any persisted stats for the named pet (e.g. on run-away / shoo). */
fun Player.clearPetStats(id: String) {
    variables.clear("pet_${id}_hunger")
    variables.clear("pet_${id}_growth")
    variables.clear("pet_${id}_warn")
}

package content.skill.summoning.pet

import world.gregs.voidps.engine.entity.character.player.Player

/** 0..PET_STAT_MAX scale for hunger and growth. Bars on iface 663 are 0..100; divide by 100 to display. */
const val PET_STAT_MAX = 10000

fun Player.getPetHunger(id: String): Int = get("pet_${id}_hunger", 0)
fun Player.getPetGrowth(id: String): Int = get("pet_${id}_growth", 0)
fun Player.getPetWarn(id: String): Int = get("pet_${id}_warn", 0)

fun Player.clearPetStats(id: String) {
    clear("pet_${id}_hunger")
    clear("pet_${id}_growth")
    clear("pet_${id}_warn")
}

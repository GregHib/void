package world.gregs.voidps.engine.entity.character.player.equip

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions

class AppearanceOverrides {

    private val maleMid by lazy { load("look_hair_male", "body_look_flat_mid") }
    private val femaleMid by lazy { load("look_hair_female", "body_look_flat_mid") }
    private val maleLow by lazy { load("look_hair_male", "body_look_flat_low") }
    private val femaleLow by lazy { load("look_hair_female", "body_look_flat_low") }

    fun hairMid(current: Int, male: Boolean): Int = (if (male) maleMid else femaleMid).getOrDefault(current, current) + 0x100

    fun hairLow(current: Int, male: Boolean): Int = (if (male) {
        maleLow.getOrDefault(current, 0)
    } else {
        femaleLow.getOrDefault(current, 243)
    }) + 0x100

    private fun load(enumName: String, key: String): Map<Int, Int> {
        val map = Int2IntOpenHashMap()
        for (value in EnumDefinitions.get(enumName).map?.values ?: return map) {
            val struct = StructDefinitions.getOrNull(value as Int) ?: continue
            val index: Int = struct.getOrNull("body_look_index") ?: continue
            val replacement: Int = struct.getOrNull(key) ?: continue
            map[index] = replacement
        }
        return map
    }
}

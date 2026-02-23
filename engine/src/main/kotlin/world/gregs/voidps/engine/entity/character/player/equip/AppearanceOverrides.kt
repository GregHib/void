package world.gregs.voidps.engine.entity.character.player.equip

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import world.gregs.voidps.cache.definition.data.EnumDefinition
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.StructDefinitions

class AppearanceOverrides() {

    constructor(enums: EnumDefinitions) : this() {
        load(enums)
    }

    private val maleMid: MutableMap<Int, Int> = Int2IntOpenHashMap(32)
    private val femaleMid: MutableMap<Int, Int> = Int2IntOpenHashMap(48)
    private val maleLow: MutableMap<Int, Int> = Int2IntOpenHashMap(32)
    private val femaleLow: MutableMap<Int, Int> = Int2IntOpenHashMap(48)

    fun hairMid(current: Int, male: Boolean): Int = (if (male) this.maleMid else femaleMid).getOrDefault(current, current) + 0x100

    fun hairLow(current: Int, male: Boolean): Int = if (male) {
        maleLow.getOrDefault(current, 0)
    } else {
        femaleLow.getOrDefault(current, 243)
    } + 0x100

    private fun load(enums: EnumDefinitions) {
        load(maleMid, enums.get("look_hair_male"), "body_look_flat_mid")
        load(femaleMid, enums.get("look_hair_female"), "body_look_flat_mid")
        load(maleLow, enums.get("look_hair_male"), "body_look_flat_low")
        load(femaleLow, enums.get("look_hair_female"), "body_look_flat_low")
    }

    private fun load(map: MutableMap<Int, Int>, enum: EnumDefinition, key: String) {
        for (value in enum.map?.values ?: return) {
            val structId = value as Int
            val struct = StructDefinitions.getOrNull(structId) ?: continue
            val index: Int = struct.getOrNull("body_look_index") ?: continue
            val replacement: Int = struct.getOrNull(key) ?: continue
            map[index] = replacement
        }
    }
}

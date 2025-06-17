package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.type.RegionLevel

class CharacterMap {

    val regions = arrayOfNulls<MutableList<Int>?>(256 * 256 * 4)

    fun add(region: RegionLevel, character: Character) {
        if (regions[region.id] == null) {
            regions[region.id] = ArrayList(8)
        }
        regions[region.id]!!.add(character.index)
    }

    fun remove(region: RegionLevel, character: Character) {
        val list = regions[region.id] ?: return
        list.remove(character.index)
    }

    operator fun get(region: RegionLevel): List<Int>? = regions[region.id]
}

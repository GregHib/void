package world.gregs.voidps.engine.entity.character

import it.unimi.dsi.fastutil.ints.IntArrayList
import world.gregs.voidps.engine.map.region.RegionLevel

class CharacterMap {

    val regions = arrayOfNulls<IntArrayList?>(256 * 256 * 4)

    fun add(region: RegionLevel, character: Character) {
        if (regions[region.id] == null) {
            regions[region.id] = IntArrayList(8)
        }
        regions[region.id]!!.add(character.index)
    }

    fun remove(region: RegionLevel, character: Character) {
        val list = regions[region.id] ?: return
        list.remove(character.index)
    }

    operator fun get(region: RegionLevel): List<Int>? {
        return regions[region.id]
    }
}
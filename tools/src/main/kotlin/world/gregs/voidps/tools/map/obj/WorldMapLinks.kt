package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder

class WorldMapLinks(
    private val scriptDecoder: ClientScriptDecoder
) {

    fun getLinks(): List<Pair<Tile, Tile>> {
        val list = mutableListOf<Pair<Tile, Tile>>()
        val script = scriptDecoder.get(295)
        val ints = script.intOperands!!
        for (i in ints.indices) {
            val int = ints[i]
            if (int == BI_DIRECTIONAL_LINK) {
                val tile = Tile(ints[i - 11])
                val tile2 = Tile(ints[i - 10])
                list.add(tile to tile2)
                list.add(tile2 to tile)
            } else if (int == UNI_DIRECTIONAL_LINK) {
                list.add(Tile(ints[i - 11]) to Tile(ints[i - 10]))
            }
        }
        return list
    }

    companion object {
        private const val BI_DIRECTIONAL_LINK = 297
        private const val UNI_DIRECTIONAL_LINK = 298
    }
}
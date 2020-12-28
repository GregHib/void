package rs.dusk.tools.map.obj

import rs.dusk.cache.Cache
import rs.dusk.cache.definition.data.ClientScriptDefinition
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.cache.definition.decoder.WorldMapDetailsDecoder
import rs.dusk.cache.definition.decoder.WorldMapIconDecoder
import rs.dusk.engine.map.Tile
import rs.dusk.tools.ClientScriptDefinitions

class WorldMapDungeons(
    private val detailsDecoder: WorldMapDetailsDecoder,
    private val iconDecoder: WorldMapIconDecoder,
    private val scriptDecoder: ClientScriptDecoder,
    private val cache: Cache
) {

    fun getLinks(): List<Pair<Tile, Tile>> {
        val list = mutableListOf<Pair<Tile, Tile>>()
        for (i in detailsDecoder.indices) {
            val def = detailsDecoder.getOrNull(i) ?: continue
            val iconDef = iconDecoder.getOrNull(def.map) ?: continue
            iconDef.icons.forEach { (id, position) ->
                val scriptId = ClientScriptDefinitions.getScriptId(cache, id, 10)
                val script = scriptDecoder.get(scriptId)
                if (script.hasInstruction(0, LOCATION) && script.hasInstruction(1, SCRIPT_ID) && script.getIntOrNull(1) == 304) {
                    val targetPos = script.getInt(0)

                    when (position) {
                        // Manual fixes
                        Tile.getId(2827, 3646) -> {
                            // entrance can only be used during troll stronghold quest
                        }
                        Tile.getId(2998, 3376) -> {
//                            val link = graph.addLink(Tile(position), Tile(targetPos))
//                            link.actions = mutableListOf("item 952 Dig")
                        }
                        Tile.getId(2163, 5115) -> list.add(Tile(2162, 5112, 1) to Tile(targetPos))
                        Tile.getId(3821, 9462) -> {
                            list.add(Tile(3815, 9463) to Tile(3815, 3063))
                            list.add(Tile(3830, 9463) to Tile(3830, 3063))
                        }
                        Tile.getId(3821, 3062) -> {
                            list.add(Tile(3815, 3063) to Tile(3815, 9463))
                            list.add(Tile(3830, 3063) to Tile(3830, 9463))
                        }
                        else -> list.add(Tile(position) to Tile(targetPos))
                    }
                }
            }
        }
        return list
    }

    companion object {
        private fun ClientScriptDefinition.hasInstruction(index: Int, type: Int) = instructions.getOrNull(index) == type
        private fun ClientScriptDefinition.getIntOrNull(index: Int) = intOperands?.getOrNull(index)
        private fun ClientScriptDefinition.getInt(index: Int) = intOperands!![index]
        private const val LOCATION = 0
        private const val SCRIPT_ID = 40
    }
}
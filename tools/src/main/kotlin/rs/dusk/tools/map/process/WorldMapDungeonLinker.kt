package rs.dusk.tools.map.process

import rs.dusk.cache.Cache
import rs.dusk.cache.definition.data.ClientScriptDefinition
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.WorldMapDetailsDecoder
import rs.dusk.cache.definition.decoder.WorldMapIconDecoder
import rs.dusk.engine.client.update.task.viewport.Spiral
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.tools.ClientScriptDefinitions
import rs.dusk.tools.Pipeline
import rs.dusk.tools.map.view.graph.Link
import rs.dusk.tools.map.view.graph.NavigationGraph

/**
 * Grabs data from the world map and converts them to [NavigationGraph] [Link]'s
 */
class WorldMapDungeonLinker(
    private val graph: NavigationGraph,
    private val objectDecoder: ObjectDecoder,
    private val scriptDecoder: ClientScriptDecoder,
    private val detailsDecoder: WorldMapDetailsDecoder,
    private val iconDecoder: WorldMapIconDecoder,
    private val cache: Cache,
    private val linker: ObjectLinker
) : Pipeline.Modifier<Map<Tile, List<GameObject>>> {

    override fun process(content: Map<Tile, List<GameObject>>) {
        val start = graph.links.size
        val tiles = getDungeonLinkTiles()
        process(content, tiles)
        println("${graph.links.size - start} dungeon links found.")
    }

    /**
     * Complete list of dungeon icon positions and their target positions
     */
    private fun getDungeonLinkTiles(): List<Pair<Tile, Tile>> {
        val tiles = mutableListOf<Pair<Tile, Tile>>()
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
                        Tile.getId(2998, 3376) -> {// Giant mole
                            val link = graph.addLink(Tile(position), Tile(targetPos))
                            link.actions = mutableListOf("item 952 Dig")
                        }
                        Tile.getId(2163, 5115) -> tiles.add(Tile(2162, 5112, 1) to Tile(targetPos))
                        Tile.getId(3821, 9462) -> {
                            tiles.add(Tile(3815, 9463) to Tile(3815, 3063))
                            tiles.add(Tile(3830, 9463) to Tile(3830, 3063))
                        }
                        Tile.getId(3821, 3062) -> {
                            tiles.add(Tile(3815, 3063) to Tile(3815, 9463))
                            tiles.add(Tile(3830, 3063) to Tile(3830, 9463))
                        }
                        else -> tiles.add(Tile(position) to Tile(targetPos))
                    }
                }
            }
        }
        return tiles
    }

    fun Map<Tile, List<GameObject>>.getNearbyObjects(tile: Tile): List<GameObject> {
        val objects = mutableListOf<GameObject>()
        Spiral.spiral(tile, 5) { t ->
            objects.addAll(this[t] ?: return@spiral)
        }
        return objects
    }

    /**
     * Expecting an object and action at the start position, the end position should only be walkable.
     */
    fun process(objects: Map<Tile, List<GameObject>>, tiles: List<Pair<Tile, Tile>>) {
        tiles.forEach { (start, end) ->
            val potentialInteractions = objects.getNearbyObjects(start)
            val potentialTargets = objects.getNearbyObjects(end)
            addLink(potentialInteractions, potentialTargets, end)
        }
    }

    fun addLink(interactions: List<GameObject>, targets: List<GameObject>, end: Tile) {
        interactions.forEach {
//            val category = it.validOptions()
//            if(category.isEmpty()) {
//                println("Unknown category $it")
//            } else {
//
//            }
        }
    }

    fun addUniLink(start: Tile, end: Tile) {

    }

    companion object {
        private const val LOCATION = 0
        private const val SCRIPT_ID = 40
    }

    private fun ClientScriptDefinition.hasInstruction(index: Int, type: Int) = instructions.getOrNull(index) == type
    private fun ClientScriptDefinition.getIntOrNull(index: Int) = intOperands?.getOrNull(index)
    private fun ClientScriptDefinition.getInt(index: Int) = intOperands!![index]
}
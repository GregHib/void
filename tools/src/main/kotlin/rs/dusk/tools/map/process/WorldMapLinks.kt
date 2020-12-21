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
import rs.dusk.tools.map.view.graph.NavigationGraph

/**
 * Grabs data from the world map and converts them to [NavigationGraph] [Link]'s
 */
class WorldMapLinks(
    private val graph: NavigationGraph,
    private val objectDecoder: ObjectDecoder,
    private val scriptDecoder: ClientScriptDecoder,
    private val detailsDecoder: WorldMapDetailsDecoder,
    private val iconDecoder: WorldMapIconDecoder,
    private val cache: Cache
) : Pipeline.Modifier<Map<Tile, List<GameObject>>> {

    override fun process(content: Map<Tile, List<GameObject>>) {
        addDungeonLinks(content)
        addMapLinks(content)
        println("${graph.links.size} world map links found.")
    }

    /**
     * Adds enter and exit dungeons
     */
    private fun addDungeonLinks(content: Map<Tile, List<GameObject>>) {
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
                            val link = graph.addLink(Tile(position), Tile(targetPos))
                            link.actions = mutableListOf("item 952 Dig")
                        }
                        Tile.getId(2163, 5115) -> addUniLink(content, Tile(2162, 5112, 1), Tile(targetPos), 1)
                        Tile.getId(3821, 9462) -> {
                            addUniLink(content, Tile(3815, 9463), Tile(3815, 3063), 1)
                            addUniLink(content, Tile(3830, 9463), Tile(3830, 3063), 1)
                        }
                        Tile.getId(3821, 3062) -> {
                            addUniLink(content, Tile(3815, 3063), Tile(3815, 9463), 1)
                            addUniLink(content, Tile(3830, 3063), Tile(3830, 9463), 1)
                        }
                        else -> addUniLink(content, Tile(position), Tile(targetPos), 5)
                    }
                }
            }
        }
    }

    /**
     * Adds map links (yellow lines)
     */
    private fun addMapLinks(content: Map<Tile, List<GameObject>>) {
        val script = scriptDecoder.get(295)
        val ints = script.intOperands!!
        for (i in ints.indices) {
            val int = ints[i]
            if (int == BI_DIRECTIONAL_LINK) {
                val tile = Tile(ints[i - 11])
                val tile2 = Tile(ints[i - 10])
                val ones = content.getObjectsNear(tile, 4)
                val twos = content.getObjectsNear(tile2, 4)
                if (ones.isNotEmpty() && twos.isNotEmpty()) {
                    val start = ones.first()
                    var link = graph.addLink(tile, tile2)
                    link.actions = mutableListOf("object ${start.id} ${getFirstOption(start.id)}")
                    val end = twos.first()
                    link = graph.addLink(tile2, tile)
                    link.actions = mutableListOf("object ${end.id} ${getFirstOption(end.id)}")
                } else {
                    println("Unknown $tile <-> $tile2")
                }
            } else if (int == UNI_DIRECTIONAL_LINK) {
                addUniLink(content, Tile(ints[i - 11]), Tile(ints[i - 10]), 4)
            }
        }
    }

    private fun addUniLink(content: Map<Tile, List<GameObject>>, tile: Tile, tile2: Tile, radius: Int) {
        val ones = content.getObjectsNear(tile, radius)
        if (ones.isNotEmpty()) {
            val start = ones.first()
            val link = graph.addLink(tile, tile2)
            link.actions = mutableListOf("object ${start.id} ${getFirstOption(start.id)}")
        } else {
            println("Unknown $tile -> $tile2")
        }
    }

    companion object {
        private const val BI_DIRECTIONAL_LINK = 297
        private const val UNI_DIRECTIONAL_LINK = 298

        private const val LOCATION = 0
        private const val SCRIPT_ID = 40
    }

    private fun ClientScriptDefinition.hasInstruction(index: Int, type: Int) = instructions.getOrNull(index) == type
    private fun ClientScriptDefinition.getIntOrNull(index: Int) = intOperands?.getOrNull(index)
    private fun ClientScriptDefinition.getInt(index: Int) = intOperands!![index]

    private val interactive: (GameObject) -> Boolean = {
        getFirstOption(it.id) != null
    }

    private fun Map<Tile, List<GameObject>>.getObjectsNear(tile: Tile, radius: Int): List<GameObject> {
        val list = mutableListOf<GameObject>()
        Spiral.spiral(tile, radius) { t ->
            list.addAll(this[t] ?: return@spiral)
        }
        return list.filter(interactive)
    }

    private fun getFirstOption(id: Int): String? {
        val def = objectDecoder.get(id)
        var option = def.options.firstOrNull { it != null && it != "Examine" }
        if (option == null) {
            def.configObjectIds?.forEach { id ->
                option = objectDecoder.get(id).options.firstOrNull { it != null && it != "Examine" }
                if (option != null) {
                    return option
                }
            }
        }
        return option
    }
}
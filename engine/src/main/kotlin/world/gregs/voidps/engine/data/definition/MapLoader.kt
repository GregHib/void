package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import world.gregs.voidps.engine.map.collision.CollisionDecoder

/**
 * Keeps its own map files, which are valid for as long as this loader's key is.
 */
object MapLoader : Loader<MapDefinitions> {
    override val name = "maps"
    override val settings = listOf("development.loadAllObjects", "world.members")
    override val usesCache = true
    override val dependsOn: List<Loader<*>> get() = listOf(ObjectLoader)

    override fun load(context: LoadContext): MapDefinitions = MapDefinitions(CollisionDecoder(), context.requireCache()).load(context.key)

    override fun install(state: MapDefinitions) {}
}

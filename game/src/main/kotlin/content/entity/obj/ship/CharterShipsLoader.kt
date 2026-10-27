package content.entity.obj.ship

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader
import kotlin.collections.set

object CharterShipsLoader : Loader<CharterShips> {
    val instance = CharterShips()
    override val name = "charter_ships"
    override val file = listOf("map.ships.prices")

    override fun load(context: LoadContext): CharterShips = CharterShips().load(context.paths(this).single())

    override fun install(state: CharterShips) = instance.copyFrom(state)
}

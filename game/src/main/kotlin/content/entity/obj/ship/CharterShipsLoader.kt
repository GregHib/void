package content.entity.obj.ship

import world.gregs.voidps.engine.data.load.LoadContext
import world.gregs.voidps.engine.data.load.Loader

object CharterShipsLoader : Loader<CharterShips> {
    val instance = CharterShips()
    override val name = "charter_ships"
    override val label = "charter ship"
    override val file = listOf("map.ships.prices")

    override fun load(context: LoadContext): CharterShips = CharterShips().load(context.paths(this).single())

    override fun total(state: CharterShips) = state.prices.size

    override fun install(state: CharterShips) = instance.copyFrom(state)
}

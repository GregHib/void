package world.gregs.voidps.world.interact.world

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.getProperty

val stairsModule = module {
    single(createdAtStart = true) { Stairs(get()).load() }
}

class Stairs(
    private val storage: FileStorage
) {

    private lateinit var teleports: Map<Tile, Map<String, Teleport>>

    fun get(id: Int, tile: Tile, option: String): Delta? {
        val teleport = teleports[tile]?.get(option) ?: return null
        if (teleport.id != id) {
            return null
        }
        return teleport.delta
    }

    fun load(path: String = getProperty("stairsPath")): Stairs {
        timedLoad("stair") {
            load(storage.load<Array<Teleport>>(path))
        }
        return this
    }

    private fun load(array: Array<Teleport>): Int {
        val map = mutableMapOf<Tile, MutableMap<String, Teleport>>()
        for (tele in array) {
            map.getOrPut(tele.tile) { mutableMapOf() }[tele.option] = tele
        }
        teleports = map
        return teleports.size
    }


    private data class TeleportBuilder(
        val id: Int,
        val option: String,
        val tile: TeleTile,
        val delta: TeleTile
    ) {
        data class TeleTile(val x: Int, val y: Int, val plane: Int = 0)

        fun build() = Teleport(id, option, Tile(tile.x, tile.y, tile.plane), Delta(delta.x, delta.y, delta.plane))
    }

    @JsonDeserialize(builder = TeleportBuilder::class)
    private data class Teleport(val id: Int, val option: String, val tile: Tile, val delta: Delta)
}
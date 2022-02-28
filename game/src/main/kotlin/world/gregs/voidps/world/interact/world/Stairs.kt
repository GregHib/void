package world.gregs.voidps.world.interact.world

import org.koin.dsl.module
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.update.visual.player.move
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

    fun get(id: Int, tile: Tile, option: String): Teleport? {
        val teleport = teleports[tile]?.get(option) ?: return null
        if (teleport.id != id) {
            return null
        }
        return teleport
    }

    fun load(path: String = getProperty("stairsPath")): Stairs {
        timedLoad("stair") {
            val data = storage.load<Array<Map<String, Any>>>(path)
            load(data.map(Teleport::fromMap))
        }
        return this
    }

    private fun load(array: List<Teleport>): Int {
        val map = mutableMapOf<Tile, MutableMap<String, Teleport>>()
        for (tele in array) {
            map.getOrPut(tele.tile) { mutableMapOf() }[tele.option] = tele
        }
        teleports = map
        return teleports.size
    }

    data class Teleport(val id: Int, val option: String, val tile: Tile, val delta: Delta = Delta.EMPTY, val to: Tile = Tile.EMPTY) {
        fun apply(character: Character) {
            if (delta != Delta.EMPTY) {
                character.move(delta)
            } else if (to != Tile.EMPTY) {
                character.move(to)
            }
        }

        companion object {
            @Suppress("UNCHECKED_CAST")
            fun fromMap(map: Map<String, Any>): Teleport {
                return Teleport(
                    id = map["id"] as Int,
                    option = map["option"] as String,
                    tile = Tile.fromMap(map["tile"] as Map<String, Any>),
                    delta = if (map.containsKey("delta")) Delta.fromMap(map["delta"] as Map<String, Any>) else Delta.EMPTY,
                    to = if (map.containsKey("to")) Tile.fromMap(map["to"] as Map<String, Any>) else Tile.EMPTY
                )
            }
        }
    }
}
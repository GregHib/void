package rs.dusk.world.entity.obj

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.world.Tile

data class ReplaceObjectPair(
    val firstOriginal: Location,
    val firstReplacement: Int,
    val firstTile: Tile,
    val firstRotation: Int,
    val secondOriginal: Location,
    val secondReplacement: Int,
    val secondTile: Tile,
    val secondRotation: Int,
    val ticks: Int,
    val owner: String? = null
) : Event() {
    companion object : EventCompanion<ReplaceObjectPair>
}
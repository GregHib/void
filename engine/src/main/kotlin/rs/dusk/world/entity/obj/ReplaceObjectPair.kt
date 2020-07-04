package rs.dusk.world.entity.obj

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.obj.Location
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.get

/**
 * Replaces two existing map objects with replacements provided.
 * The replacements can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for objects to replaced just for one player.
 */
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

fun replaceObjectPair(
    firstOriginal: Location,
    firstReplacement: Int,
    firstTile: Tile,
    firstRotation: Int,
    secondOriginal: Location,
    secondReplacement: Int,
    secondTile: Tile,
    secondRotation: Int,
    ticks: Int,
    owner: String? = null
) = get<EventBus>().emit(
    ReplaceObjectPair(
        firstOriginal,
        firstReplacement,
        firstTile,
        firstRotation,
        secondOriginal,
        secondReplacement,
        secondTile,
        secondRotation,
        ticks,
        owner
    )
)
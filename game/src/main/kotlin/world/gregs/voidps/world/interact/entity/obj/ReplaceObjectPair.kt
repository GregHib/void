package world.gregs.voidps.world.interact.entity.obj

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.EventCompanion
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get

/**
 * Replaces two existing map objects with replacements provided.
 * The replacements can be permanent if [ticks] is -1 or temporary
 * [owner] is also optional to allow for objects to replaced just for one player.
 */
data class ReplaceObjectPair(
    val firstOriginal: GameObject,
    val firstReplacement: Int,
    val firstTile: Tile,
    val firstRotation: Int,
    val secondOriginal: GameObject,
    val secondReplacement: Int,
    val secondTile: Tile,
    val secondRotation: Int,
    val ticks: Int,
    val owner: String? = null
) : Event<Unit>() {
    companion object : EventCompanion<ReplaceObjectPair>
}

fun replaceObjectPair(
    firstOriginal: GameObject,
    firstReplacement: Int,
    firstTile: Tile,
    firstRotation: Int,
    secondOriginal: GameObject,
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
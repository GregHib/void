package world.gregs.voidps.world.interact.entity.obj.door

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.TargetContext
import world.gregs.voidps.type.Tile

/**
 * Walks a player through a door which other players can't walk through
 */
data class EnterDoor(
    override val character: Player,
    override val target: GameObject,
    val ticks: Int = 3
) : TargetContext<Player, GameObject>, Event {

    var tile: Tile? = null

    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "open_door"
        else -> null
    }
}

fun enterDoor(handler: EnterDoor.(Player) -> Unit) {
    Events.handle("open_door", handler = handler)
}

/**
 * Enter through a doorway
 */
suspend fun Interaction<Player>.enterDoor(door: GameObject, ticks: Int = 3) {
    val open = EnterDoor(player, door, ticks)
    player.emit(open)
    player.walkOverDelay(open.tile ?: return)
}

/**
 * Enter through a door with fixed [delay]
 */
suspend fun Interaction<Player>.enterDoor(door: GameObject, ticks: Int = 3, delay: Int) {
    val open = EnterDoor(player, door, ticks)
    player.emit(open)
    player.walkTo(open.tile ?: return, noCollision = true, forceWalk = true)
    delay(delay)
}
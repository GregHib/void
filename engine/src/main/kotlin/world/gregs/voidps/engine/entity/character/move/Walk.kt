package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.PathFinder
import org.rsmod.pathfinder.Route
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.map.Distance.getNearest
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions

fun NPC.walkTo(target: Any, force: Boolean = false) {
    movement.queueRouteStep(when(target) {
        is Entity -> target.tile
        is Tile -> target
        else -> return
    }, force)
}

fun Player.walkTo(
    target: Any,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    action: ((Route) -> Unit)? = null
) {
    walkTo(when(target) {
        is Entity -> target.tile
        is Tile -> target
        else -> return
    }, when (target) {
        is Entity -> target.size
        is Tile -> Size.ONE
        else -> return
    }, watch, distance, cancelAction, action)
}

fun Player.walkTo(
    target: Tile,
    targetSize: Size,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    block: ((Route) -> Unit)? = null
) {
    walkTo(target, targetSize, watch, distance, cancelAction, true, block)
}

private val EMPTY = Route(ArrayDeque(), false, false)

fun Character.clearWalk() {
    val watch: Character? = getOrNull("walk_watch")
    if (watch != null) {
        watch(null)
        face(watch)
    }
    clear("walk_stop")
    clear("walk_path")
    clear("walk_block")
    clear("walk_watch")

    clear("walk_target")
    clear("walk_distance")
    clear("walk_character")
    watch?.get<MutableList<Character>>("walk_followers")?.remove(this)
}

/**
 * @param target goal location and if it has been reached
 * @param watch character to watch while moving
 * @param distance distance within [target] to execute [block]
 * @param cancelAction whether to interrupt the current action
 * @param stop when target is reached or continue moving if target moves
 * @param block callback once [target] or target [distance] has been reached
 */
private fun Player.walkTo(
    target: Tile,
    targetSize: Size,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    stop: Boolean = true,
    block: ((Route) -> Unit)? = null
) = cancelAction(cancelAction) {
    clear("walk_stop")
    clear("walk_path")
    clear("walk_block")
    clear("walk_watch")

    //DefaultReachStrategy
    if (stop && (/*target.reached(tile, size) ||*/ withinDistance(tile, size, target, targetSize, distance))) {
        block?.invoke(EMPTY)
        return@cancelAction
    }

    this["walk_distance"] = distance
    watch?.getOrPut("walk_followers") { mutableListOf<Character>() }?.add(this)
    dialogues.clear()
    watch(null)
    if (watch != null) {
        watch(watch)
        set("walk_watch", watch)
    }
    val pf = PathFinder(flags = world.gregs.voidps.engine.utility.get<Collisions>().data, useRouteBlockerFlags = true)
    val route = pf.findPath(
        tile.x,
        tile.y,
        target.x,
        target.y,
        tile.plane,
        srcSize = size.width,
        destWidth = targetSize.width,
        destHeight = targetSize.height)
    movement.queueRouteTurns(route)
    set("walk_stop", stop)
    set("walk_path", movement.route ?: EMPTY)
    if (block != null) {
        set("walk_block", block)
    }
}

private fun Character.cancelAction(cancelAction: Boolean, block: () -> Unit) {
    if (cancelAction && action.type != ActionType.None) {
        this["walk_cancel"] = block
        action.cancel()
    } else {
        block()
    }
}

fun Character.cantReach(path: Route?, distance: Int = 0): Boolean {
    return path!= null && (path.failed || (path.alternative /*&& !path.strategy.reached(tile, size) && !withinDistance(tile, size, path.strategy, distance)*/))
}

fun withinDistance(tile: Tile, size: Size, target: Tile, targetSize: Size, distance: Int, walls: Boolean = false, ignore: Boolean = true): Boolean {
    if (Overlap.isUnder(tile, size, target, targetSize)) {
        return false
    }
    return distance > 0 && tile.distanceTo(target, targetSize) <= distance && tile.withinSight(getNearest(target, targetSize, tile), walls = walls, ignore = ignore)
}

fun Player.interact(event: Event) {
    if (!events.emit(event)) {
        noInterest()
    }
}
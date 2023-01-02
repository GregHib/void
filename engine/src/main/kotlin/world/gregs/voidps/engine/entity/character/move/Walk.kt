package world.gregs.voidps.engine.entity.character.move

import org.rsmod.pathfinder.PathFinder
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
import world.gregs.voidps.engine.path.strat.TileTargetStrategy

fun NPC.walkTo(target: Any, force: Boolean = false) {
    movement.queueRouteStep(when(target) {
        is TileTargetStrategy -> target.tile
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
    action: ((MutableRoute) -> Unit)? = null
) {
    walkTo(TargetStrategies.getStrategy(target), watch, distance, cancelAction, action)
}

fun Player.walkTo(
    strategy: TileTargetStrategy,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    block: ((MutableRoute) -> Unit)? = null
) {
    walkTo(strategy, watch, distance, cancelAction, true, block)
}

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
    target: TileTargetStrategy,
    watch: Character? = null,
    distance: Int = 0,
    cancelAction: Boolean = false,
    stop: Boolean = true,
    block: ((MutableRoute) -> Unit)? = null
) = cancelAction(cancelAction) {
    clear("walk_stop")
    clear("walk_path")
    clear("walk_block")
    clear("walk_watch")

    if (stop && (target.reached(tile, size) || withinDistance(tile, size, target, distance))) {
        block?.invoke(MutableRoute.EMPTY)
        return@cancelAction
    }

    this["walk_target"] = target
    this["walk_distance"] = distance
    watch?.getOrPut("walk_followers") { mutableListOf<Character>() }?.add(this)
    if (this is Player) {
        dialogues.clear()
        watch(null)
    }
    if (watch != null) {
        watch(watch)
        set("walk_watch", watch)
    }
    val pf = PathFinder(flags = world.gregs.voidps.engine.utility.get<Collisions>().data, useRouteBlockerFlags = true)
    val route = pf.findPath(
        tile.x,
        tile.y,
        target.tile.x,
        target.tile.y,
        tile.plane,
        srcSize = size.width,
        destWidth = target.size.width,
        destHeight = target.size.height).toMutableRoute()
    movement.queueRouteTurns(route)
    set("walk_stop", stop)
    set("walk_path", movement.route ?: MutableRoute.EMPTY)
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

fun Character.cantReach(path: MutableRoute?, distance: Int = 0): Boolean {
    return path!= null && (path.failed || (path.partial /*&& !path.strategy.reached(tile, size) && !withinDistance(tile, size, path.strategy, distance)*/))
}

fun withinDistance(tile: Tile, size: Size, target: TileTargetStrategy, distance: Int, walls: Boolean = false, ignore: Boolean = true): Boolean {
    if (Overlap.isUnder(tile, size, target.tile, target.size)) {
        return false
    }
    return distance > 0 && tile.distanceTo(target.tile, target.size) <= distance && tile.withinSight(getNearest(target.tile, target.size, tile), walls = walls, ignore = ignore)
}

fun Player.interact(event: Event) {
    if (!events.emit(event)) {
        noInterest()
    }
}
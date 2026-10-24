package content.quest.free.ernest_the_chicken

import content.entity.obj.ObjectTeleports
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

/** Six personal switches and nine doors reproducing the historical quest puzzle.
 * IDs and variables come from the revision-634 cache. */
class ErnestLeverPuzzle(val teleports: ObjectTeleports) : Script {
    init {
        playerSpawn { setErnestPuzzle(get("ernest_levers", 0)) }
        for (index in 0..5) {
            objectOperate("Pull", "ernest_lever_${'a' + index}*") {
                val mask = get("ernest_levers", 0) xor (1 shl (index + 1))
                val raised = mask and (1 shl (index + 1)) != 0
                anim(if (raised) "ernest_lever_up" else "ernest_lever_down")
                delay()
                setErnestPuzzle(mask)
                message("You pull lever ${'A' + index} ${if (raised) "up" else "down"}.")
                message("You hear a clunk.")
            }
            objectOperate("Inspect", "ernest_lever_${'a' + index}*") {
                message("The lever is ${if (get("ernest_levers", 0) and (1 shl (index + 1)) != 0) "up" else "down"}.")
            }
        }
        for (index in 0..8) {
            val id = "ernest_puzzle_door_${index + 1}*"
            objectOperate("Open", id) { (target) -> passErnestDoor(index, target) }
            // These are solid area-object gates; their own collision can prevent melee reach.
            objectApproach("Open", id) { (target) -> passErnestDoor(index, target) }
        }
        objectOperate("Search", "ernest_candle_sconce") { (target) ->
            if (target.tile != Tile(3097, 3360)) return@objectOperate
            message("You find a hidden catch. A secret passage opens.")
            walkOverDelay(Tile(3095, 3359))
        }
        objectOperate("Pull", "ernest_secret_exit_lever") { (target) ->
            if (target.tile != Tile(3096, 3357)) return@objectOperate
            anim("human_lever_down")
            delay()
            walkOverDelay(Tile(3100, 3359))
        }
        objectOperate("Climb-down", "ernest_cellar_ladder_down") { (target) ->
            if (target.tile != Tile(3092, 3362)) return@objectOperate
            setErnestPuzzle(0)
            tele(3117, 9753)
        }
        objectOperate("Climb-up", "basic_ladder_up") { (target, option) ->
            if (target.tile != Tile(3117, 9754)) {
                val def = target.def(this)
                val id = def.stringId.ifEmpty { def.id.toString() }
                if (teleports.contains(id, target.tile, option)) {
                    delay()
                    teleports.teleport(this, target, option, def)
                }
                return@objectOperate
            }
            setErnestPuzzle(0)
            tele(3092, 3361)
        }
        takeable("oil_can") { item, telegrab ->
            if (item.tile != Tile(3092, 9755)) {
                item.id
            } else if (telegrab || tile.level != 0 || tile.x >= 3100) {
                message("You need to solve the lever puzzle and enter the room to take the oil can.")
                null
            } else {
                item.id
            }
        }
    }
}

/** Input bits A–F (0–5); output follows cache door varbits 1794–1802. */
internal fun ernestDoorMask(levers: Int): Int {
    val a = levers and 1 != 0
    val b = levers and 2 != 0
    val c = levers and 4 != 0
    val d = levers and 8 != 0
    val e = levers and 16 != 0
    val f = levers and 32 != 0
    val finalPassage = !a && !b && c && d && !e && f
    val open = listOf(
        !a && !b && d && e && f,
        !b && d && f,
        a && b && d,
        d,
        !e && f,
        finalPassage,
        !b && d && !f,
        a && b && !c && !d && !e && !f,
        !c && d || finalPassage,
    )
    return open.withIndex().sumOf { (index, allowed) -> if (allowed) 1 shl index else 0 }
}

/** Keep packed client state and server-resolved cache selectors consistent, including on login. */
private fun Player.setErnestPuzzle(mask: Int) {
    val doors = ernestDoorMask(mask shr 1)
    set("ernest_levers", mask)
    set("ernest_doors", doors)
    for (index in 0..5) set("ernest_lever_${'a' + index}_raised", (mask shr (index + 1)) and 1)
    for (index in 0..8) set("ernest_door_${index + 1}_unlocked", (doors shr index) and 1)
}

private suspend fun Player.passErnestDoor(index: Int, target: GameObject) {
    if (tile.distanceTo(target.tile) > 1) {
        approachRange(1)
        if (tile.distanceTo(target.tile) > 1) return
    }
    if (ernestDoorMask(get("ernest_levers", 0) shr 1) and (1 shl index) == 0) {
        message("This door is locked.")
        return
    }
    // Approach the correct side normally, then pass only this personal gate.
    val northSouth = index >= 5
    val start = if (northSouth) Tile(target.x, if (tile.y > target.y) target.y + 1 else target.y - 1) else Tile(if (tile.x > target.x) target.x + 1 else target.x - 1, target.y)
    walkToDelay(start, forceWalk = true)
    if (tile != start) {
        message("I can't reach that.")
        return
    }
    val end = if (northSouth) Tile(start.x, 2 * target.y - start.y) else Tile(2 * target.x - start.x, start.y)
    walkOverDelay(end)
}

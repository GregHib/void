package content.quest.free.ernest_the_chicken

import content.entity.obj.Replace
import content.entity.obj.door.Door
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Tile

/** Six personal switches and nine doors reproducing the historical quest puzzle.
 * IDs and variables come from the revision-634 cache. */
class ErnestLeverPuzzle : Script {
    private var passageBusy = false

    init {
        playerSpawn { setErnestPuzzle(get("ernest_levers", 0)) }
        objectOperate("Pull", "ernest_lever_*") { (target) ->
            val index = target.id.removePrefix("ernest_lever_").first() - 'a'
            val mask = get("ernest_levers", 0) xor (1 shl (index + 1))
            val raised = mask and (1 shl (index + 1)) != 0
            anim(if (raised) "ernest_lever_up" else "ernest_lever_down")
            delay()
            setErnestPuzzle(mask)
            message("You pull lever ${'A' + index} ${if (raised) "up" else "down"}.")
            message("You hear a clunk.")
        }
        objectOperate("Inspect", "ernest_lever_*") { (target) ->
            val index = target.id.removePrefix("ernest_lever_").first() - 'a'
            message("The lever is ${if (get("ernest_levers", 0) and (1 shl (index + 1)) != 0) "up" else "down"}.")
        }
        objectOperate("Open", "ernest_puzzle_door_*") { (target) -> passErnestDoor(target) }
        // These are solid area-object gates; their own collision can prevent melee reach.
        objectApproach("Open", "ernest_puzzle_door_*") { (target) -> passErnestDoor(target) }
        objectOperate("Search", "ernest_candle_sconce") { (target) ->
            if (target.tile != Tile(3097, 3360)) return@objectOperate
            passSecretWall(entering = true)
        }
        objectOperate("Pull", "ernest_secret_exit_lever") { (target) ->
            if (target.tile != Tile(3096, 3357)) return@objectOperate
            passSecretWall(entering = false)
        }
        objTeleportLand("Climb-down", "ernest_cellar_ladder_down") { _, _ -> setErnestPuzzle(0) }
        objTeleportLand("Climb-up", "basic_ladder_up") { target, _ ->
            if (target.tile == Tile(3117, 9754)) setErnestPuzzle(0)
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

    private suspend fun Player.passSecretWall(entering: Boolean) {
        if (passageBusy) {
            message("The secret passage is already moving. Please wait a moment.")
            return
        }
        anim(if (entering) "take" else "human_lever_down")
        delay()
        // Reach the opening in two cardinal legs while the wall is still closed.
        val approachRow = Tile(tile.x, 3359)
        walkToDelay(approachRow, forceWalk = true)
        if (tile != approachRow) return
        val approach = Tile(if (entering) 3097 else 3096, 3359)
        walkToDelay(approach, forceWalk = true)
        if (tile != approach) return
        if (passageBusy) {
            message("The secret passage is already moving. Please wait a moment.")
            return
        }
        val south = GameObjects.findOrNull(Tile(3097, 3358), "ernest_secret_wall_south_closed") ?: return
        val north = GameObjects.findOrNull(Tile(3097, 3359), "ernest_secret_wall_north_closed") ?: return
        passageBusy = true
        // Always hinge into the western room, including when the inner lever is used.
        Replace.objects(
            south, "ernest_secret_wall_south_opened", Tile(3096, 3358), 3,
            north, "ernest_secret_wall_north_opened", Tile(3096, 3359), 1,
            ticks = 12,
            onRevert = { passageBusy = false },
        )
        try {
            if (entering) message("You find a hidden catch. A secret passage opens.")
            delay()
            val destination = Tile(if (entering) 3096 else 3097, 3359)
            walkOverDelay(destination)
            // Let the client finish rendering the last step before replacing the wall leaves.
            if (tile == destination) delay()
        } finally {
            // The world timer also closes it if UI/movement discards the suspended interaction.
            GameObjects.timers.execute(south)
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

private suspend fun Player.passErnestDoor(target: GameObject) {
    val index = target.id.removePrefix("ernest_puzzle_door_").substringBefore('_').toInt() - 1
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
    if (!GameObjects.contains(target) || target.id.endsWith("_opened")) {
        message("The door is already moving. Please wait a moment.")
        return
    }
    val end = if (northSouth) Tile(start.x, 2 * target.y - start.y) else Tile(2 * target.x - start.x, start.y)
    // The cache provides a wall-shaped leaf for each scenery-shaped personal gate.
    // Keep the original collision: seeing another player open a door must not bypass a lock.
    target.replace(
        "ernest_puzzle_door_${index + 1}_opened",
        tile = Door.tile(target, 1),
        shape = ObjectShape.WALL_STRAIGHT,
        rotation = (target.rotation + 1) and 3,
        ticks = 12,
        collision = false,
        onRevert = { areaSound("iron_door_close", target.tile) },
    )
    areaSound("iron_door_open", target.tile)
    try {
        delay()
        walkOverDelay(end)
        if (tile == end) delay()
    } finally {
        // The timer restores the gate even if movement or logout discards this interaction.
        GameObjects.timers.execute(target)
    }
}

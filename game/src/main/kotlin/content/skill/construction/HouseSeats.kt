package content.skill.construction

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

/**
 * Chairs, benches and thrones can be sat on. The seated animations hold the seat's model
 * so the seat is swapped for an invisible one, keeping its collision, until the player gets up by walking away.
 */
class HouseSeats : Script {
    init {
        objectOperate("Sit-on", SEATS) { (target) ->
            if (contains("house_seat") || !GameObjects.contains(target)) {
                return@objectOperate
            }
            val diagonal = target.shape == ObjectShape.CENTRE_PIECE_DIAGONAL
            val bench = diningBench(target)
            val from = tile
            anim(if (diagonal) "chair_sit_diagonal" else "chair_sit", delay = 17)
            if (bench) {
                // Dining benches are pushed back from the table as they're sat on
                target.replace("invisible_seat", collision = false)
                pushBack(target)
            }
            exactMoveDelay(target.tile, startDelay = 17, delay = 30, direction = facing(target))
            if (!bench) {
                if (!GameObjects.contains(target)) {
                    return@objectOperate
                }
                target.replace("invisible_seat", collision = false)
            }
            anim(seated(target))
            set("house_seat", target)
            set("house_seat_from", from)
            softTimers.start("house_seat")
            walkTrigger { stand(animate = true) }
        }

        // Replay the seated animation so it doesn't run out and is seen by players arriving later
        timerStart("house_seat") { if (contains("house_seat")) 50 else Timer.CANCEL }

        timerTick("house_seat") {
            val seat: GameObject = get("house_seat") ?: return@timerTick Timer.CANCEL
            anim(seated(seat))
            Timer.CONTINUE
        }

        timerStop("house_seat") { stand(animate = false) }

        moved {
            val seat: GameObject = get("house_seat") ?: return@moved
            if (tile != seat.tile) {
                stand(animate = false)
            }
        }
    }

    /**
     * Gets up from the seat back onto the tile sat down from, putting the seat back if it hasn't been already.
     */
    private fun Player.stand(animate: Boolean) {
        val seat: GameObject = remove("house_seat") ?: return
        val from: Tile = remove("house_seat_from") ?: seat.tile
        clearWalkTrigger()
        softTimers.stop("house_seat")
        if (!animate) {
            restore(seat)
            return
        }
        if (diningBench(seat)) {
            // Dining benches are pulled back out before returning to their place
            pushBack(seat)
            // Put back next tick along with the pushed back bench being removed
            World.queue("house_seat_${seat.tile.id}") { restore(seat) }
        } else {
            restore(seat)
        }
        start("movement_delay", 1)
        anim(if (seat.shape == ObjectShape.CENTRE_PIECE_DIAGONAL) "chair_get_up_diagonal" else "chair_get_up")
        exactMove(from, delay = 25, direction = facing(seat))
    }

    /**
     * Benches around the dining table, throne room benches are against the wall so aren't moved
     */
    private fun diningBench(seat: GameObject) = seat.id.contains("bench") && !seat.id.endsWith("_throne_room")

    /**
     * Puts [seat] back in place of its invisible placeholder, unless the house was reloaded since sitting down
     */
    private fun restore(seat: GameObject) {
        val placeholder = GameObjects.getLayer(seat.tile, ObjectLayer.layer(seat.shape))
        if (placeholder?.id == "invisible_seat") {
            placeholder.replace(seat.id, collision = false)
        }
    }

    /**
     * Shows [seat] on the tile behind it for a tick if there's nothing already there
     */
    private fun pushBack(seat: GameObject) {
        val behind = seat.tile.add(facing(seat).inverse())
        if (GameObjects.getLayer(behind, ObjectLayer.layer(seat.shape)) == null) {
            GameObjects.add(seat.id, behind, seat.shape, seat.rotation, ticks = 1, collision = false)
        }
    }

    /**
     * Seats face away from their rotation
     */
    private fun facing(seat: GameObject): Direction = directions[seat.rotation and 0x3]

    /**
     * The seated animation for [seat], throne room benches use the same as the dining room
     */
    private fun seated(seat: GameObject): String {
        val id = "sit_${seat.id.removeSuffix("_throne_room")}"
        return if (seat.shape == ObjectShape.CENTRE_PIECE_DIAGONAL && id.endsWith("chair")) "${id}_diagonal" else id
    }

    companion object {
        private const val SEATS = "crude_wooden_chair,wooden_chair,rocking_chair,oak_chair,oak_armchair,teak_armchair,mahogany_armchair," +
            "wooden_bench,oak_bench,carved_oak_bench,teak_dining_bench,carved_teak_bench,carved_teak_bench_throne_room,mahogany_bench,mahogany_bench_throne_room,gilded_bench,gilded_bench_throne_room," +
            "oak_throne,teak_throne,mahogany_throne,gilded_throne,skeleton_throne,crystal_throne,demonic_throne"
        private val directions = arrayOf(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST)
    }
}

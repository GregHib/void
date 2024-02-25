package world.gregs.voidps.world.activity.dnd.shootingstar

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

object ShootingStarHandler {

    var earlyBird: Player? = null
    var totalCollected: Int = 0
    val objects: GameObjects by inject()
    val npcs: NPCs by inject()
    val players: Players by inject()
    var currentStarTile = Tile.EMPTY
    var currentActiveObject: GameObject? = null
    val startEvent = TimeUnit.HOURS.toTicks(random.nextInt(1, 2))

    fun addStarDustCollected() {
        totalCollected++
    }

    fun handleMinedStarDust(currentMinedStar: GameObject) {
        val starPayout = currentMinedStar.def["collect_for_next_layer", -1]
        if (totalCollected >= starPayout) {
            val stage = currentMinedStar.id.takeLast(1)
            if (stage.equals("1")) {
                currentMinedStar.remove()
                cleanseEvent(false)
                return
            }
            val nextStage = currentMinedStar.id.replace(stage, (stage.toInt() - 1).toString())
            val nextStar = currentMinedStar.replace(nextStage)
            totalCollected = 0
            changeStar(currentMinedStar.id, nextStar.id)
        }
    }

    fun cleanseEvent(forceStopped: Boolean) {
        val existing = currentActiveObject?.let { objects.get(currentStarTile, it.id) }
        if (existing != null) {
            existing.remove(existing.intId, true)
        }
        if (!forceStopped) {
            val starSprite = npcs.add("star_sprite", currentStarTile, Direction.NONE, 0)
            World.queue("start_sprite_despawn_timer", TimeUnit.MINUTES.toTicks(10)) {
                npcs.remove(starSprite)
            }
        }
        totalCollected = 0
        currentStarTile = Tile.EMPTY
        currentActiveObject = null
        earlyBird = null
    }

    fun changeStar(oldStar: String, newStar: String): Boolean {
        val existing = objects.get(currentStarTile, oldStar)
        if (existing != null) {
            currentActiveObject = existing.replace(newStar)
            return true
        }
        return false
    }

    fun isEarlyBird(player: Player): Boolean {
        if (earlyBird == null) {
            earlyBird = player
            return true
        }
        return false
    }
}

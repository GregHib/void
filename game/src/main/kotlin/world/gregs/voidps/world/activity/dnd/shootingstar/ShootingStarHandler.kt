package world.gregs.voidps.world.activity.dnd.shootingstar

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.sound.areaSound
import java.util.concurrent.TimeUnit

object ShootingStarHandler {

    var earlyBird: Boolean = false
    var totalCollected: Int = 0
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
            if (stage == "1") {
                currentMinedStar.remove()
                return
            }
            val nextStage = currentMinedStar.id.replace(stage, (stage.toInt() - 1).toString())
            val nextStar = currentMinedStar.replace(nextStage)
            totalCollected = 0
            currentActiveObject = nextStar
            areaSound("star_meteor_change", currentStarTile, radius = 10)
        }
    }

    fun isEarlyBird(): Boolean {
        if (!earlyBird) {
            earlyBird = true
            return true
        }
        return false
    }
}

package world.gregs.voidps.world.activity.dnd.shootingstar

import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.sound.playSound
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
            if (stage.equals("1")) {
                currentMinedStar.remove()
                return
            }
            val nextStage = currentMinedStar.id.replace(stage, (stage.toInt() - 1).toString())
            val nextStar = currentMinedStar.replace(nextStage)
            totalCollected = 0
            changeStar(currentMinedStar.id, nextStar.id)
        }
    }

    fun changeStar(oldStar: String, newStar: String): Boolean {
        val objects: GameObjects = get()
        val existing = objects.get(currentStarTile, oldStar)
        if (existing != null) {
            currentActiveObject = existing.replace(newStar)
            playSoundForPlayers("star_meteor_change")
            return true
        }
        return false
    }

    fun playSoundForPlayers(soundId: String) {
        val players: Players = get()
        for (player in players.get(currentStarTile.zone)) {
            if (currentStarTile.distanceTo(player) <= 5) { // make sure that the players are within 5 tiles to play sound?
                player.playSound(soundId)
            }
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

package world.gregs.voidps.world.activity.dnd.shootingstar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.sound.areaSound
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object ShootingStarHandler {

    var earlyBird: Boolean = false
    var totalCollected: Int = 0
    var currentStarTile = Tile.EMPTY
    var currentActiveObject: GameObject? = null
    val startEvent = TimeUnit.HOURS.toTicks(random.nextInt(1, 2))

    fun addStarDustCollected() {
        totalCollected++
    }

    fun rewardPlayerBonusOre(player: Player): Boolean {
        return player.timers.contains("shooting_star_bonus_ore_timer")
    }

    fun extraOreHandler(player: Player, ore: String, xpReward: Double) {
        if(getChance() && rewardPlayerBonusOre(player)) {
            player.message("<dark_green>You managed to mine an extra ore from the rock.")
            player.inventory.add(ore)
            player.experience.add(Skill.Mining, xpReward)
        }
    }

    private fun getChance(): Boolean {
        val chance = Random.nextInt(100) + 1
        return chance <= 25
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
package content.entity.player.stat

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer

/**
 * Bonus XP Weekend
 * Boosts all experience gained while active, the multiplier starts at 2.7x
 * and decreases for every 30 minutes a player spends online, down to 1.1x.
 * https://runescape.wiki/w/Bonus_XP_Weekend
 */
class BonusExperience : Script {

    init {
        playerSpawn {
            if (!Settings["events.bonusExperience.enabled", false]) {
                reset(this)
                return@playerSpawn
            }
            experience.multiplier = multiplier(get("bonus_xp_time", 0))
            softTimers.start("bonus_xp")
            set("bonus_xp_enabled", true)
            sendScript("refresh_bonus_experience")
            message("Bonus XP Weekend is now active!")
        }

        timerStart("bonus_xp") { 100 } // 1 minute

        timerTick("bonus_xp") {
            val minutes = inc("bonus_xp_time")
            experience.multiplier = multiplier(minutes)
            sendScript("refresh_bonus_experience")
            return@timerTick Timer.CONTINUE
        }

        experience { _, from, to ->
            val multiplier = experience.multiplier
            if (multiplier > 1.0) {
                val increase = to - from
                inc("bonus_xp_counter", increase - (increase / multiplier).toInt())
                sendScript("refresh_bonus_experience")
            }
        }
    }

    fun reset(player: Player) {
        if (player["bonus_xp_time", 0] > 0 || player["bonus_xp_counter", 0] > 0) {
            player["bonus_xp_time"] = 0
            player["bonus_xp_counter"] = 0
            player.sendScript("refresh_bonus_experience")
        }
    }

    companion object {
        /**
         * Experience multipliers for each 30 minutes spent online.
         */
        private val multipliers = doubleArrayOf(
            2.7, 2.55, 2.4, 2.25, 2.1, 2.0, 1.9, 1.8, 1.7, 1.6, 1.5,
            1.45, 1.4, 1.35, 1.3, 1.25, 1.2, 1.175, 1.15, 1.125, 1.1,
        )

        fun multiplier(minutes: Int): Double = multipliers[((minutes - 1) / 30).coerceIn(0, multipliers.lastIndex)]
    }
}

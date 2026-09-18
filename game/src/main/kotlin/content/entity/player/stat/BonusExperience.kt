package content.entity.player.stat

import content.social.report.epochSeconds
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
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
            startBonusExperience()
        }

        // Reloading the settings switches the event on or off for anyone already online, the same
        // way the console command does
        settingsReload {
            syncBonusExperience()
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

    fun reset(player: Player) = player.resetBonusExperience()

    companion object {
        /**
         * When the running event started, or zero while it's off.
         *
         * Progress is saved, so without knowing which event a player earned it in, someone who
         * spent a previous weekend online would start a new one already decayed to the floor
         * multiplier. Nothing in the server persists world state, so this is the second the event
         * began rather than a count of events; a count would restart at zero and collide with the
         * stamps players saved before the restart.
         */
        var event: Int = 0
            private set

        /**
         * Identity of the running event, starting one if it isn't already.
         */
        fun begin(): Int {
            if (event == 0) {
                event = epochSeconds()
            }
            return event
        }

        fun end() {
            event = 0
        }

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

/**
 * Bring everyone online in line with the bonus experience setting, returning how many players it
 * changed anything for.
 */
fun syncBonusExperience(): Int {
    val enabled = Settings["events.bonusExperience.enabled", false]
    if (!enabled) {
        // Switching it on again is a new event, which nobody has taken part in yet
        BonusExperience.end()
    }
    var changed = 0
    for (player in Players) {
        if (enabled == player["bonus_xp_enabled", false]) {
            continue
        }
        if (enabled) {
            player.startBonusExperience()
        } else {
            player.stopBonusExperience()
        }
        changed++
    }
    return changed
}

/**
 * Start boosting a player's experience, from however long they'd spent online during this event.
 *
 * Shared by logging in while the event is on and by switching it on underneath players who are
 * already online. Time and bonus earned before it started don't count towards it, but a player who
 * logs back in during the same event keeps the progress they made in it.
 */
fun Player.startBonusExperience() {
    val event = BonusExperience.begin()
    if (this["bonus_xp_event", 0] != event) {
        this["bonus_xp_time"] = 0
        this["bonus_xp_counter"] = 0
        this["bonus_xp_event"] = event
    }
    experience.multiplier = BonusExperience.multiplier(this["bonus_xp_time", 0])
    softTimers.start("bonus_xp")
    this["bonus_xp_enabled"] = true
    sendScript("refresh_bonus_experience")
    message("Bonus XP Weekend is now active!")
}

/**
 * Stop boosting a player's experience and put their progress back to nothing, as logging in outside
 * the event does.
 */
fun Player.stopBonusExperience() {
    experience.multiplier = 1.0
    softTimers.clear("bonus_xp")
    this["bonus_xp_enabled"] = false
    resetBonusExperience()
    sendScript("refresh_bonus_experience")
    message("Bonus XP Weekend has ended.")
}

fun Player.resetBonusExperience() {
    this["bonus_xp_event"] = 0
    if (this["bonus_xp_time", 0] > 0 || this["bonus_xp_counter", 0] > 0) {
        this["bonus_xp_time"] = 0
        this["bonus_xp_counter"] = 0
        sendScript("refresh_bonus_experience")
    }
}

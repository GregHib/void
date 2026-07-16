package content.area.wilderness.abyss

import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random
import kotlin.random.nextInt

/**
 * The enchantments protecting the Abyss drain a player's Prayer points to zero on entry. An
 * abyssal parasite familiar absorbs that drain, turning it into a gradual leak of 10 points
 * every ~5-10 seconds for as long as the player stays in the Abyss and the parasite survives.
 */
val Player.hasAbyssalPrayerProtection: Boolean
    get() = follower?.id == "abyssal_parasite_familiar"

class AbyssPrayerDrain : Script {

    init {
        entered("abyss_multi_area") {
            if (hasAbyssalPrayerProtection) {
                softTimers.start("abyss_prayer_leak")
            }
        }

        exited("abyss_multi_area") {
            softTimers.stop("abyss_prayer_leak")
        }

        timerStart("abyss_prayer_leak") {
            random.nextInt(8..16)
        }

        timerTick("abyss_prayer_leak") {
            if (!hasAbyssalPrayerProtection) {
                // The parasite was dismissed or killed; the Abyss reclaims the rest.
                levels.drain(Skill.Prayer, levels.get(Skill.Prayer))
                return@timerTick Timer.CANCEL
            }
            levels.drain(Skill.Prayer, 10)
            random.nextInt(8..16)
        }
    }
}

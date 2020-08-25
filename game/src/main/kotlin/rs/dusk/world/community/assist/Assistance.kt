package rs.dusk.world.community.assist

import rs.dusk.engine.client.variable.getVar
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.skill.Skill
import rs.dusk.engine.entity.character.remove
import rs.dusk.engine.entity.character.set
import java.util.concurrent.TimeUnit
import kotlin.math.max

object Assistance {

    const val maximumExperience = 300000.0// 30k

    fun redirectSkillExperience(player: Player, skill: Skill) {
        player["blocked_${skill.name}"] = player.experience.blocked(skill)
        player.experience.addBlock(skill)
    }

    fun toggleInventory(player: Player, enabled: Boolean) {
        var settings: IntArray = if (enabled) intArrayOf(0, 1, 2, 6, 7, 9, 10, 11, 12, 13, 15, 17, 21) else intArrayOf()
        player.interfaces.sendSettings("inventory", "container", 0, 27, *settings)
        settings = if (enabled) intArrayOf(21) else intArrayOf()
        player.interfaces.sendSettings("inventory", "container", 28, 55, *settings)
    }

    fun canAssist(player: Player, assisted: Player, skill: Skill) =
        player.levels.getMax(skill) >= assisted.levels.getMax(skill)

    fun getHoursRemaining(player: Player): Int {
        val timeout = player["assist_timeout", 0L]
        if (timeout == 0L) {
            return 0
        }
        val remainingTime = timeout - System.currentTimeMillis()
        return max(0, TimeUnit.MILLISECONDS.toHours(remainingTime).toInt())
    }

    fun hasEarnedMaximumExperience(player: Player): Boolean {
        val earned = player.getVar("total_xp_earned", 0.0)
        return exceededMaximum(earned)
    }

    fun exceededMaximum(earned: Double): Boolean {
        return earned >= maximumExperience
    }

    fun stopRedirectingSkillExp(player: Player, skill: Skill) {
        val key = "blocked_${skill.name}"
        val blocked: Boolean = player.remove(key) ?: return
        if (blocked) {
            player.experience.addBlock(skill)
        } else {
            player.experience.removeBlock(skill)
        }
    }
}
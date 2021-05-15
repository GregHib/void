package world.gregs.voidps.world.community.assist

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.remove
import world.gregs.voidps.engine.entity.set
import java.util.concurrent.TimeUnit
import kotlin.math.max

object Assistance {

    const val maximumExperience = 300000.0// 30k

    fun redirectSkillExperience(player: Player, skill: Skill) {
        player["blocked_${skill.name}"] = player.experience.blocked(skill)
        player.experience.addBlock(skill)
    }

    fun toggleInventory(player: Player, enabled: Boolean) {
        if(enabled) {
            player.interfaceOptions.unlockAll("inventory", "container", 0 until 28)
            player.interfaceOptions.unlock("inventory", "container", 28 until 56, "Drag")
        } else {
            player.interfaceOptions.lockAll("inventory", "container", 0 until 56)
        }
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
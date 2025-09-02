package content.skill.constitution.drink

import content.area.wilderness.inWilderness
import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.TimerState
import world.gregs.voidps.type.sub.*
import java.util.concurrent.TimeUnit

class Overload {

    val skills = listOf(
        Skill.Attack,
        Skill.Strength,
        Skill.Defence,
        Skill.Magic,
        Skill.Ranged,
    )

    @Consume("overload*")
    fun overload(player: Player): Boolean {
        if (player.inWilderness) {
            player.message("You cannot drink an overload potion while you're in the wilderness.", ChatType.Game)
            return true
        } else if (player.timers.contains("overload")) {
            player.message("You may only use this potion every five minutes.")
            return true
        } else if (player.levels.get(Skill.Constitution) < 500) {
            player.message("You need more than 500 life points to survive the power of overload.")
            return true
        }
        return false
    }

    @Enter("wilderness")
    fun enter(player: Player) {
        if (!player.timers.contains("overload")) {
            return
        }
        for (skill in skills) {
            val max = player.levels.get(skill)
            val offset = player.levels.getOffset(skill)
            val superBoost = (max * if (skill == Skill.Ranged) 0.1 else 0.15).toInt() + (if (skill == Skill.Ranged) 4 else 5)
            if (offset > superBoost) {
                player.levels.drain(skill, offset - superBoost)
            }
        }
    }

    @Spawn
    fun spawn(player: Player) {
        if (player["overload_refreshes_remaining", 0] > 0) {
            player.timers.restart("overload")
        }
    }

    @TimerStart("overload")
    fun start(player: Player, restart: Boolean): Int {
        if (!restart) {
            applyBoost(player)
            player.queue(name = "overload_hits") {
                repeat(5) {
                    player.directHit(100)
                    player.anim("overload")
                    player.gfx("overload")
                    pause(2)
                }
            }
        }
        return TimeUnit.SECONDS.toTicks(15)
    }

    @TimerTick("overload")
    fun start(player: Player): Int {
        if (player.dec("overload_refreshes_remaining") <= 0) {
            return TimerState.CANCEL
        }
        if (!player.inWilderness) {
            applyBoost(player)
        }
        return TimerState.CONTINUE
    }

    @TimerStop("overload")
    fun stop(player: Player, logout: Boolean) {
        if (logout) {
            return
        }
        removeBoost(player)
        player.levels.restore(Skill.Constitution, 500)
        player.message("<dark_red>The effects of overload have worn off and you feel normal again.")
        player["overload_refreshes_remaining"] = 0
    }

    fun applyBoost(player: Player) {
        player.levels.boost(Skill.Attack, 5, 0.22)
        player.levels.boost(Skill.Strength, 5, 0.22)
        player.levels.boost(Skill.Defence, 5, 0.22)
        player.levels.boost(Skill.Magic, 7)
        player.levels.boost(Skill.Ranged, 4, 0.1923)
    }

    fun removeBoost(player: Player) {
        for (skill in skills) {
            reset(player, skill)
        }
    }

    fun reset(player: Player, skill: Skill) {
        if (player.levels.getOffset(skill) > 0) {
            player.levels.clear(skill)
        }
    }
}

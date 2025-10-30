package content.skill.prayer.active

import content.entity.combat.dead
import content.entity.combat.hit.combatAttack
import content.entity.proj.shoot
import content.skill.prayer.praying
import content.skill.summoning.isFamiliar
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.*

class SoulSplit : Script {

    init {
        combatAttack { player ->
            if (!usingSoulSplit(player) || damage < 5 || type == "deflect" || type == "cannon" || target.isFamiliar) {
                return@combatAttack
            }
            val time = player.shoot("soul_split", target, height = 10, endHeight = 10)
            target["soul_split_delay"] = CLIENT_TICKS.toTicks(time)
            target["soul_split_source"] = player
            target["soul_split_damage"] = damage
            target.gfx("soul_split_impact", time)
            target.softTimers.start("soul_split")
        }

        timerStart("soul_split", ::start)
        npcTimerStart("soul_split", ::start)
        timerStop("soul_split", ::stop)
        npcTimerStop("soul_split", ::stop)
    }

    fun start(character: Character, restart: Boolean): Int {
        return character.remove("soul_split_delay") ?: return Timer.CANCEL
    }

    fun stop(character: Character, logout: Boolean) {
        val player = character.remove<Character>("source_split_source") ?: return
        val damage = character.remove<Int>("source_split_damage") ?: return
        var heal = if (character is Player) 0.4 else 0.2
        if (character.dead) {
            heal += 0.05
        }
        player.levels.restore(Skill.Constitution, (damage * heal).toInt())
        if (damage >= 50) {
            character.levels.drain(Skill.Prayer, damage / 50)
        }
        character.shoot("soul_split", player, height = 10, endHeight = 10)
    }

    fun usingSoulSplit(player: Player) = player.praying("soul_split") && player.levels.getOffset(Skill.Constitution) < 0
}

package content.skill.prayer.active

import content.entity.combat.dead
import content.entity.combat.hit.combatAttack
import content.entity.proj.shoot
import content.skill.prayer.praying
import content.skill.summoning.isFamiliar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop

fun usingSoulSplit(player: Player) = player.praying("soul_split") && player.levels.getOffset(Skill.Constitution) < 0

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

characterTimerStart("soul_split") { character ->
    interval = character.remove("soul_split_delay") ?: return@characterTimerStart
}

characterTimerStop("soul_split") { target ->
    val player = target.remove<Character>("source_split_source") ?: return@characterTimerStop
    val damage = target.remove<Int>("source_split_damage") ?: return@characterTimerStop
    var heal = if (target is Player) 0.4 else 0.2
    if (target.dead) {
        heal += 0.05
    }
    player.levels.restore(Skill.Constitution, (damage * heal).toInt())
    if (damage >= 50) {
        target.levels.drain(Skill.Prayer, damage / 50)
    }
    target.shoot("soul_split", player, height = 10, endHeight = 10)
}

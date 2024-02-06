package world.gregs.voidps.world.interact.entity.player.combat.prayer.active

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.dead
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying
import world.gregs.voidps.world.interact.entity.proj.shoot

fun usingSoulSplit(player: Player) = player.praying("soul_split") && player.levels.getOffset(Skill.Constitution) < 0

combatAttack { player: Player ->
    if (!usingSoulSplit(player) || damage < 5 || type == "deflect" || type == "cannon" || target.isFamiliar) {
        return@combatAttack
    }
    val distance = player.tile.distanceTo(target)
    player.shoot("soul_split", target, height = 10, endHeight = 10)
    val ticks = Hit.magicDelay(distance)
    target["soul_split_distance"] = ticks
    target["soul_split_source"] = player
    target["soul_split_damage"] = damage
    target.setGraphic("soul_split_hit", TICKS.toClientTicks(ticks))
    target.softTimers.start("soul_split")
}

characterTimerStart("soul_split") { character: Character ->
    interval = character.remove("soul_split_distance") ?: return@characterTimerStart
}

characterTimerStop("soul_split") { target: Character ->
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
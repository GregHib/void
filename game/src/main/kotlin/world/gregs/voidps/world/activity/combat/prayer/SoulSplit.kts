import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TICKS
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.world.activity.combat.prayer.prayerActive
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun usingSoulSplit(player: Player) = player.prayerActive("soul_split") && !player.hasClock("blood_forfeit") && player.levels.getOffset(Skill.Constitution) < 0

on<CombatAttack>({ source -> source is Player && usingSoulSplit(source) && damage >= 5 && type != "deflect" && type != "cannon" && !target.isFamiliar }) { player: Character ->
    val distance = player.tile.distanceTo(target)
    player.shoot("soul_split", target, height = 10, endHeight = 10)
    val ticks = magicHitDelay(distance)
    target["soul_split_distance"] = ticks
    target["soul_split_source"] = player
    target["soul_split_damage"] = damage
    target.setGraphic("soul_split_hit", TICKS.toClientTicks(ticks))
    target.softTimers.start("soul_split")
}

on<TimerStart>({ timer == "soul_split" }) { character: Character ->
    interval = character.removeVar("soul_split_distance") ?: return@on
}

on<TimerStop>({ timer == "soul_split" }) { target: Character ->
    val player = target.removeVar<Character>("source_split_source") ?: return@on
    val damage = target.removeVar<Int>("source_split_damage") ?: return@on
    var heal = if (target is Player) 0.4 else 0.2
    if (target["dead", false]) {
        heal += 0.05
    }
    player.levels.restore(Skill.Constitution, (damage * heal).toInt())
    if (damage >= 50) {
        target.levels.drain(Skill.Prayer, damage / 50)
    }
    target.shoot("soul_split", player, height = 10, endHeight = 10)
}
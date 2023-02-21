import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.TICKS
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun usingSoulSplit(player: Player) = player.hasEffect("prayer_soul_split") && !player.hasEffect("blood_forfeit") && player.levels.getOffset(Skill.Constitution) < 0

on<CombatAttack>({ source -> source is Player && usingSoulSplit(source) && damage >= 5 && type != "deflect" && type != "cannon" && !target.isFamiliar }) { player: Character ->
    val distance = player.tile.distanceTo(target)
    player.shoot("soul_split", target, height = 10, endHeight = 10)
    val ticks = magicHitDelay(distance)
    target.setGraphic("soul_split_hit", TICKS.toClientTicks(ticks))
    target.delay(ticks) {
        var heal = if (target is Player) 0.4 else 0.2
        if (target.hasEffect("dead")) {
            heal += 0.05
        }
        player.levels.restore(Skill.Constitution, (damage * heal).toInt())
        if (damage >= 50) {
            target.levels.drain(Skill.Prayer, damage / 50)
        }
        target.shoot("soul_split", player, height = 10, endHeight = 10)
    }
}
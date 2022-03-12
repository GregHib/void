import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.world.activity.skill.summoning.isFamiliar
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.player.combat.magicHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun usingSoulSplit(player: Player) = player.hasEffect("prayer_soul_split") && !player.hasEffect("blood_forfeit") && player.levels.getOffset(Skill.Constitution) < 0

on<CombatHit>({ target -> source is Player && usingSoulSplit(source) && damage >= 5 && type != "deflect" && type != "cannon" && !target.isFamiliar }) { target: Character ->
    val player = source as Player
    val distance = player.tile.distanceTo(target)
    player.shoot("soul_split", target, height = 10, endHeight = 10)
    target.delay(magicHitDelay(distance)) {
        var heal = if (target is Player) 0.4 else 0.2
        if (target.hasEffect("dead")) {
            heal += 0.05
        }
        player.levels.restore(Skill.Constitution, (damage * heal).toInt())
        if (damage >= 50) {
            target.levels.drain(Skill.Prayer, damage / 50)
        }
        target.setGraphic("soul_split_hit")
        target.shoot("soul_split", player, height = 10, endHeight = 10)
    }
}
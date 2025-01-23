package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttackPrepare
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

fun seersVillageEliteTasks(player: Player) = false

specialAttackPrepare("sanctuary") { player ->
    cancel()
    if (!SpecialAttack.drain(player)) {
        return@specialAttackPrepare
    }
    player.setAnimation("${id}_special")
    player.setGraphic("${id}_special")
    player.playSound("${id}_special")
    player.say("For Camelot!")
    if (player.weapon.id.startsWith("enhanced")) {
        player.levels.boost(Skill.Defence, multiplier = 0.15)
        player[id] = TimeUnit.SECONDS.toTicks(if (seersVillageEliteTasks(player)) 24 else 12) / 4
        player.softTimers.start(id)
    } else {
        player.levels.boost(Skill.Defence, amount = 8)
    }
}


timerStart("sanctuary") {
    interval = 4
}

timerTick("sanctuary") { player ->
    val cycle = player["sanctuary", 1] - 1
    player["sanctuary"] = cycle
    if (cycle <= 0) {
        cancel()
        return@timerTick
    }
    player.levels.restore(Skill.Constitution, 40)
}


timerStop("sanctuary") { player ->
    player.clear("sanctuary")
}
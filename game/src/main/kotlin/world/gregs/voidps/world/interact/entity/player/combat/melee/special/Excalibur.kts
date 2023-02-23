package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import java.util.concurrent.TimeUnit

fun isExcalibur(weapon: Item?) = weapon != null && (weapon.id.startsWith("excalibur") || weapon.id.startsWith("enhanced_excalibur"))

fun seersVillageEliteTasks(player: Player) = false

on<VariableSet>({ key == "special_attack" && to == true && isExcalibur(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        return@on
    }
    player.setAnimation("sanctuary")
    player.setGraphic("sanctuary")
    player.forceChat = "For Camelot!"
    if (player.weapon.id.startsWith("enhanced")) {
        player.levels.boost(Skill.Defence, multiplier = 0.15)
        player["sanctuary"] = TimeUnit.SECONDS.toTicks(if (seersVillageEliteTasks(player)) 24 else 12) / 4
        player.softTimers.start("sanctuary")
    } else {
        player.levels.boost(Skill.Defence, amount = 8)
    }
    player.specialAttack = false
}


on<TimerStart>({ timer == "sanctuary" }) { _: Player ->
    interval = 4
}

on<TimerTick>({ timer == "sanctuary" }) { player: Player ->
    val cycle = player["sanctuary", 1] - 1
    player["sanctuary"] = cycle
    if (cycle <= 0) {
        return@on cancel()
    }
    player.levels.restore(Skill.Constitution, 40)
}


on<TimerStop>({ timer == "sanctuary" }) { player: Player ->
    player.clearVar("sanctuary")
}
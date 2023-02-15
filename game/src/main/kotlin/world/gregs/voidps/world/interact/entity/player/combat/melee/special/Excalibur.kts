package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.softTimer
import world.gregs.voidps.engine.timer.stopTimer
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack

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
        player.start("sanctuary", ticks = if (seersVillageEliteTasks(player)) 40 else 20)
        player.softTimer("sanctuary", 4)
    } else {
        player.levels.boost(Skill.Defence, amount = 8)
    }
    player.specialAttack = false
}


on<TimerTick>({ timer == "sanctuary" }) { player: Player ->
    player.levels.restore(Skill.Constitution, 40)
}

on<EffectStop>({ effect == "sanctuary" }) { player: Player ->
    player.stopTimer("sanctuary")
}
package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.forceChat
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.range.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.special.specialAttack

fun isExcalibur(weapon: Item?) = weapon != null && (weapon.name.startsWith("excalibur") || weapon.name.startsWith("enhanced_excalibur"))

fun seersVillageEliteTasks(player: Player) = false

on<VariableSet>({ key == "special_attack" && to == true && isExcalibur(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK)) {
        return@on
    }
    player.setAnimation("sanctuary")
    player.setGraphic("sanctuary")
    player.forceChat = "For Camelot!"
    if (player.weapon.name.startsWith("enhanced")) {
        player.levels.boost(Skill.Defence, multiplier = 0.15)
        player.start("sanctuary", ticks = if (seersVillageEliteTasks(player)) 40 else 20)
    } else {
        player.levels.boost(Skill.Defence, amount = 8)
    }
    player.specialAttack = false
}


on<EffectStart>({ effect == "sanctuary" }) { player: Player ->
    player["sanctuary_job"] = delay(player, 4, loop = true) {
        player.levels.restore(Skill.Constitution, 40)
    }
}

on<EffectStop>({ effect == "sanctuary" }) { player: Player ->
    player.remove<Job>("sanctuary_job")?.cancel()
}
package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

val handler: suspend CombatSwing.(Player) -> Unit = handler@{ player ->
    player.setAnimation("bow_accurate")
    player.setGraphic("special_arrow_shoot")
    player.playSound("magic_longbow_special")
    val time = player.shoot(id = "special_arrow", target = target)
    player.hit(target, delay = time)
    delay = player.weapon.def["attack_speed", 4] - (player.attackType == "rapid").toInt()
}
combatSwing("magic_longbow*", style = "range", special = true, block = handler)
combatSwing("magic_composite_bow*", style = "range", special = true, block = handler)
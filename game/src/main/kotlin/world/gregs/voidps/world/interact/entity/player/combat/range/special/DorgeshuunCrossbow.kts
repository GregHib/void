package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.proj.shoot

combatSwing("dorgeshuun_crossbow", style = "range", special = true) { player ->
    player.setAnimation("crossbow_accurate")
    val time = player.shoot(id = "bone_bolts_spec", target = target)
    player.hit(target, delay = time)
    delay = player.weapon.def["attack_speed", 4] - (player.attackType == "rapid").toInt()
}
package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

fun isZamorakianSpear(item: Item) = item.id.startsWith("zamorakian_spear")

weaponSwing("zamorakian_spear*", Priority.LOW) { player: Player ->
    player.setAnimation("zamorakian_spear_${
        when (player.attackType) {
            "block" -> "lunge"
            else -> player.attackType
        }
    }")
    player.hit(target)
    delay = 4
}

combatAttack({ !blocked && target is Player && isZamorakianSpear(target.weapon) }) { _: Character ->
    target.setAnimation("zamorakian_spear_block", delay)
    blocked = true
}
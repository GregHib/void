package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.combat.weaponSwing

fun isGoldenHammer(item: Item) = item.id == "golden_hammer"

weaponSwing("golden_hammer", Priority.LOW) { player: Player ->
    player.setAnimation("tzhaar_ket_om_attack")
    player.hit(target)
    delay = 6
}

combatAttack({ !blocked && target is Player && isGoldenHammer(target.weapon) }) { _: Character ->
    target.setAnimation("tzhaar_ket_om_block", delay)
    blocked = true
}
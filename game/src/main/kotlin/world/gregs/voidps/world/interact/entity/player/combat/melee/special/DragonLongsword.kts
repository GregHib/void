package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

fun isDragonLongsword(item: Item) = item.id.startsWith("dragon_longsword") || item.id.startsWith("corrupt_dragon_longsword")

combatSwing({ !swung() && it.specialAttack && isDragonLongsword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 4)) {
        delay = -1
        return@combatSwing
    }
    player.setAnimation("cleave")
    player.setGraphic("cleave")
    player.hit(target)
    delay = 5
}
package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isToragsHammers(item: Item) = item.id.startsWith("torags_hammers")

combatSwing({ !swung() && isToragsHammers(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("torags_hammers_attack")
    player.hit(target)
    delay = 5
}
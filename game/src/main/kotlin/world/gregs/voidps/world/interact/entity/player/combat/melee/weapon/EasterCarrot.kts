package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isEasterCarrot(item: Item) = item.id.startsWith("easter_carrot")

combatSwing({ !swung() && isEasterCarrot(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("easter_carrot_whack")
    player.hit(target)
    delay = 6
}
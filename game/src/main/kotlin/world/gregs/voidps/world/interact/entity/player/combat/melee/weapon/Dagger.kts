package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWeapon(item: Item?) = item != null && (isDagger(item) || isHarpoon(item) || isFunWeapon(item))
fun isDagger(item: Item) = item.name.endsWith("dagger") || item.name.endsWith("dagger_p") || item.name.endsWith("dagger_p+") || item.name.endsWith("dagger_p++") || item.name == "wolfbane" || item.name == "keris"
fun isHarpoon(item: Item) = item.name.endsWith("harpoon") || item.name.startsWith("harpoon")
fun isFunWeapon(item: Item) = item.name == "egg_whisk" || item.name == "magic_secateurs" || item.name == "cattleprod"

on<CombatSwing>({ !swung() && isWeapon(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("dagger_${
        when (player.attackType) {
            "lunge", "slash" -> "slash"
            else -> "stab"
        }
    }")
    player.hit(target)
    delay = 4
}

on<CombatHit>({ isWeapon(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("dagger_block")
}
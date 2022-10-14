package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.*

fun isWeapon(item: Item?) = item != null && (isDagger(item) || isHarpoon(item) || isFunWeapon(item))
fun isDagger(item: Item) = item.id.endsWith("dagger") || item.id.endsWith("dagger_p") || item.id.endsWith("dagger_p+") || item.id.endsWith("dagger_p++") || item.id == "wolfbane" || item.id == "keris"
fun isHarpoon(item: Item) = item.id.endsWith("harpoon") || item.id.startsWith("harpoon")
fun isFunWeapon(item: Item) = item.id == "egg_whisk" || item.id == "magic_secateurs" || item.id == "cattleprod"

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

on<CombatAttack>({ !blocked && target is Player && isWeapon(target.weapon) }, Priority.LOW) { _: Character ->
    target.setAnimation("dagger_block", delay)
    blocked = true
}
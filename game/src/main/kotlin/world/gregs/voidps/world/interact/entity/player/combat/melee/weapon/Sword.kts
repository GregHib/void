package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon

fun isWeapon(item: Item?) = item != null && (isSword(item) || item.id.endsWith("rapier") || isFunWeapon(item) || isObsidianWeapon(item))
fun isSword(item: Item) = item.id.endsWith("sword") && item.id != "shadow_sword" && !item.id.endsWith("2h_sword")
fun isFunWeapon(item: Item) = item.id == "spork" || item.id == "kitchen_knife"
fun isObsidianWeapon(item: Item) = item.id == "toktz_xil_ak"

on<CombatSwing>({ !swung() && isWeapon(it.weapon) }, Priority.LOWER) { player: Player ->
    player.setAnimation("sword_${
        when (player.attackType) {
            "stab", "block" -> "stab"
            else -> "slash"
        }
    }")
    player.hit(target)
    delay = 4
}
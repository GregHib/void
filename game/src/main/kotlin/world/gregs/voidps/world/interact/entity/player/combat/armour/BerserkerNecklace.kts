package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

fun isTzhaarWeapon(weapon: String?) = weapon != null && (weapon == "toktz-xil-ak" || weapon == "tzhaar-ket-om" || weapon == "tzhaar-ket-em" || weapon == "toktz-xil-ek")

on<HitDamageModifier>({ player -> type == "melee" && isTzhaarWeapon(weapon?.name) && player.equipped(EquipSlot.Amulet).name == "berserker_necklace" }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.20)
}
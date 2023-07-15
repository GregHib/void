package world.gregs.voidps.world.interact.entity.player.combat.melee.weapon

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

fun isDemonbaneWeapon(item: Item?) = item != null && (item.id == "silverlight" || item.id == "darklight" || item.id == "holy_water")

fun isDemon(target: Character?) = target is NPC && target["race", ""] == "demon"

on<HitDamageModifier>({ type == "melee" && isDemonbaneWeapon(weapon) && isDemon(target) }, Priority.LOW) { _: Player ->
    damage = floor(damage * 1.60)
}
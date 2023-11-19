package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

val areas: AreaDefinitions by inject()
val area = areas["kuradals_dungeon"]

on<HitDamageModifier>({ it.tile in area && it.equipped(EquipSlot.Ring).id.startsWith("ferocious_ring") }, Priority.LOWER) { _: Player ->
    damage = floor(damage * 1.04)
}
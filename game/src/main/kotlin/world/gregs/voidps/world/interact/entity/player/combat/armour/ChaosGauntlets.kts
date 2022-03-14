package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.spell

on<HitDamageModifier>({ type == "magic" && it.spell.endsWith("_bolt") && it.equipped(EquipSlot.Hands).id == "chaos_gauntlets" }, Priority.LOWEST) { _: Player ->
    damage += 30.0
}
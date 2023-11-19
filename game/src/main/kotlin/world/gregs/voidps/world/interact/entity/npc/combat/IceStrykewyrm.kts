package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import kotlin.math.floor

on<HitDamageModifier>({ type == "magic" && target is NPC && target.id == "ice_strykewyrm" }, priority = Priority.LOWER) { player: Player ->
    val fireCape = player.equipped(EquipSlot.Cape).id == "fire_cape"
    if (fireCape) {
        damage += 40
    }
    if (player.spell.startsWith("fire_")) {
        damage = floor(damage * if (fireCape) 2.0 else 1.5)
    }
}
package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier

on<HitDamageModifier>({ target is NPC && target.def.has("damage_cap") }, priority = Priority.LOWEST) { _: Player ->
    damage = damage.coerceAtMost((target as NPC).def["damage_cap"])
}
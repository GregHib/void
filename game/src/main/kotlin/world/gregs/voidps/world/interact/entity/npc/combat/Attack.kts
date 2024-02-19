package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.npcSwing

npcSwing(priority = Priority.LOWEST) { npc ->
    npc.setAnimation(attackAnimation(npc))
//    (target as? Player)?.playSound(attackSound(npc))
    npc.hit(target, delay = 1)
    delay = npc.def["attack_speed", 4]
}

npcSwing(priority = Priority.HIGHER) { npc ->
    if (npc.tile.distanceTo(target) > npc.def["attack_radius", 8]) {
        delay = -1
        npc.mode = Retreat(npc, target)
    }
}

fun attackAnimation(npc: NPC): String {
    if (npc.race.isNotEmpty()) {
        return "${npc.race}_attack"
    }
    return npc.def.getOrNull("hit_anim") ?: ""
}

fun attackSound(npc: NPC): String {
    if (npc.race.isNotEmpty()) {
        return "${npc.race}_attack"
    }
    return npc.def.getOrNull("hit_anim") ?: ""
}
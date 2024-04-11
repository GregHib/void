package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.data.definition.WeaponStyleDefinitions
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.npcCombatSwing

val definitions: WeaponStyleDefinitions by inject()

npcCombatSwing { npc ->
    if (npc.tile.distanceTo(target) > npc.def["attack_radius", 8]) {
        cancel()
        npc.mode = Retreat(npc, target)
        return@npcCombatSwing
    }
    npc.setAnimation(attackAnimation(npc))
//    (target as? Player)?.playSound(attackSound(npc))
    npc.hit(target)
}

fun attackAnimation(npc: NPC): String {
    if (npc.def.contains("weapon_style")) {
        val id = npc.def["weapon_style", "unarmed"]
        val styleDefinition = definitions.get(id)
        var style = styleDefinition.combatStyles.indexOf(npc.def["style"])
        if (style == -1) {
            style = 0
        }
        return "${styleDefinition.stringId}_${styleDefinition.attackTypes[style]}"
    }
    if (npc.race.isNotEmpty()) {
        return "${npc.race}_attack"
    }
    return npc.def["attack_anim", ""]
}

fun attackSound(npc: NPC): String {
    if (npc.race.isNotEmpty()) {
        return "${npc.race}_attack"
    }
    return npc.def.getOrNull("hit_sound") ?: ""
}
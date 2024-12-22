package world.gregs.voidps.world.interact.entity.npc.combat

import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.world.activity.skill.slayer.race

object NPCAttack {
    fun animation(npc: NPC, animationDefinitions: AnimationDefinitions): String {
        var animation = "${npc.id}_attack"
        if (animationDefinitions.contains(animation)) {
            return animation
        }
        if (npc.def.contains("attack_anim")) {
            animation = npc.def["attack_anim", ""]
            if (animationDefinitions.contains(animation)) {
                return animation
            }
        }
        if (npc.race.isNotEmpty()) {
            animation = "${npc.race}_attack"
            if (animationDefinitions.contains(animation)) {
                return animation
            }
        }
        return ""
    }
}
package content.entity.npc.combat

import content.skill.slayer.categories
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC

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
        for (category in npc.categories) {
            animation = "${category}_${npc.def["style", "unarmed"]}"
            if (animationDefinitions.contains(animation)) {
                return animation
            }
            animation = "${category}_attack"
            if (animationDefinitions.contains(animation)) {
                return animation
            }
        }
        return ""
    }
}
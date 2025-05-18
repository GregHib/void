package content.entity.npc.combat

import content.skill.slayer.categories
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.SoundDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC

object NPCAttack {
    fun anim(definitions: AnimationDefinitions, npc: NPC, type: String): String {
        var animation = "${npc.id}_${type}"
        if (definitions.contains(animation)) {
            return animation
        }
        if (npc.def.contains("${type}_anim")) {
            animation = npc.def["${type}_anim"]
            if (definitions.contains(animation)) {
                return animation
            }
        }
        if (npc.def.contains("combat_anims")) {
            animation = "${npc.def["combat_anims", ""]}_${type}"
            if (definitions.contains(animation)) {
                return animation
            }
        }
        return ""
    }
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

    fun sound(definitions: SoundDefinitions, npc: NPC, type: String): String {
        var animation = "${npc.id}_${type}"
        if (definitions.contains(animation)) {
            return animation
        }
        if (npc.def.contains("${type}_sound")) {
            animation = npc.def["${type}_sound"]
            if (definitions.contains(animation)) {
                return animation
            }
        }
        if (npc.def.contains("combat_sounds")) {
            animation = "${npc.def["combat_sounds", ""]}_${type}"
            if (definitions.contains(animation)) {
                return animation
            }
        }
        return ""
    }
}
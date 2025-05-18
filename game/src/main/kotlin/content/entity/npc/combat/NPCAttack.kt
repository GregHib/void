package content.entity.npc.combat

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
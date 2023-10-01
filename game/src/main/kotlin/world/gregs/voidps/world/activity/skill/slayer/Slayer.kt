package world.gregs.voidps.world.activity.skill.slayer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

val Player.hasSlayerTask: Boolean
    get() = this["slayer_task", false]

fun Player.isTask(character: Character?): Boolean {
    val target = character ?: return false
    if (target !is NPC) {
        return false
    }
    val race = target.def["race", ""]
    val type = this["slayer_type", "null"]
    return race == type
}

fun Player.unlocked(reward: String): Boolean {
    return false
}
package content.skill.slayer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

// shade, zombie, skeleton, ghost, zogre, ankou
val Character.undead: Boolean
    get() = if (this is NPC) categories.contains("undead") else false

val Player.hasSlayerTask: Boolean
    get() = this["slayer_task", false]

val NPC.categories: Set<String>
    get() = this.def["categories", emptySet()]

fun Player.isTask(character: Character?): Boolean {
    val target = character ?: return false
    if (target !is NPC) {
        return false
    }
    val type = this["slayer_type", "null"]
    return target.categories.contains(type)
}

fun Player.unlocked(reward: String): Boolean {
    return false
}
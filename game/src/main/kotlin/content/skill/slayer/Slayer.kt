package content.skill.slayer

import content.quest.questCompleted
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.type.random

// shade, zombie, skeleton, ghost, zogre, ankou
val Character.undead: Boolean
    get() = if (this is NPC) categories.contains("undead") else false

var Player.slayerTask: String
    get() = this["slayer_target", "nothing"]
    set(value) {
        this["slayer_target"] = value
    }

var Player.slayerMaster: String
    get() = this["slayer_master", ""]
    set(value) {
        this["slayer_master"] = value
    }

var Player.slayerTaskRemaining: Int
    get() = this["slayer_count", 0]
    set(value) {
        this["slayer_count"] = value
    }

var Player.slayerStreak: Int
    get() = this["slayer_task_streak", 0]
    set(value) {
        this["slayer_task_streak"] = value
    }

var Player.slayerTasks: Int
    get() = this["slayer_tasks_given", 0]
    set(value) {
        this["slayer_tasks_given"] = value
    }

var Player.slayerPoints: Int
    get() = this["slayer_points", 0]
    set(value) {
        this["slayer_points"] = value
    }

val NPC.categories: Set<String>
    get() = this.def["categories", emptySet()]

fun Player.isTask(character: Character?): Boolean {
    val target = character ?: return false
    if (target !is NPC) {
        return false
    }
    return target.categories.contains(slayerTask)
}

fun assignTask(player: Player, master: String): Pair<Int, Int> {
    val npc = rollTask(player, master)!!
    val amount = EnumDefinitions.string("${master}_task_amount", npc).toIntRange(inclusive = true).random(random)
    player.slayerTasks++
    player.slayerMaster = master
    player.slayerTask = EnumDefinitions.string("slayer_tasks_categories", npc)
    player.slayerTaskRemaining = amount
    return Pair(npc, amount)
}

fun rollTask(player: Player, master: String): Int? {
    var total = 0
    val weights = EnumDefinitions.getOrNull("${master}_task_weight")?.map ?: return null
    for ((npc, weight) in weights) {
        if (!hasRequirements(player, npc)) {
            continue
        }
        total += weight as Int
    }
    val roll = random.nextInt(total)
    var count = 0
    for ((npc, weight) in weights) {
        if (!hasRequirements(player, npc)) {
            continue
        }
        count += weight as Int
        if (roll < count) {
            return npc
        }
    }
    return null
}

private fun hasRequirements(player: Player, index: Int): Boolean {
    val slayerLevel = NPCDefinitions.get(index)["slayer_level", 1]
    if (!player.has(Skill.Slayer, slayerLevel)) {
        return false
    }
    val combatLevel = EnumDefinitions.int("slayer_task_combat_level", index)
    if (player.combatLevel < combatLevel) {
        return false
    }
    val quest = EnumDefinitions.stringOrNull("slayer_task_quest", index) ?: return true
    return player.questCompleted(quest)
}

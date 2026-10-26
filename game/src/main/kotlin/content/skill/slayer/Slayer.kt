package content.skill.slayer

import content.quest.questCompleted
import world.gregs.voidps.engine.data.definition.ColumnType
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
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

/**
 * Stand-in masters cover for the original while they're away and hand out the same task list.
 */
fun baseSlayerMaster(master: String): String = when (master) {
    "spria" -> "turael"
    "achtryn" -> "mazchna"
    "lapalok" -> "duradel"
    else -> master
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

var Player.slayerLastTask: String
    get() = this["slayer_last_task", "nothing"]
    set(value) {
        this["slayer_last_task"] = value
    }

const val BLOCKED_TASK_SLOTS = 5

val Player.blockedTasks: Set<String>
    get() = (0 until BLOCKED_TASK_SLOTS).filter { contains("blocked_task_$it") }.map { this["blocked_task_$it", "nothing"] }.toSet()

/**
 * Assigns a new task from [master], or null if the player isn't eligible for any of their tasks.
 * The same task is never assigned twice in a row unless it's the only one available.
 */
fun assignTask(player: Player, master: String): Pair<String, Int>? {
    val pair = rollTask(player, master, player.slayerLastTask) ?: rollTask(player, master, null) ?: return null
    player.slayerTasks++
    player.slayerMaster = master
    player.slayerTask = pair.first
    player.slayerTaskRemaining = pair.second
    player.slayerLastTask = pair.first
    return pair
}

private fun rollTask(player: Player, master: String, exclude: String?): Pair<String, Int>? {
    var total = 0
    val table = Tables.getOrNull("${master}_slayer_tasks") ?: return null
    val blocked = player.blockedTasks
    fun eligible(row: Int): Boolean {
        val category = Rows.get(row).rowId
        return category != exclude && category !in blocked && hasRequirements(player, row)
    }
    for (row in table.rows) {
        if (!eligible(row)) {
            continue
        }
        total += table.int("weight", row)
    }
    if (total <= 0) {
        return null
    }
    val roll = random.nextInt(total)
    var count = 0
    for (row in table.rows) {
        if (!eligible(row)) {
            continue
        }
        val weight = table.int("weight", row)
        count += weight
        if (roll < count) {
            val range = table.get("amount", row, ColumnType.ColumnIntRange)
            val row = Rows.get(row)
            return Pair(row.rowId, range.random(random))
        }
    }
    return null
}

private fun hasRequirements(player: Player, row: Int): Boolean {
    val category = Rows.get(row).rowId
    // A master listing a task with no matching slayer_tasks entry shouldn't crash the whole
    // assignment - skip it so the other tasks can still be rolled.
    val npc = Tables.intOrNull("slayer_tasks.$category.npc") ?: return false
    val slayerLevel = NPCDefinitions.get(npc)["slayer_level", 1]
    if (!player.has(Skill.Slayer, slayerLevel)) {
        return false
    }
    val combatLevel = Tables.int("slayer_tasks.$category.combat_level")
    if (player.combatLevel < combatLevel) {
        return false
    }
    val pair = Tables.skillPairOrNull("slayer_tasks.$category.skill")
    if (pair != null && !player.has(pair.first, pair.second)) {
        return false
    }
    val variable = Tables.stringOrNull("slayer_tasks.$category.variable")
    if (variable != null && !player.contains(variable)) {
        return false
    }
    val quest = Tables.stringOrNull("slayer_tasks.$category.quest") ?: return true
    return player.questCompleted(quest)
}

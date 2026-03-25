package content.skill.slayer

import content.quest.questCompleted
import world.gregs.voidps.engine.data.config.TableDefinition
import world.gregs.voidps.engine.data.definition.ColumnType
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
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

fun assignTask(player: Player, master: String): Pair<String, Int> {
    val pair = rollTask(player, master) ?: error("No task found for $master")
    player.slayerTasks++
    player.slayerMaster = master
    player.slayerTask = pair.first
    player.slayerTaskRemaining = pair.second
    return pair
}

private fun rollTask(player: Player, master: String): Pair<String, Int>? {
    var total = 0
    val table = Tables.getOrNull("${master}_slayer_tasks") ?: return null
    for (row in table.rows) {
        val weight = table.int("weight", row)
        if (!hasRequirements(player, table, row)) {
            continue
        }
        total += weight
    }
    val roll = random.nextInt(total)
    var count = 0
    for (row in table.rows) {
        if (!hasRequirements(player, table, row)) {
            continue
        }
        val weight = table.int("weight", row)
        count += weight
        if (roll < count) {
            val range = table.get("amount", row, ColumnType.ColumnIntRange)
            val row = Rows.get(row)
            return Pair(row.stringId, range.random(random))
        }
    }
    return null
}

private fun hasRequirements(player: Player, table: TableDefinition, row: Int): Boolean {
//    val slayerLevel = NPCDefinitions.get(index)["slayer_level", 1] // FIXME
//    if (!player.has(Skill.Slayer, slayerLevel)) {
//        return false
//    }
    val combatLevel = table.int("combat_level", row)
    if (player.combatLevel < combatLevel) {
        return false
    }
    val variable = table.stringOrNull("variable", row)
    if (variable != null && !player.contains(variable)) {
        return false
    }
    val quest = table.stringOrNull("quest", row) ?: return true
    return player.questCompleted(quest)
}

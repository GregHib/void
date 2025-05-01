package content.skill.slayer

import content.quest.questCompleted
import world.gregs.voidps.engine.data.config.SlayerTaskDefinition
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

fun rollTask(player: Player, definitions: Map<String, SlayerTaskDefinition>): SlayerTaskDefinition {
    var total = 0
    for (definition in definitions.values) {
        if (!hasRequirements(player, definition)) {
            continue
        }
        total += definition.weight
    }
    val roll = random.nextInt(total)
    var count = 0
    for (definition in definitions.values) {
        if (!hasRequirements(player, definition)) {
            continue
        }
        count += definition.weight
        if (roll < count) {
            return definition
        }
    }
    return SlayerTaskDefinition.EMPTY
}

private fun hasRequirements(player: Player, definition: SlayerTaskDefinition): Boolean {
    if (!player.has(Skill.Slayer, definition.slayerLevel)) {
        return false
    }
    if (player.combatLevel < definition.combatLevel) {
        return false
    }
    for (quest in definition.quests) {
        if (!player.questCompleted(quest)) {
            return false
        }
    }
    return true
}
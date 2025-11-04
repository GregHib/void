package content.entity.player.dialogue.type

import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.engine.suspend.SuspendableContext

private const val QUEST_START_ID = "quest_intro"

suspend fun SuspendableContext<Player>.startQuest(questId: String): Boolean {
    check(player.open(QUEST_START_ID)) { "Unable to open destroy dialogue for $questId $player" }
    val questDefinitions: QuestDefinitions = get()
    val quest = questDefinitions.getOrNull(questId)
    check(quest != null) { "Unable to find quest with id $questId $player" }
    val completed = player.questCompleted(questId)
    player.interfaces.sendVisibility("quest_intro", "start_choice_layer", !completed)
    player.interfaces.sendVisibility("quest_intro", "progress_status_layer", completed)
    val status = when (player.quest(questId)) {
        "completed" -> "Quest Complete!"
        "unstarted" -> "Not started"
        else -> "Started"
    }
    player.interfaces.sendText("quest_intro", "status_field", status)
    player.sendVariable("quest_intro_mark_map")
    player.interfaces.sendText("quest_intro", "quest_field", quest["name", ""])
    var requirements = buildString {
        for (q in quest["req_quests", emptyList<String>()]) {
            append(questDefinitions.get(q).name)
            append("<br>")
        }
        for ((skill, level) in quest["req_skills", emptyMap<String, Int>()]) {
            val s = Skill.valueOf(skill)
            if (player.hasMax(s, level)) {
                append("<str>Level $level $skill<br>")
            } else {
                append("Level $level $skill<br>")
            }
        }
    }.removeSuffix("<br>")
    if (requirements.isBlank()) {
        requirements = "None."
    }
    player.interfaces.sendText("quest_intro", "req_field", requirements)
    player.sendScript("quest_intro_req_text", requirements)

    val items = quest["req_items", "None."]
    if (items.startsWith("None")) {
        player.interfaces.sendVisibility("quest_intro", "items_hide_show_layer", false)
        player.interfaces.sendVisibility("quest_intro", "items_text_details_layer", true)
        player.interfaces.sendVisibility("quest_intro", "scroll_layer_item", true)
    }
    player.interfaces.sendText("quest_intro", "items_field", items)
    player.sendScript("quest_intro_req_items_text", items)
    val rewards = buildString {
        if (quest.contains("points")) {
            val points = quest["points", -1]
            append("$points Quest ${"Point".plural(points)}<br>")
        }
        if (quest.contains("xp")) {
            append((quest["xp", ""]))
            append("<br>")
        }
        if (quest.contains("reward")) {
            append(quest["reward", ""])
            append("<br>")
        }
    }.removeSuffix("<br>")
    player.interfaces.sendText("quest_intro", "rewards_field", rewards)
    player.sendScript("quest_intro_rewards_text", rewards)

    player.interfaces.sendText("quest_intro", "start_point_field", quest["start_point", ""])
    player.interfaces.sendText("quest_intro", "combat_field", quest["req_combat", "None."])
    if (quest.contains("sprite")) {
        player.interfaces.sendSprite("quest_intro", "quest_icon", quest["sprite", -1])
    }
    val result = StringSuspension.get(player) == "yes"
    player.close(QUEST_START_ID)
    return result
}

suspend fun Player.startQuest(questId: String): Boolean {
    check(open(QUEST_START_ID)) { "Unable to open destroy dialogue for $questId $this" }
    val questDefinitions: QuestDefinitions = get()
    val quest = questDefinitions.getOrNull(questId)
    check(quest != null) { "Unable to find quest with id $questId $this" }
    val completed = questCompleted(questId)
    interfaces.sendVisibility("quest_intro", "start_choice_layer", !completed)
    interfaces.sendVisibility("quest_intro", "progress_status_layer", completed)
    val status = when (quest(questId)) {
        "completed" -> "Quest Complete!"
        "unstarted" -> "Not started"
        else -> "Started"
    }
    interfaces.sendText("quest_intro", "status_field", status)
    sendVariable("quest_intro_mark_map")
    interfaces.sendText("quest_intro", "quest_field", quest["name", ""])
    var requirements = buildString {
        for (q in quest["req_quests", emptyList<String>()]) {
            append(questDefinitions.get(q).name)
            append("<br>")
        }
        for ((skill, level) in quest["req_skills", emptyMap<String, Int>()]) {
            val s = Skill.valueOf(skill)
            if (hasMax(s, level)) {
                append("<str>Level $level $skill<br>")
            } else {
                append("Level $level $skill<br>")
            }
        }
    }.removeSuffix("<br>")
    if (requirements.isBlank()) {
        requirements = "None."
    }
    interfaces.sendText("quest_intro", "req_field", requirements)
    sendScript("quest_intro_req_text", requirements)

    val items = quest["req_items", "None."]
    if (items.startsWith("None")) {
        interfaces.sendVisibility("quest_intro", "items_hide_show_layer", false)
        interfaces.sendVisibility("quest_intro", "items_text_details_layer", true)
        interfaces.sendVisibility("quest_intro", "scroll_layer_item", true)
    }
    interfaces.sendText("quest_intro", "items_field", items)
    sendScript("quest_intro_req_items_text", items)
    val rewards = buildString {
        if (quest.contains("points")) {
            val points = quest["points", -1]
            append("$points Quest ${"Point".plural(points)}<br>")
        }
        if (quest.contains("xp")) {
            append((quest["xp", ""]))
            append("<br>")
        }
        if (quest.contains("reward")) {
            append(quest["reward", ""])
            append("<br>")
        }
    }.removeSuffix("<br>")
    interfaces.sendText("quest_intro", "rewards_field", rewards)
    sendScript("quest_intro_rewards_text", rewards)

    interfaces.sendText("quest_intro", "start_point_field", quest["start_point", ""])
    interfaces.sendText("quest_intro", "combat_field", quest["req_combat", "None."])
    if (quest.contains("sprite")) {
        interfaces.sendSprite("quest_intro", "quest_icon", quest["sprite", -1])
    }
    val result = StringSuspension.get(this) == "yes"
    close(QUEST_START_ID)
    return result
}

class QuestStart : Script {

    init {
        interfaceOption("Show required items", "quest_intro:items_hidden_button_txt") {
            interfaces.sendVisibility("quest_intro", "items_hide_show_layer", false)
            interfaces.sendVisibility("quest_intro", "items_text_details_layer", true)
            interfaces.sendVisibility("quest_intro", "scroll_layer_item", true)
        }

        interfaceOption("Show rewards", "quest_intro:hidden_button_txt") {
            interfaces.sendVisibility("quest_intro", "hide_show_layer", false)
            interfaces.sendVisibility("quest_intro", "text_details_layer", true)
            interfaces.sendVisibility("quest_intro", "scroll_layer_rewards", true)
        }

        interfaceOption("Mark", "quest_intro:objective_set") {
            set("quest_intro_unmark_map", !get("quest_intro_unmark_map", false))
        }

        interfaceOption("Mark", "quest_intro:objective_text") {
            set("quest_intro_unmark_map", !get("quest_intro_unmark_map", false))
        }

        interfaceOption("No", "quest_intro:startno_layer") {
            (dialogueSuspension as? StringSuspension)?.resume("no")
        }

        interfaceOption("Yes", "quest_intro:startyes_layer") {
            (dialogueSuspension as? StringSuspension)?.resume("yes")
        }

        interfaceClose("quest_intro") {
            (dialogueSuspension as? StringSuspension)?.resume("no")
        }
    }
}

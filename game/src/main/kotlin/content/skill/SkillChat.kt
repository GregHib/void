package content.skill

import content.bot.chat.api.BotChatApi
import content.bot.chat.api.Style
import content.bot.chat.tag.ChatEntityType
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class SkillChat :
    Script,
    BotChatApi {

    init {
        botChat("ask_level") {
            val skill = slot(ChatEntityType.Skill)
            if (skill == null) {
                say("My combat level is: <CombatLevel>.")
                return@botChat
            }
            // The level itself is filled in from the bot's stats when sent
            say("My ${skill(skill.key).name} level is <SkillLevel>.")
        }

        botChat("ask_combat") {
            say("My combat level is: <CombatLevel>.")
        }

        botChat("ask_activity") {
            val activity = activity
            if (activity == null) {
                say("Meh.", style = Style.Curt)
                say(":-|")
                say("Not right now.")
                return@botChat
            }
            val phrase = Tables.stringOrNull("chat_skills.${activity.skill}.activity")
            if (!phrase.isNullOrEmpty() && activity.product != null) {
                say(phrase, activity.product, weight = 3f)
            }
            say("I am training: <MultipleChoice>.", activity.skill)
            type("${activity.skill} lol", style = Style.Slang)
            type("Just training ${activity.skill}.")
        }

        botChat("ask_where_train") {
            val skill = slot(ChatEntityType.Skill)
            if (skill == null) {
                say("I don't know.")
                say("Try looking in the Game Guide.")
                return@botChat
            }
            // Advice is for the speakers level, not the bot's
            val level = speaker.levels.getMax(skill(skill.key))
            val phrase = Tables.stringOrNull("chat_skills.${skill.key}.advice")
            val spots = Tables.intStrListOrNull("chat_skills.${skill.key}.spots") ?: emptyList()
            val available = spots.filter { it.first <= level }
            if (!phrase.isNullOrEmpty() && available.isNotEmpty()) {
                // Pick from the best two so bots don't all give identical advice
                say(phrase, available.takeLast(2).random(random).second, weight = 3f)
            }
            for ((required, tip) in Tables.intStrListOrNull("chat_skills.${skill.key}.tips") ?: emptyList()) {
                if (level >= required) {
                    say(tip)
                }
            }
            say("Try looking in the Game Guide.", weight = 0.2f)
        }
    }

    private fun skill(key: String): Skill = Skill.all[Skill.map.getValue(key)]
}

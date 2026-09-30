package content.skill

import content.bot.behaviour.product
import content.bot.behaviour.skill
import content.bot.chat.api.BotChatApi
import content.bot.chat.api.ChatStyle
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
                sayQuick("My combat level is: <CombatLevel>.")
                return@botChat
            }
            // The level itself is filled in from the bot's stats when sent
            sayQuick("My ${skill(skill.key).name} level is <SkillLevel>.")
        }

        botChat("ask_combat") {
            sayQuick("My combat level is: <CombatLevel>.")
        }

        botChat("ask_activity") {
            val skill = activity?.skill
            if (skill == null) {
                sayQuick("Meh.", style = ChatStyle.Blunt)
                sayQuick(":-|")
                sayQuick("Not right now.")
                return@botChat
            }
            val phrase = Tables.stringOrNull("chat_skills.$skill.activity")
            val product = activity?.product
            if (!phrase.isNullOrEmpty() && product != null) {
                sayQuick(phrase, product, weight = 3f)
            }
            sayQuick("I am training: <MultipleChoice>.", skill)
            say("$skill lol", style = ChatStyle.Slang)
            say("Just training $skill.")
        }

        botChat("ask_where_train") {
            val skill = slot(ChatEntityType.Skill)
            if (skill == null) {
                sayQuick("I don't know.")
                sayQuick("Try looking in the Game Guide.")
                return@botChat
            }
            // Advice is for the speakers level, not the bot's
            val level = speaker.levels.getMax(skill(skill.key))
            val phrase = Tables.stringOrNull("chat_skills.${skill.key}.advice")
            val spots = Tables.intStrListOrNull("chat_skills.${skill.key}.spots") ?: emptyList()
            val available = spots.filter { it.first <= level }
            if (!phrase.isNullOrEmpty() && available.isNotEmpty()) {
                // Pick from the best two so bots don't all give identical advice
                sayQuick(phrase, available.takeLast(2).random(random).second, weight = 3f)
            }
            for ((required, tip) in Tables.intStrListOrNull("chat_skills.${skill.key}.tips") ?: emptyList()) {
                if (level >= required) {
                    sayQuick(tip)
                }
            }
            sayQuick("Try looking in the Game Guide.", weight = 0.2f)
        }
    }

    private fun skill(key: String): Skill = Skill.all[Skill.map.getValue(key)]
}

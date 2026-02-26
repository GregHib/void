package content.area.misthalin.draynor_village.wise_old_man

import content.quest.wiseOldManScroll
import world.gregs.voidps.engine.Script

class OldMansMessage : Script {
    init {
        itemOption("Read", "old_mans_message") {
            when (get("wise_old_man_npc", "")) {
                "father_aereck" -> wiseOldManScroll(
                    listOf(
                        "",
                        "To Aereck, resident priest of Lumbridge,",
                        "greetings:",
                        "",
                        "I am pleased to inform you that, following a",
                        "careful search, the staff of the seminary have",
                        "located your pyjamas.",
                        "",
                        "Before returning them to you, however, they",
                        "would be very interested to know exactly how",
                        "these garments came to be in the graveyard.",
                        "",
                        "Please pass on my regards to Urhney.",
                        "*D",
                        "",
                        "",
                    )
                )
                "abbot_langley" -> wiseOldManScroll(
                    listOf(
                        "",
                        "To Langley, Abbot of the Monastery of",
                        "Saradomin, greetings:",
                        "",
                        "Long has it been since our last meeting, my",
                        "friend, too long. Truly we are living in",
                        "tumultuous times, and the foul works of Zamorak",
                        "can be seen across the lands. Indeed, I hear a",
                        "whisper from the south that the power of the",
                        "Terrible One has been rediscovered!",
                        "But be of good cheer, my friend, for we are all",
                        "in the hands of Lord Saradomin.",
                        "",
                        "Until our next meeting, then,",
                        "*D",
                        "",
                    )
                )
                "high_priest_entrana" -> wiseOldManScroll(
                    listOf(
                        "To the High Priest of Entrana, greetings:",
                        "",
                        "In an effort to respond to your recent questions",
                        "about the effects of summoning the power of",
                        "Saradomin, I have spent some time searching",
                        "through the scrolls of Kolodion the Battle Mage.",
                        "He records that a bolt of lightning falls from",
                        "above, accompanied by a resounding crash, and",
                        "the victim loses up to 20 points of health.",
                        "However, he believed that this could be increased",
                        "by 50% should one be wearing the Cape of",
                        "Saradomin and be Charged when casting the",
                        "spell.",
                        "",
                        "Fare thee well, my young friend, - *D",
                        "",
                    )
                )
                "father_lawrence" -> wiseOldManScroll(
                    listOf(
                        "",
                        "To Lawrence, resident priest of Varrock,",
                        "greetings:",
                        "",
                        "Despite our recent conversation on this matter, I",
                        "hear that you are still often found in a less than",
                        "sober condition. I am forced to repeat the",
                        "warning I gave you at the time: if you continue",
                        "to indulge yourself in this manner, the Council",
                        "will have no choice but to transfer you to",
                        "Entrana where you can be supervised more",
                        "carefully.",
                        "",
                        "I trust you will heed this message.",
                        "*D",
                        "",
                    )
                )
                "thurgo" -> wiseOldManScroll(
                    listOf(
                        "",
                        "To Thurgo, master blacksmith, greetings:",
                        "",
                        "Following your request, I have spent some time",
                        "re-reading the relevant scrolls in the Library of",
                        "Varrock. It appears that when your forefathers",
                        "encountered that adventurer, he was on a quest",
                        "to find a mysterious shield.",
                        "",
                        "Many thanks for the recipe you sent me; I shall",
                        "certainly try this 'redberry pie' of which you",
                        "speak so highly.",
                        "",
                        "Regards,",
                        "*D",
                        "",
                    )
                )
            }
        }
    }
}
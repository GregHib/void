package content.area.misthalin.draynor_village.wise_old_man

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.wiseOldManScroll
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.random

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
                    ),
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
                    ),
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
                    ),
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
                    ),
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
                    ),
                )
            }
        }
    }

    companion object {
        suspend fun rewardLetter(player: Player): String? {
            if (player.inventory.isFull()) {
                player.npc<Neutral>("I'd give you a reward, but you don't seem to have any space for it. Come back when you do.")
                return null
            }
            player.inventory.remove("old_mans_message")
            player.clear("wise_old_man_npc")
            player.inc("wise_old_man_letters_completed")
            return reward(player, true)
        }

        suspend fun reward(player: Player, hard: Boolean): String {
            val drops: DropTables = get()
            val chance = random.nextInt(16)
            if (chance < 1) { // 6.25%
                val drops = drops.getValue("wise_old_man_gems").roll()
                give(player, drops)
                return drops.first().id
            } else if (chance < 3) { // 12.5%
                if (!repeat(player, "wise_old_man_runes", if (hard) 25 else 10)) {
                    player.statement("Unfortunately you don't have space for all the runes.")
                }
                return "runes"
            } else if (chance < 5) { // 12.5%
                if (!repeat(player, "wise_old_man_herbs", if (hard) 10 else 3)) {
                    player.statement("Unfortunately you don't have space for all the herbs.")
                }
                return "herbs"
            } else if (chance < 7) { // 12.5%
                if (!repeat(player, "wise_old_man_herbs", if (hard) 10 else 3)) {
                    player.statement("Unfortunately you don't have space for all the seeds.")
                }
                return "seeds"
            } else if (player.has(Skill.Prayer, 3) && chance < 14) { // 43.75%
                val range = if (hard) 215..430 else 185..370
                val amount = range.random(random)
                player.exp(Skill.Prayer, amount.toDouble())
                return "prayer"
            } else { // 12.5% or 56.25% depending on prayer level
                val range = if (hard) 990..1020 else 185..215
                player.inventory.add("coins", range.random(random))
                return "coins"
            }
        }

        private fun repeat(player: Player, table: String, max: Int): Boolean {
            val tables: DropTables = get()
            val table = tables.getValue(table)
            val drops = mutableListOf<ItemDrop>()
            val count = random.nextInt(1, max + 1)
            for (i in 0 until count) {
                table.roll(list = drops)
            }
            return give(player, drops)
        }

        private fun give(player: Player, drops: List<ItemDrop>): Boolean {
            var dropped = false
            for (drop in drops) {
                val item = drop.toItem()
                if (!player.addOrDrop(item.id, item.amount)) {
                    dropped = true
                }
            }
            return !dropped
        }
    }
}

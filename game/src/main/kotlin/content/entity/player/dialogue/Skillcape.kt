package content.entity.player.dialogue

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

// Constitution's max level is in life points (990 at level 99), not 1-99 like every other skill.
private fun masteryLevel(skill: Skill) = if (skill == Skill.Constitution) 990 else Level.MAX_LEVEL

fun Player.skillcapeMastered(skill: Skill): Boolean = levels.getMax(skill) >= masteryLevel(skill)

fun Player.otherSkillMaxed(skill: Skill): Boolean = Skill.entries.any { it != skill && skillcapeMastered(it) }

/**
 * The shop variant a skill master should open: the base [shop] until the player has 99 in
 * [skill], then the skillcape variant, trimmed once a second skill is also mastered.
 */
fun Player.skillcapeShop(skill: Skill, shop: String): String = when {
    !skillcapeMastered(skill) -> shop
    otherSkillMaxed(skill) -> "${shop}_trimmed"
    else -> "${shop}_skillcape"
}

suspend fun Player.buySkillcape(
    skill: Skill,
    success: String = "Excellent! Wear that cape with pride my friend.",
    deficient: String = "But, unfortunately, I was mistaken.",
) {
    val id = skill.name.lowercase()
    inventory.transaction {
        remove("coins", 99_000)
        add("${id}_cape${if (otherSkillMaxed(skill)) "_t" else ""}")
        add("${id}_hood")
    }
    when (inventory.transaction.error) {
        TransactionError.None -> {
            jingle("buy_skillcape")
            npc<Happy>(success)
        }
        is TransactionError.Deficient -> {
            player<Sad>(deficient)
            npc<Idle>("Well, come back and see me when you do.")
        }
        else -> npc<Sad>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
    }
}

suspend fun Player.skillcapeOffer(
    skill: Skill,
    mastery: String,
    success: String = "Excellent! Wear that cape with pride my friend.",
) {
    npc<Neutral>("Ah, but I see you are already $mastery, perhaps you have come to me to purchase a Skillcape of ${skill.name}, and thus join the elite few who have mastered this exacting skill?")
    choice {
        option("Yes, I'd like to buy one please.") {
            player<Happy>("Yes, I'd like to buy one please.")
            npc<Neutral>("Most certainly; unfortunately being such a prestigious item, they are appropriately expensive. I'm afraid I must ask you for 99,000 gold.")
            choice {
                option<Quiz>("99,000 coins? That's much too expensive.") {
                    npc<Idle>("Not at all; there are many other adventurers who would love the opportunity to purchase such a prestigious item! You can find me here if you change your mind.")
                }
                option("I think I have the money right here, actually.") {
                    player<Happy>("I think I have the money right here, actually.")
                    buySkillcape(skill, success)
                }
            }
        }
        option<Neutral>("Nevermind.") {
            npc<Neutral>("No problem; there are many other adventurers who would love the opportunity to purchase such a prestigious item! You can find me here if you change your mind.")
        }
    }
}

suspend fun Player.skillcapeMasterDialogue(
    skill: Skill,
    mastery: String,
    success: String = "Excellent! Wear that cape with pride my friend.",
) {
    npc<Happy>("Ah, this is a Skillcape of ${skill.name}. I have mastered the art of ${skill.name.lowercase()} and wear it proudly to show others.")
    player<Quiz>("Hmm, interesting.")
    if (skillcapeMastered(skill)) {
        skillcapeOffer(skill, mastery, success)
    } else {
        choice {
            option("Please tell me more about skillcapes.") {
                player<Happy>("Please tell me more about skillcapes.")
                npc<Neutral>("Of course. Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
            }
            option<Neutral>("Bye.") {
                npc<Neutral>("Bye.")
            }
        }
    }
}

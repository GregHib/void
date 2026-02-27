package content.area.misthalin.edgeville.monastery

import content.area.misthalin.draynor_village.wise_old_man.OldMansMessage
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.*
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.carriesItem

class AbbotLangley : Script {
    init {
        npcOperate("Talk-to", "abbot_langley") {
            npc<Neutral>("Greetings traveller.")
            wiseOldManLetter()
            choice {
                option<Quiz>("Can you heal me? I'm injured.") {
                    npc<Neutral>("Ok.")
                    message("You feel a little better.")
                    gfx("heal")
                    areaSound("heal", tile, radius = 10)
                    levels.restore(Skill.Constitution, -levels.getOffset(Skill.Constitution))
                    statement("Abbot Langley places his hands on your head. You feel a little better.")
                }
                option<Quiz>("Isn't this place built a bit out of the way?") {
                    npc<Neutral>("We like it that way actually! We get disturbed less. We still get rather a large amount of travellers looking for sanctuary and healing here as it is!")
                }
                if (!get("edgeville_monastery_order_member", false)) {
                    option<Quiz>("How do I get further into the monastery?") {
                        npc<Neutral>("I'm sorry but only members of our order are allowed in the second level of the monastery.")
                        choice {
                            option<Neutral>("Well can I join your order?") {
                                canIJoin()
                            }
                            option<Sad>("Oh, sorry.")
                        }
                    }
                }
            }
        }
    }

    private suspend fun Player.canIJoin() {
        if (!has(Skill.Prayer, 31)) {
            npc<Neutral>("No. I am sorry, but I feel you are not devout enough.")
            message("You need a prayer level of 31 to join the order.")
            return
        }
        npc<Happy>("Ok, I see you are someone suitable for our order. You may join.")
        set("edgeville_monastery_order_member", true)
    }

    private suspend fun Player.wiseOldManLetter() {
        if (get("wise_old_man_npc", "") != "abbot_langley" || !carriesItem("old_mans_message")) {
            return
        }
        player<Happy>("I've got a message for you from your friend in Draynor Village.")
        npc<Happy>("Gosh, you are very kind to bring a message to my remote monastery!")
        val reward = OldMansMessage.reward(this) ?: return
        when (reward) {
            "runes" -> items("nature_rune", "water_rune", "Abbot Langley gives you some runes.") // TODO proper message
            "herbs" -> {
                item("grimy_tarromin", 400, "<navy>Abbot Langley gives you some banknotes that can be exchanged for herbs.")
                npc<Happy>("I grow a few herbs in my little cabbage patch; please take some as a sign of my gratitude.")
            }
            "seeds" -> item("potato_seed", 400, "<navy>Abbot Langley gives you some seeds.") // TODO proper message
            "prayer" -> {
                item(167, "<navy>Abbot Langley blesses you.<br>You gain some Prayer xp.")
                npc<Happy>("Allow me to bestow on you Saradomin's blessings...")
            }
            "coins" -> item("coins_8", 400, "<navy>Abbot Langley gives you some coins.")
            else -> item(reward, 400, "Abbot Langley gives you an ${reward.toSentenceCase()}!") // TODO proper message
        }
    }
}

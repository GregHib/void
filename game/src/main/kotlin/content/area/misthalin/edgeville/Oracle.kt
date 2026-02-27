package content.area.misthalin.edgeville

import content.area.misthalin.draynor_village.wise_old_man.OldMansMessage
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.type.random

class Oracle : Script {
    init {
        npcOperate("Talk-to", "oracle") {
            wiseOldManLetter()
            player<Quiz>("Can you impart your wise knowledge to me, O Oracle?")
            when (random.nextInt(0, 30)) {
                0 -> npc<Neutral>("They say that ham does not mix well with other kinds of meat.")
                1 -> npc<Neutral>("Capes are always in fashion!")
                2 -> npc<Sad>("The goblins will never make up their minds on their own.")
                3 -> npc<Sad>("No. I'm not in the mood.")
                4 -> npc<Neutral>("An answer is unimportant; it is the question that matters.")
                5 -> npc<Neutral>("Nothing like a tasty fish.")
                6 -> npc<Neutral>("Is it time to wake up? I am not sure...")
                7 -> npc<Neutral>("There are no crisps at the party.")
                8 -> npc<Neutral>("Don't judge a book by its cover -  judge it on its' grammar and, punctuation.")
                9 -> npc<Neutral>("Pies...they're great, aren't they?")
                10 -> npc<Neutral>("It's not you; it's me.")
                11 -> npc<Neutral>("Help wanted? Enquire within.")
                12 -> npc<Neutral>("Jas left a stone behind.")
                13 -> npc<Neutral>("Too many cooks spoil the anchovy pizza.")
                14 -> npc<Shifty>("Do not fear the dragons...fear their kin.")
                15 -> npc<Neutral>("A bird in the hand can make a tasty snack.")
                16 -> npc<Neutral>("Sometimes you get lucky, sometimes you don't.")
                17 -> npc<Neutral>("The God Wars are over...as long as the thing they were fighting over remains hidden.")
                18 -> npc<Neutral>("Everyone you know will one day be dead.")
                19 -> npc<Neutral>("If a tree falls in the forest and no one is around, then nobody gets Woodcutting xp.")
                20 -> npc<Neutral>("The chicken came before the egg.")
                21 -> npc<Neutral>("A woodchuck does not chuck wood.")
                22 -> npc<Neutral>("When in Asgarnia, do as the Asgarnians do.")
                23 -> npc<Neutral>("Many secrets are buried under this land.")
                24 -> npc<Neutral>("The light at the end of the tunnel is the demon-infested lava pit.")
                25 -> npc<Happy>("Yes, I can. But I'm not going to.")
                26 -> npc<Neutral>("The great snake of Guthix guards more than she knows.")
                27 -> npc<Neutral>("Beware the cabbage: it is both green AND leafy.")
                28 -> npc<Neutral>("He who uses the power of custard mixes it with his tears.")
                29 -> npc<Neutral>("Who guards the guardsmen?")
            }
        }
    }

    private suspend fun Player.wiseOldManLetter() {
        if (get("wise_old_man_npc", "") != "oracle" || !carriesItem("old_mans_message")) {
            return
        }
        player<Happy>("I've got a message for you from the Wise Old Man who lives in Draynor Village.")
        npc<Happy>("Many do my wisdom seek; few do their own wisdom to me send!")
        val reward = OldMansMessage.reward(this) ?: return
        when (reward) {
            "runes" -> {
                items("nature_rune", "water_rune", "The Oracle gives you some runes.") // TODO proper message
            }
            "herbs" -> {
                item("grimy_tarromin", 400, "<navy>The Oracle gives you some herbs.") // TODO proper message
            }
            "seeds" -> {
                item("potato_seed", 400, "<navy>The Oracle gives you some seeds.")
                npc<Happy>("New life from these shall perchance spring!")
            }
            "prayer" -> {
                item(167, "<navy>The Oracle blesses you.<br>You gain some Prayer xp.") // TODO proper message
            }
            "coins" -> {
                item("coins_8", 400, "<navy>The Oracle gives you some coins.") // TODO proper message
            }
            else -> {
                item(reward, 400, "The Oracle gives you an ${reward.toSentenceCase()}!") // TODO proper message
                npc<Happy>("I found this while I was mining. Hope you like it.")
            }
        }
    }
}

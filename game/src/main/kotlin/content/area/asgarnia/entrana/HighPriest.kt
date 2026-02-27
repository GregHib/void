package content.area.asgarnia.entrana

import content.area.misthalin.draynor_village.wise_old_man.OldMansMessage
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem

class HighPriest : Script {
    init {
        npcOperate("Talk-to", "high_priest_entrana") {
            npc<Happy>("Many greetings. Welcome to our fair island.")
            wiseOldManLetter()
            npc<Happy>("Enjoy your stay here. May it be spiritually uplifting!")
        }

        itemOnNPCOperate(npc = "high_priest_entrana") {
            npc<Neutral>("No thank you, I am not accepting donations for the church at this time.")
        }
    }

    private suspend fun Player.wiseOldManLetter() {
        if (get("wise_old_man_npc", "") != "high_priest_entrana" || !carriesItem("old_mans_message")) {
            return
        }
        player<Happy>("I've got a message for you from the Wise Old Man in Draynor Village.")
        npc<Happy>("How kind of you to bring me a message to this remote island!")
        val reward = OldMansMessage.reward(this) ?: return
        when (reward) {
            "runes" -> items("nature_rune", "water_rune", "The High Priest gives you some runes.") // TODO proper message
            "herbs" -> npc<Happy>("Here, let me give you some herbs.")
            "seeds" -> item("potato_seed", 400, "<navy>The High Priest gives you some seeds.") // TODO proper message
            "prayer" -> {
                item(167, "<navy>The High Priest blesses you.<br>You gain some Prayer xp.")
                npc<Happy>("In the name of Saradomin I shall bless you...")
            }
            "coins" -> {
                item("coins_8", 400, "The High Priest gives you some coins.")
                npc<Happy>("I don't have much in the way of wealth, but I can spare you a few coins for your trouble.")
            }
            else -> {
                item(reward, 400, "The High Priest gives you an ${reward.toLowerSpaceCase()}!")
                npc<Happy>("The beasts which dwell under this island occasionally drop gems when they die - please take this one as a sign of my gratitude.")
            }
        }
    }
}

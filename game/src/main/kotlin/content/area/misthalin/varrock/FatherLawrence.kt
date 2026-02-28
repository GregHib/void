package content.area.misthalin.varrock

import content.area.misthalin.draynor_village.wise_old_man.OldMansMessage
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem

class FatherLawrence : Script {
    init {
        npcOperate("Talk-to", "father_lawrence") {
            wiseOldManLetter()
            npc<Happy>("Oh, to be a father in the times of whiskey! I sing and I drink and I wake up in gutters.")
            player<Shifty>("Good morning.")
            npc<Happy>("Top of the morning to you.")
        }
    }

    private suspend fun Player.wiseOldManLetter() {
        if (get("wise_old_man_npc", "") != "father_lawrence" || !carriesItem("old_mans_message")) {
            return
        }
        player<Happy>("The Wise Old Man of Draynor Village sent you this message about your drinking habits!")
        npc<Drunk>("mssge? wha messsge?")
        npc<Drunk>("oh, msesesge for me.")
        message("Oh dear, he doesn't look like he's going to be able to read the message!")
        val reward = OldMansMessage.rewardLetter(this) ?: return
        when (reward) {
            "runes" -> {
                items("nature_rune", "water_rune", "Father Lawrence gives you some runes.")
                npc<Drunk>("Whee! Mmmgic power, kazamm...")
            }
            "herbs" -> {
                item("grimy_tarromin", 400, "<navy>Father Lawrence gives you some herbs.") // TODO proper message
            }
            "seeds" -> {
                item("potato_seed", 400, "<navy>Father Lawrence gives you some seeds.") // TODO proper message
            }
            "prayer" -> {
                item(167, "<navy>Father lawrence blesses you.<br>You gain some Prayer xp.")
                npc<Drunk>("in nomine saradomini, blah blah blah...")
            }
            "coins" -> {
                item("coins_8", 400, "<navy>Father Lawrence gives you some coins.")
                npc<Drunk>("here, hve som munny.")
            }
            else -> {
                item(reward, 400, "Father Lawrence gives you an ${reward.toSentenceCase()}!") // TODO proper message
                npc<Happy>("I found this while I was mining. Hope you like it.")
            }
        }
    }
}

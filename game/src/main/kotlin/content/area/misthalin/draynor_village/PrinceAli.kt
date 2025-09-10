package content.area.misthalin.draynor_village

import content.entity.effect.transform
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit
import world.gregs.voidps.engine.event.Script
@Script
class PrinceAli {

    val disguise = listOf(
        Item("pink_skirt"),
        Item("wig_blonde"),
        Item("paste"),
    )
    
    init {
        npcOperate("Talk-to", "prince_ali") {
            when (player.quest("prince_ali_rescue")) {
                "completed" -> npc<Talk>("I owe you my life for that escape. You cannot help me this time, they know who you are. Go in peace, friend of Al-Kharid.")
                "keli_tied_up" -> escape()
                "prince_ali_disguise" -> leave()
            }
        }

        itemOnNPCOperate("wig_blonde", "prince_ali") {
            escape()
        }

        itemOnNPCOperate("pink_skirt", "prince_ali") {
            escape()
        }

    }

    suspend fun TargetInteraction<Player, NPC>.leave() {
        npc<Happy>("Thank you, my friend. I must leave you now, but my father will pay you well for this.")
        player<Happy>("Go to Leela, she is close to here.")
        target.hide = true
        target.softQueue("ali_respawn", TimeUnit.SECONDS.toTicks(60)) {
            target.hide = false
        }
        statement("The prince has escaped, well done! You are now a friend of Al-Kharid and may pass through the Al-Kharid toll gate for free.")
    }
    
    suspend fun TargetInteraction<Player, NPC>.escape() {
        player<Happy>("Prince, I've come to rescue you.")
        npc<Talk>("That is very very kind of you, how do I get out?")
        if (!player.inventory.contains(disguise)) {
            player<Happy>("I've already dealt with Lady Keli and the guard. I'm going to get you a disguise so the guards outside don't spot you leaving. I'll be back once I have it.")
            return
        }
        player<Happy>("With a disguise. I have removed the Lady Keli. She is tied up, but will not stay tied up for long.")
        player<Talk>("Take this disguise, and this key.")
        statement("You hand the disguise and the key to the prince.")
        player.inventory.remove(disguise)
        player.inventory.remove("bronze_key_prince_ali_rescue")
        target.transform("prince_ali_disguise")
        player["prince_ali_rescue"] = "prince_ali_disguise"
        leave()
    }
}

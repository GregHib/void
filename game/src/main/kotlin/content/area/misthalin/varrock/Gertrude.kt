package content.area.misthalin.varrock

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

/**
 * Minimal Gertrude dialogue. Once Gertrude's Cat quest is completed, lets the
 * player request a replacement kitten (the canonical post-quest pet acquisition
 * path). The quest itself is not yet implemented in Void; this dialogue is
 * inert until the quest sets `quest("gertrudes_cat")` to `"completed"`.
 */
class Gertrude : Script {

    init {
        npcOperate("Talk-to", "gertrude") {
            when (quest("gertrudes_cat")) {
                "completed" -> postQuest()
                else -> unstarted()
            }
        }
    }

    private suspend fun Player.postQuest() {
        npc<Happy>("Hello dear. How are my kittens treating you?")
        choice {
            option<Quiz>("Could I have another kitten, please?") {
                giveKitten()
            }
            option<Happy>("Just fine, thank you.") {
                npc<Happy>("Wonderful. Do come back if you'd like another one.")
            }
        }
    }

    private suspend fun Player.giveKitten() {
        if (inventory.contains("pet_kitten")) {
            npc<Sad>("It looks like you've already got a kitten with you. Come back when you'd like another.")
            return
        }
        if (get("pet_active_item", "") == "pet_kitten") {
            npc<Sad>("You've already got one of my kittens following you about. Look after that one first.")
            return
        }
        if (!inventory.add("pet_kitten")) {
            npc<Sad>("Come back when you've got room in your backpack, dear.")
            return
        }
        npc<Happy>("Here you go - take good care of her!")
    }

    private suspend fun Player.unstarted() {
        npc<Sad>("Oh dear, oh dear. Whatever shall I do...")
        player<Quiz>("What's wrong?")
        npc<Sad>("My poor little kitten has wandered off. Come back another time, dear, I haven't got the heart to chat.")
    }
}

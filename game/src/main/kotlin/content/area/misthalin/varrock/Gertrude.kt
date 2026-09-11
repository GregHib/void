package content.area.misthalin.varrock

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
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
        player<Quiz>("Hello, are you okay?")
        npc<Angry>("Do I look okay? Those kids drive me crazy.")
        npc<Sad>("I'm sorry. It's just that I've lost her.")
        player<Quiz>("Lost whom?")
        npc<Sad>("Fluffs, poor Fluffs. She never hurt anyone.")
        player<Quiz>("Who's Fluffs?")
        npc<Sad>("My beloved feline friend, Fluffs. She's been purring by my side for almost a decade. Please, could you go and search for her while I take care of the children?")
        choice {
            option<Idle>("Well, I suppose I could, though I'd need more details.") {

            }
            option<Quiz>("What's in it for me?") {

            }
            option<Idle>("Sorry, I'm too busy to play pet rescue.") {

            }
        }
    }
}
